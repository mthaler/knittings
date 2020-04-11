package com.mthaler.knittings.details

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.mthaler.knittings.DatasourceViewModel
import com.mthaler.knittings.model.Knitting
import com.mthaler.knittings.utils.setMutVal

class EditKnittingDetailsViewModel(application: Application) : DatasourceViewModel(application) {

    private var knittingID = -1L
    val knitting = MutableLiveData<Knitting>()

    fun init(id: Long) {
        if (id != knittingID) {
            knittingID = id
            val n = datasource.getKnitting(id)
            knitting.setMutVal(n)
        }
    }

    fun saveKnitting(knitting: Knitting) {
        if (knitting.id == -1L) {
            datasource.addKnitting(knitting)
        } else {
            val k = datasource.getKnitting(knittingID)
            datasource.updateKnitting(knitting.copy(defaultPhoto = k.defaultPhoto))
        }
    }

    override fun databaseChanged() {
        val k = datasource.getKnitting(knittingID)
        knitting.setMutVal(k)
    }
}