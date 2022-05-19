package com.mthaler.knittings

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.mthaler.knittings.database.DataSourceViewModel
import com.mthaler.knittings.filter.CombinedFilter
import com.mthaler.knittings.model.Knitting
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainViewModel(application: Application) : DataSourceViewModel() {

    private val ds = (application as DatabaseApplication).getProjectsDataSource()

    val projects = MutableLiveData<List<Knitting>>(null)
    var sorting: Sorting = Sorting.NewestFirst
        set(value) {
            field = value
            updateProjects()
        }
    var filter: CombinedFilter<Knitting> = CombinedFilter.empty()
        set(value) {
            field = value
            updateProjects()
        }

    init {
        updateProjects()
    }

    override fun databaseChanged() {
        updateProjects()
    }

    private fun updateProjects() {
        viewModelScope.launch {
            val filtered = withContext(Dispatchers.IO) {
                val allProjects = ds.allProjects
                val filtered = filter.filter(allProjects)
                val sorted = when (sorting) {
                    Sorting.NewestFirst -> filtered.sortedByDescending { it.started }
                    Sorting.OldestFirst -> filtered.sortedBy { it.started }
                    Sorting.Alphabetical -> filtered.sortedBy { it.title.lowercase() }
                }
                sorted
            }
            projects.value = filtered
        }
    }
}