package org.litepal

import android.os.SystemClock
import kotlinx.coroutines.sync.Mutex
import org.litepal.LitePalContext.debugMode
import org.litepal.LitePalContext.durationThreshold
import org.litepal.LitePalContext.logger
import org.litepal.LitePalContext.useLock
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



inline fun <T> withLockAndDbContext(crossinline block: () -> T): T {
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