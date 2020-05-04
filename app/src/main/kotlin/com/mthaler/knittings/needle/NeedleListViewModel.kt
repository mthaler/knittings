package com.mthaler.knittings.needle

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.mthaler.knittings.DatasourceViewModel
import com.mthaler.knittings.model.Needle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class NeedleListViewModel(application: Application) : DatasourceViewModel(application) {

    val needles = MutableLiveData<List<Needle>>(emptyList())
    var filter: Filter = NoFilter
        set(value) {
            field = value
            updateNeedles()
        }

    init {
        updateNeedles()
    }

    override fun databaseChanged() {
        updateNeedles()
    }

    private fun updateNeedles() {
        viewModelScope.launch {
            val filteredNeedles = withContext(Dispatchers.IO) {
                filter.filter(datasource.allNeedles)
            }
            needles.value = filteredNeedles
        }
    }
}