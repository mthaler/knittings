package com.mthaler.knittings

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainViewModel(application: Application) : DatasourceViewModel(application) {

    val knittings = MutableLiveData(datasource.allKnittings)

    override fun databaseChanged() {
        viewModelScope.launch {
            val allKnittings = withContext(Dispatchers.IO) {
                datasource.allKnittings
            }
            knittings.value = allKnittings
        }
    }
}