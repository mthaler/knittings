package com.mthaler.knittings.photo

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.mthaler.knittings.DatabaseApplication
import com.mthaler.knittings.database.DataSourceViewModel
import com.mthaler.knittings.database.PhotoDataSource
import com.mthaler.knittings.model.Photo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PhotoGalleryViewModel(application: Application) : DataSourceViewModel(application) {

    private val ds: PhotoDataSource = (application as DatabaseApplication).getPhotoDataSource()
    private var ownerID = -1L
    val photos = MutableLiveData<ArrayList<Photo>>()

    fun init(id: Long) {
        if (id != ownerID) {
            ownerID = id
            viewModelScope.launch {
                val ps = withContext(Dispatchers.IO) {
                    ds.getAllPhotos(ownerID)
                }
                photos.value = ps
            }
        }
    }

    override fun databaseChanged() {
        viewModelScope.launch {
            val ps = withContext(Dispatchers.IO) {
                ds.getAllPhotos(ownerID)
            }
            photos.value = ps
        }
    }
}