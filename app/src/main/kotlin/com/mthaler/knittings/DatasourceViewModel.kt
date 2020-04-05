package com.mthaler.knittings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.mthaler.knittings.database.DatabaseObserver
import com.mthaler.knittings.database.KnittingsDataSource

abstract class DatasourceViewModel(application: Application) : AndroidViewModel(application), DatabaseObserver {

    protected val datasource = KnittingsDataSource.getInstance(application.applicationContext)

    init {
        datasource.addObserver(this)
    }

    override fun onCleared() {
        super.onCleared()
        datasource.removeObserver(this)
    }
}