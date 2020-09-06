package com.mthaler.knittings.photo

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.mthaler.dbapp.model.Photo
import com.mthaler.knittings.DatasourceViewModel
import com.mthaler.knittings.database.KnittingsDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PhotoGalleryViewModel(application: Application) : DatasourceViewModel(application) {

    private var knittingID = -1L
    val photos = MutableLiveData<ArrayList<Photo>>()

    fun init(id: Long) {
        if (id != knittingID) {
            knittingID = id
            viewModelScope.launch {
                val ps = withContext(Dispatchers.IO) {
                    val k = KnittingsDataSource.getKnitting(id)
                    KnittingsDataSource.getAllPhotos(k.id)
                }
                photos.value = ps
            }
        }
    }

    override fun databaseChanged() {
        viewModelScope.launch {
            val ps = withContext(Dispatchers.IO) {
                val k = KnittingsDataSource.getKnitting(knittingID)
                KnittingsDataSource.getAllPhotos(k.id)
            }
            photos.value = ps
        }
    }
}