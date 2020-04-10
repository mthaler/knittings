package com.mthaler.knittings.photo

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.mthaler.knittings.DatasourceViewModel
import com.mthaler.knittings.model.Photo
import com.mthaler.knittings.utils.setMutVal

class PhotoGalleryViewModel(application: Application) : DatasourceViewModel(application) {

    private var knittingID = -1L
    val photos = MutableLiveData<ArrayList<Photo>>()

    fun init(id: Long) {
        if (id != knittingID) {
            knittingID = id
            val k = datasource.getKnitting(id)
            val ps = datasource.getAllPhotos(k)
            photos.setMutVal(ps)
        }
    }

    override fun databaseChanged() {
        val k = datasource.getKnitting(knittingID)
        val ps = datasource.getAllPhotos(k)
        photos.setMutVal(ps)
    }
}