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

    private var filter: Filter = NoFilter

    val needles = MutableLiveData<List<Needle>>(emptyList())

    init {
        updateNeedles()
    }

    fun setFilter(filter: Filter) {
        this.filter = filter
        updateNeedles()
    }

    fun getFilter(): Filter = filter

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