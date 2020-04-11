package com.mthaler.knittings.photo

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.mthaler.knittings.DatasourceViewModel
import com.mthaler.knittings.model.Photo
import com.mthaler.knittings.utils.setMutVal

class PhotoViewModel(application: Application) : DatasourceViewModel(application) {

    private var photoID = -1L
    val photos = MutableLiveData<Photo>()

    fun init(id: Long) {
        if (id != photoID) {
            photoID = id
            val p = datasource.getPhoto(id)
            photos.setMutVal(p)
        }
    }

    override fun databaseChanged() {
        val p = datasource.getPhoto(photoID)
        photos.setMutVal(p)
    }
}