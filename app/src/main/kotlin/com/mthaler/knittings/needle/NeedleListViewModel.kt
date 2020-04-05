package com.mthaler.knittings.needle

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.mthaler.knittings.DatasourceViewModel
import com.mthaler.knittings.utils.setMutVal

class NeedleListViewModel(application: Application) : DatasourceViewModel(application) {

    val needles = MutableLiveData(datasource.allNeedles)

    override fun databaseChanged() {
        needles.setMutVal(datasource.allNeedles)
    }
}