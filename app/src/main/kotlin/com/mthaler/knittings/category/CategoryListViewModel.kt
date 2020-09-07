package com.mthaler.knittings.category

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.mthaler.dbapp.database.CategoryRepository
import com.mthaler.dbapp.DataSourceViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CategoryListViewModel(application: Application) : DataSourceViewModel(application) {

    val categories = MutableLiveData(CategoryRepository.allCategories)

    override fun databaseChanged() {
        viewModelScope.launch {
            val allCategories = withContext(Dispatchers.IO) {
                CategoryRepository.allCategories
            }
            categories.value = allCategories
        }
    }
}