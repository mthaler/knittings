package com.mthaler.knittings

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.mthaler.knittings.model.Knitting
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainViewModel(application: Application) : DatasourceViewModel(application) {

    val knittings = MutableLiveData<List<Knitting>>(emptyList())
    var sorting: Sorting = Sorting.NewestFirst
        set(value) {
            field = value
            updateKnittings()
        }
    var filter: CombinedFilter = CombinedFilter.Empty
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
                val allKnittings = datasource.allKnittings
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