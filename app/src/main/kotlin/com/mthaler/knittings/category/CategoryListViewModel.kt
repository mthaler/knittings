package com.mthaler.knittings.category

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.mthaler.knittings.DatasourceViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CategoryListViewModel(application: Application) : DatasourceViewModel(application) {

    val categories = MutableLiveData(datasource.allCategories)

    override fun databaseChanged() {
        viewModelScope.launch {
            val allCategories = withContext(Dispatchers.IO) {
                datasource.allCategories
            }
            categories.value = allCategories
        }
    }
}