package com.mthaler.knittings.database

interface ObservableDatabaseOps {

    fun addObserver(observer: DatabaseObserver)

    fun removeObserver(observer: DatabaseObserver)

    fun notifyObservers()
}