package com.mthaler.knittings.database

import android.app.Application
import androidx.lifecycle.AndroidViewModel

abstract class DataSourceViewModel(application: Application) : AndroidViewModel(application), DatabaseObserver {

    init {
        ObservableDatabase.addObserver(this)
    }

    override fun onCleared() {
        super.onCleared()
        ObservableDatabase.removeObserver(this)
    }
}