package com.mthaler.knittings.category

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.mthaler.knittings.DatasourceViewModel
import com.mthaler.knittings.utils.setMutVal

class CategoryListViewModel(application: Application) : DatasourceViewModel(application) {

    val categories = MutableLiveData(datasource.allCategories)

    override fun databaseChanged() {
        categories.setMutVal(datasource.allCategories)
    }
}