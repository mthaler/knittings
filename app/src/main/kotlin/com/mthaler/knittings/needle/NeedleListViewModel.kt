package com.mthaler.knittings.needle

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.mthaler.dbapp.DataSourceViewModel
import com.mthaler.knittings.database.KnittingsDataSource
import com.mthaler.knittings.model.Needle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class NeedleListViewModel(application: Application) : DataSourceViewModel(application) {

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
                filter.filter(KnittingsDataSource.allNeedles)
            }
            needles.value = filteredNeedles
        }
    }
}