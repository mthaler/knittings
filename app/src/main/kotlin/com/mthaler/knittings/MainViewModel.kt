package com.mthaler.knittings

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.mthaler.dbapp.DataSourceViewModel
import com.mthaler.dbapp.DatabaseApplication
import com.mthaler.dbapp.Sorting
import com.mthaler.dbapp.filter.CombinedFilter
import com.mthaler.dbapp.model.Project
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainViewModel<T : Project>(application: Application) : DataSourceViewModel(application) {

    private val ds = (application as DatabaseApplication<T>).getProjectsDataSource()

    val projects = MutableLiveData<List<T>>(emptyList())
    var sorting: Sorting = Sorting.NewestFirst
        set(value) {
            field = value
            updateProjects()
        }
    var filter: CombinedFilter<T> = CombinedFilter.empty()
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
                    Sorting.Alphabetical -> filtered.sortedBy { it.title.toLowerCase() }
                }
                sorted
            }
            projects.value = filtered
        }
    }

    companion object {
        fun <T : Project>javaClass(): Class<MainViewModel<T>> = MainViewModel::class.java as Class<MainViewModel<T>>
    }
}