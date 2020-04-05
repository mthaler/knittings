package com.mthaler.knittings.category

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.mthaler.knittings.database.DatabaseObserver
import com.mthaler.knittings.database.KnittingsDataSource
import com.mthaler.knittings.utils.setMutVal

class CategoryListViewModel(application: Application): AndroidViewModel(application), DatabaseObserver {

    private val datasource = KnittingsDataSource.getInstance(application.applicationContext)
    val categories = MutableLiveData(datasource.allCategories)

    init {
        datasource.addObserver(this)
    }

    override fun databaseChanged() {
        categories.setMutVal(datasource.allCategories)
    }

    override fun onCleared() {
        super.onCleared()
        datasource.removeObserver(this)
    }
}