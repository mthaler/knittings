package com.mthaler.knittings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.mthaler.dbapp.database.DatabaseObserver
import com.mthaler.knittings.database.KnittingsDataSource

abstract class DatasourceViewModel(application: Application) : AndroidViewModel(application), DatabaseObserver {

    init {
        KnittingsDataSource.addObserver(this)
    }

    override fun onCleared() {
        super.onCleared()
        KnittingsDataSource.removeObserver(this)
    }
}