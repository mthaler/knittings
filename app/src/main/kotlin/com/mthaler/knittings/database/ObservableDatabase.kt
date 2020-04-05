package com.mthaler.knittings.database

interface ObservableDatabase {

    fun addObserver(observer: DatabaseObserver)

    fun removeObserver(observer: DatabaseObserver)

    fun notifyObservers()
}