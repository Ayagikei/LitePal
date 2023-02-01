package org.litepal.litepalsample.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import org.litepal.LitePal
import org.litepal.litepalsample.model.Album
import org.litepal.litepalsample.model.Singer
import org.litepal.litepalsample.model.Song
import org.litepal.tablemanager.Connector
import org.litepal.tablemanager.Generator

@Database(entities = [Album::class, Singer::class, Song::class], version = 2)
//@TypeConverters(DateConverter::class)
abstract class MyDatabase : RoomDatabase() {
    //abstract fun userDao(): UserDao
}

object MyRoom {
    val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(database: SupportSQLiteDatabase) {
            // Empty implementation, because the schema isn't changing.
            // Generator.upgrade(database)
        }
    }

    fun init(context: Context) {
        val db = Room.databaseBuilder(
            context,
            MyDatabase::class.java, "database-name"
        ).addMigrations(MIGRATION_1_2).build()

        Connector.db = db.openHelper
        LitePal.initialize(context)
    }
}