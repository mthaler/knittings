package com.mthaler.knittings.database

import com.mthaler.dbapp.database.DatabaseObserver
import com.mthaler.dbapp.database.ObservableDatabase

abstract class AbstractObservableDatabase : ObservableDatabase {

    private val observers = HashSet<DatabaseObserver>()

    @Synchronized override fun addObserver(observer: DatabaseObserver) {
        observers.add(observer)
    }

    @Synchronized override fun removeObserver(observer: DatabaseObserver) {
        observers.remove(observer)
    }

    @Synchronized override fun notifyObservers() {
        observers.forEach { it.databaseChanged() }
    }
}