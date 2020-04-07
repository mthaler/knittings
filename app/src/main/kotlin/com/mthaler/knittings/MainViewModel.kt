package com.mthaler.knittings

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.mthaler.knittings.utils.setMutVal

class MainViewModel(application: Application) : DatasourceViewModel(application) {

    val knittings = MutableLiveData(datasource.allKnittings)

    override fun databaseChanged() {
        knittings.setMutVal(datasource.allKnittings)
    }
}