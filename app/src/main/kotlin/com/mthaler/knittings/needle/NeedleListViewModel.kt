package com.mthaler.knittings.needle

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.mthaler.knittings.DatasourceViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class NeedleListViewModel(application: Application) : DatasourceViewModel(application) {

    val needles = MutableLiveData(datasource.allNeedles)

    override fun databaseChanged() {
        viewModelScope.launch {
            val allNeedles = withContext(Dispatchers.IO) {
                datasource.allNeedles
            }
            needles.value = allNeedles
        }
    }
}