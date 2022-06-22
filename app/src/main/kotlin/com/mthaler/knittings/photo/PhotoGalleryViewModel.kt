package com.mthaler.knittings.photo

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.mthaler.knittings.database.DataSourceViewModel
import com.mthaler.knittings.database.KnittingsDataSource
import com.mthaler.knittings.database.PhotoDataSource
import com.mthaler.knittings.model.Photo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PhotoGalleryViewModel : DataSourceViewModel() {

    private val ds = KnittingsDataSource as PhotoDataSource
    private var ownerID = -1L
    val photos = MutableLiveData<List<Photo>>()

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