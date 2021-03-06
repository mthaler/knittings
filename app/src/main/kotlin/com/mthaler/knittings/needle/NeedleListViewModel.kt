package com.mthaler.knittings.needle

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.mthaler.knittings.database.DataSourceViewModel
import com.mthaler.knittings.filter.CombinedFilter
import com.mthaler.knittings.database.KnittingsDataSource
import com.mthaler.knittings.model.Needle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class NeedleListViewModel : DataSourceViewModel() {

    val _needles = MutableLiveData<List<Needle>>(emptyList())
    val needles: LiveData<List<Needle>> = _needles


    var filter: CombinedFilter<Needle> = CombinedFilter.empty()
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
            val ds = KnittingsDataSource
            val filteredNeedles = withContext(Dispatchers.IO) {
                filter.filter(ds.allNeedles)
            }
            _needles.value = filteredNeedles
        }
    }
}