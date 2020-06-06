package com.mthaler.knittings.rowcounter

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.mthaler.knittings.DatasourceViewModel
import com.mthaler.knittings.model.Knitting
import com.mthaler.knittings.model.Rows
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RowCounterViewModel(application: Application) : DatasourceViewModel(application) {

    private var knittingID = Knitting.EMPTY.id
    val rows = MutableLiveData<Rows>()

    fun init(id: Long) {
        if (id != knittingID) {
            knittingID = id
            viewModelScope.launch {
                val r =  withContext(Dispatchers.IO) {
                    val knitting = datasource.getKnitting(id)
                    datasource.getRows(knitting)
                }
                if (r != null) {
                    rows.value = r
                } else {
                    val rr = datasource.addRows(Rows(-1, 0, knittingID))
                    rows.value = rr
                }
            }
        }
    }

    fun incrementTotalRows() {
        val r = rows.value
        if (r != null) {
            datasource.updateRows(r.copy(totalRows = r.totalRows + 1))
        }
    }

    fun decrementTotalRows() {
        val r = rows.value
        if (r != null) {
            datasource.updateRows(r.copy(totalRows = r.totalRows - 1))
        }
    }

    override fun databaseChanged() {
        viewModelScope.launch {
            val r =  withContext(Dispatchers.IO) {
                val k = datasource.getKnitting(knittingID)
                datasource.getRows(k)
            }
            rows.value = r
        }
    }
}