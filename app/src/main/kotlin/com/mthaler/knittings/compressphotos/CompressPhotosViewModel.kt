package com.mthaler.knittings.compressphotos

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.mthaler.knittings.DatasourceViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CompressPhotosViewModel(application: Application) : DatasourceViewModel(application) {

    val photoCount = MutableLiveData(0)

    init {
        updatePhotoCount()
    }

    override fun databaseChanged() {
        updatePhotoCount()
    }

    private fun updatePhotoCount() {
        viewModelScope.launch {
            val allPhotos = withContext(Dispatchers.IO) {
                datasource.allPhotos
            }
            photoCount.value = allPhotos.size
        }
    }
}