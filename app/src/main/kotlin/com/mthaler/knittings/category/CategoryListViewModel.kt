package com.mthaler.knittings.category

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.mthaler.knittings.database.DataSourceViewModel
import com.mthaler.knittings.DatabaseApplication
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CategoryListViewModel(application: Application) : DataSourceViewModel(application) {

    private val ds = (application as DatabaseApplication<*>).getCategoryDataSource()

    val categories = MutableLiveData(ds.allCategories.sortedBy { it.name.toLowerCase() })

    override fun databaseChanged() {
        viewModelScope.launch {
            val allCategories = withContext(Dispatchers.IO) {
                ds.allCategories.sortedBy { it.name.toLowerCase() }
            }
            categories.value = allCategories
        }
    }
}