package com.mthaler.knittings.photo

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.mthaler.dbapp.model.Photo
import com.mthaler.dbapp.DataSourceViewModel
import com.mthaler.dbapp.database.PhotoRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PhotoGalleryViewModel(application: Application) : DataSourceViewModel(application) {

    private var knittingID = -1L
    val photos = MutableLiveData<ArrayList<Photo>>()

    fun init(id: Long) {
        if (id != knittingID) {
            knittingID = id
            viewModelScope.launch {
                val ps = withContext(Dispatchers.IO) {
                    PhotoRepository.getAllPhotos(knittingID)
                }
                photos.value = ps
            }
        }
    }

    override fun databaseChanged() {
        viewModelScope.launch {
            val ps = withContext(Dispatchers.IO) {
                PhotoRepository.getAllPhotos(knittingID)
            }
            photos.value = ps
        }
    }
}