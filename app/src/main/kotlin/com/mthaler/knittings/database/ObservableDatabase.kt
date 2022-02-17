package com.mthaler.knittings.database

object ObservableDatabase {

    private lateinit var impl: ObservableDatabaseOps

    fun init(impl: ObservableDatabaseOps) {
        ObservableDatabase.impl = impl
    }

    fun addObserver(observer: DatabaseObserver) = impl.addObserver(observer)

    fun removeObserver(observer: DatabaseObserver) = impl.removeObserver(observer)
}