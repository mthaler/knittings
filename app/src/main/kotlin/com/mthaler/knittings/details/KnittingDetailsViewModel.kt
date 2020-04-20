package com.mthaler.knittings.details

import android.app.Application
import android.database.CursorIndexOutOfBoundsException
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.mthaler.knittings.DatasourceViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class KnittingDetailsViewModel(application: Application) : DatasourceViewModel(application) {

    private var knittingID = -1L
    val knitting = MutableLiveData<KnittingWithPhotos>()

    fun init(id: Long) {
        if (id != knittingID) {
            knittingID = id
            viewModelScope.launch {
                val knittingWithPhotos =  withContext(Dispatchers.IO) {
                    val k = datasource.getKnitting(id)
                    val photos = datasource.getAllPhotos(k)
                    KnittingWithPhotos(k, photos)
                }
                knitting.value = knittingWithPhotos
            }
        }
    }

    override fun databaseChanged() {
        try {
            viewModelScope.launch {
                val knittingWithPhotos =  withContext(Dispatchers.IO) {
                    val k = datasource.getKnitting(knittingID)
                    val photos = datasource.getAllPhotos(k)
                    KnittingWithPhotos(k, photos)
                }
                knitting.value = knittingWithPhotos
            }
        } catch(ex: CursorIndexOutOfBoundsException) {
            // We get an exception when a knitting is deleted from the details fragment, ignore for now
        }
    }
}