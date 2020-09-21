package com.mthaler.knittings

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.mthaler.dbapp.DataSourceViewModel
import com.mthaler.dbapp.Sorting
import com.mthaler.dbapp.filter.CombinedFilter
import com.mthaler.knittings.database.KnittingsDataSource
import com.mthaler.knittings.model.Knitting
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainViewModel(application: Application) : DataSourceViewModel(application) {

    val knittings = MutableLiveData<List<Knitting>>(emptyList())
    var sorting: Sorting = Sorting.NewestFirst
        set(value) {
            field = value
            updateKnittings()
        }
    var filter: CombinedFilter<Knitting> = CombinedFilter.empty()
        set(value) {
            field = value
            updateKnittings()
        }

    init {
        updateKnittings()
    }

    override fun databaseChanged() {
        updateKnittings()
    }

    private fun updateKnittings() {
        viewModelScope.launch {
            val filtered = withContext(Dispatchers.IO) {
                val allKnittings = KnittingsDataSource.allKnittings
                val filtered = filter.filter(allKnittings)
                val sorted = when (sorting) {
                    Sorting.NewestFirst -> filtered.sortedByDescending { it.started }
                    Sorting.OldestFirst -> filtered.sortedBy { it.started }
                    Sorting.Alphabetical -> filtered.sortedBy { it.title.toLowerCase() }
                }
                sorted
            }
            knittings.value = filtered
        }
    }
}