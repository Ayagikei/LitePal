package org.litepal

import android.os.SystemClock
import kotlinx.coroutines.sync.Mutex
import org.litepal.LitePalContext.debugMode
import org.litepal.LitePalContext.durationThreshold
import org.litepal.LitePalContext.logger
import org.litepal.LitePalContext.useLock
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.locks.ReentrantLock
import kotlin.coroutines.CoroutineContext

internal val mutex = Mutex()

internal var dbSingleContextNullable: CoroutineContext? = null

val dbSingleContext: CoroutineContext
    get() = dbSingleContextNullable!!

val reentrantLock = ReentrantLock()

object LitePalContext {
    var useLock: Boolean = true

    var debugMode: Boolean = true

    var logger = { message: String -> println(message) }

    var durationThreshold = 200
}

private const val NO_EXTERNAL_TRANSACTION_THREAD_ID = -1L

private val externalTransactionDepth = ThreadLocal<Int>()

private val externalTransactionIdGenerator = AtomicInteger(1)

@Volatile
private var externalTransactionOwnerThreadId: Long = NO_EXTERNAL_TRANSACTION_THREAD_ID

@Volatile
private var externalTransactionId: Int = 0

@PublishedApi
internal fun assertNoCrossThreadExternalTransaction() {
    val ownerThreadId = externalTransactionOwnerThreadId
    if (ownerThreadId == NO_EXTERNAL_TRANSACTION_THREAD_ID) return
    val currentThreadId = Thread.currentThread().id
    if (ownerThreadId != currentThreadId) {
        throw IllegalStateException(
            "Cannot access LitePal database on thread '${Thread.currentThread().name}'. " +
                "A transaction (id=$externalTransactionId) is active on a different thread (id=$ownerThreadId). " +
                "This usually happens when using coroutines and the coroutine resumes on a different thread " +
                "after calling beginTransaction(). Use LitePal.runInTransaction{} (non-suspending) or keep " +
                "all transaction calls on the same thread."
        )
    }
}

internal fun beginExternalTransactionLockOrThrow() {
    val currentThreadId = Thread.currentThread().id
    val ownerThreadId = externalTransactionOwnerThreadId
    if (ownerThreadId != NO_EXTERNAL_TRANSACTION_THREAD_ID && ownerThreadId != currentThreadId) {
        throw IllegalStateException(
            "Cannot beginTransaction() on thread '${Thread.currentThread().name}'. " +
                "A transaction (id=$externalTransactionId) is already active on a different thread (id=$ownerThreadId)."
        )
    }
    if (useLock) {
        reentrantLock.lock()
    }
    val depth = externalTransactionDepth.get() ?: 0
    if (depth == 0) {
        externalTransactionOwnerThreadId = currentThreadId
        externalTransactionId = externalTransactionIdGenerator.getAndIncrement()
    }
    externalTransactionDepth.set(depth + 1)
}

internal fun rollbackExternalTransactionBegin() {
    val depth = externalTransactionDepth.get() ?: 0
    if (depth <= 0) {
        // Nothing to rollback.
    } else if (depth == 1) {
        externalTransactionDepth.remove()
        externalTransactionOwnerThreadId = NO_EXTERNAL_TRANSACTION_THREAD_ID
        externalTransactionId = 0
    } else {
        externalTransactionDepth.set(depth - 1)
    }
    if (useLock) {
        reentrantLock.unlock()
    }
}

internal fun endExternalTransactionLockOrThrow() {
    val currentThreadId = Thread.currentThread().id
    val ownerThreadId = externalTransactionOwnerThreadId
    if (ownerThreadId == NO_EXTERNAL_TRANSACTION_THREAD_ID) {
        throw IllegalStateException("Cannot endTransaction() because no transaction is active.")
    }
    if (ownerThreadId != currentThreadId) {
        throw IllegalStateException(
            "Cannot endTransaction() on thread '${Thread.currentThread().name}'. " +
                "The transaction (id=$externalTransactionId) is owned by a different thread (id=$ownerThreadId)."
        )
    }
    val depth = externalTransactionDepth.get() ?: 0
    if (depth <= 0) {
        throw IllegalStateException("Cannot endTransaction() because this thread didn't begin a transaction.")
    }
    if (depth == 1) {
        externalTransactionDepth.remove()
        externalTransactionOwnerThreadId = NO_EXTERNAL_TRANSACTION_THREAD_ID
        externalTransactionId = 0
    } else {
        externalTransactionDepth.set(depth - 1)
    }
    if (useLock) {
        reentrantLock.unlock()
    }
}

internal fun assertExternalTransactionOwnerThreadOrThrow(operation: String) {
    val currentThreadId = Thread.currentThread().id
    val ownerThreadId = externalTransactionOwnerThreadId
    if (ownerThreadId == NO_EXTERNAL_TRANSACTION_THREAD_ID) {
        throw IllegalStateException("Cannot call $operation because no transaction is active.")
    }
    if (ownerThreadId != currentThreadId) {
        throw IllegalStateException(
            "Cannot call $operation on thread '${Thread.currentThread().name}'. " +
                "The transaction (id=$externalTransactionId) is owned by a different thread (id=$ownerThreadId)."
        )
    }
}



inline fun <T> withLockAndDbContext(crossinline block: () -> T): T {
    assertNoCrossThreadExternalTransaction()
    if(useLock){
        reentrantLock.lock()
    }

    val startTime = SystemClock.elapsedRealtime()

    val result = try {
        block()
    }finally {
        if(useLock){
            reentrantLock.unlock()
        }
    }

    if(debugMode) {
        val duration = SystemClock.elapsedRealtime() - startTime
        if (duration > durationThreshold) {
            logger("Long running operation detected: $duration")
            logger("Stack trace: ${Thread.currentThread().stackTrace.joinToString("\n")}")
        }
    }

    return result
}
