package com.mthaler.knittings.database

import androidx.lifecycle.ViewModel

abstract class DataSourceViewModel : ViewModel(), DatabaseObserver {

    init {
        ObservableDatabase.addObserver(this)
    }

    override fun onCleared() {
        super.onCleared()
        ObservableDatabase.removeObserver(this)
    }
}