package com.mthaler.knittings.details

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.mthaler.knittings.DatasourceViewModel
import com.mthaler.knittings.utils.setMutVal

class KnittingDetailsViewModel(application: Application) : DatasourceViewModel(application) {

    private var knittingID = -1L
    val knitting = MutableLiveData<KnittingWithPhotos>()

    fun init(id: Long) {
        if (id != knittingID) {
            knittingID = id
            val k = datasource.getKnitting(id)
            val photos = datasource.getAllPhotos(k)
            val knittingWithPhotos = KnittingWithPhotos(k, photos)
            knitting.setMutVal(knittingWithPhotos)
        }
    }

    override fun databaseChanged() {
        val k = datasource.getKnitting(knittingID)
        val photos = datasource.getAllPhotos(k)
        val knittingWithPhotos = KnittingWithPhotos(k, photos)
        knitting.setMutVal(knittingWithPhotos)
    }
}