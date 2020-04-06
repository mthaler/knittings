package com.mthaler.knittings.stopwatch

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.mthaler.knittings.DatasourceViewModel
import com.mthaler.knittings.model.Knitting
import com.mthaler.knittings.utils.setMutVal

class StopWatchViewModel(application: Application) : DatasourceViewModel(application) {

    private var knittingID: Long = -1
    val knitting = MutableLiveData<Knitting>()

    fun init(id: Long) {
        if (id != knittingID) {
            knittingID = id
            val k = datasource.getKnitting(id)
            knitting.setMutVal(k)
        }
    }

    fun updateDuration(duration: Long) {
        val knitting = datasource.getKnitting(knittingID)
        datasource.updateKnitting(knitting.copy(duration = duration))
    }

    override fun databaseChanged() {
        val k = datasource.getKnitting(knittingID)
        knitting.setMutVal(k)
    }
}