package com.mthaler.knittings.database

abstract class AbstractObservableDatabase : ObservableDatabaseOps {

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