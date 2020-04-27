package com.mthaler.knittings.details

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.mthaler.knittings.DatasourceViewModel
import com.mthaler.knittings.model.Knitting
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class KnittingDetailsViewModel(application: Application) : DatasourceViewModel(application) {

    private var knittingID = Knitting.EMPTY.id
    private var deleted = false
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

    fun delete() {
        deleted = true
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val k = datasource.getKnitting(knittingID)
                datasource.deleteKnitting(k)
            }
        }
    }

    override fun databaseChanged() {
        if (!deleted) {
            viewModelScope.launch {
                val knittingWithPhotos =  withContext(Dispatchers.IO) {
                    val k = datasource.getKnitting(knittingID)
                    val photos = datasource.getAllPhotos(k)
                    KnittingWithPhotos(k, photos)
                }
                knitting.value = knittingWithPhotos
            }
        }
    }
}