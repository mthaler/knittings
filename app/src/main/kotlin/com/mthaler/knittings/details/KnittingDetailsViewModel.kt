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
    val knitting = MutableLiveData<Knitting>()

    fun init(id: Long) {
        if (id != knittingID) {
            knittingID = id
            viewModelScope.launch {
                val k =  withContext(Dispatchers.IO) {
                    datasource.getKnitting(id)
                }
                knitting.value = k
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
                val k =  withContext(Dispatchers.IO) {
                    datasource.getKnitting(knittingID)
                }
                knitting.value = k
            }
        }
    }
}