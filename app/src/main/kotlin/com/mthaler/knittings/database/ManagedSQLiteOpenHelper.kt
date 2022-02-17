package com.mthaler.knittings.database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.util.concurrent.atomic.AtomicInteger

abstract class ManagedSQLiteOpenHelper(
        ctx: Context,
        name: String?,
        factory: SQLiteDatabase.CursorFactory? = null,
        version: Int = 1
): SQLiteOpenHelper(ctx, name, factory, version) {

    private val counter = AtomicInteger()
    private var db: SQLiteDatabase? = null

    fun <T> use(f: SQLiteDatabase.() -> T): T {
        try {
            return openDatabase().f()
        } finally {
            closeDatabase()
        }
    }

    @Synchronized
    private fun openDatabase(): SQLiteDatabase {
        if (counter.incrementAndGet() == 1) {
            db = writableDatabase
        }
        return db!!
    }

    @Synchronized
    private fun closeDatabase() {
        if (counter.decrementAndGet() == 0) {
            db?.close()
        }
    }
}