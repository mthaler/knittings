package com.mthaler.knittings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.mthaler.dbapp.database.DatabaseObserver
import com.mthaler.dbapp.database.ObservableDatabase

abstract class DatasourceViewModel(application: Application) : AndroidViewModel(application), DatabaseObserver {

    init {
        ObservableDatabase.addObserver(this)
    }

    override fun onCleared() {
        super.onCleared()
        ObservableDatabase.removeObserver(this)
    }
}