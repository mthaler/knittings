package com.mthaler.knittings.rowcounter

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.mthaler.dbapp.DataSourceViewModel
import com.mthaler.knittings.database.KnittingsDataSource
import com.mthaler.knittings.model.Knitting
import com.mthaler.knittings.model.RowCounter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RowCounterViewModel(application: Application) : DataSourceViewModel(application) {

    private var knittingID = Knitting.EMPTY.id
    val rows = MutableLiveData<RowCounter>()

    fun init(id: Long) {
        if (id != knittingID) {
            knittingID = id
            viewModelScope.launch {
                val r =  withContext(Dispatchers.IO) {
                    val knitting = KnittingsDataSource.getProject(id)
                    KnittingsDataSource.getRows(knitting)
                }
                if (r != null) {
                    rows.value = r
                } else {
                    val rr = KnittingsDataSource.addRows(RowCounter(-1, 0, 1, knittingID))
                    rows.value = rr
                }
            }
        }
    }

    fun incrementTotalRows() {
        val r = rows.value
        if (r != null) {
            KnittingsDataSource.updateRows(r.copy(totalRows = r.totalRows + 1))
        }
    }

    fun decrementTotalRows() {
        val r = rows.value
        if (r != null && r.totalRows > 0) {
            KnittingsDataSource.updateRows(r.copy(totalRows = r.totalRows - 1))
        }
    }

    fun clearTotalRows() {
        val r = rows.value
        if (r != null) {
            KnittingsDataSource.updateRows(r.copy(totalRows = 0))
        }
    }

    fun setRowsPerRepeat(rowsPerRepeat: Int) {
        val r = rows.value
        if (r != null && r.rowsPerRepeat != rowsPerRepeat) {
            KnittingsDataSource.updateRows(r.copy(rowsPerRepeat = rowsPerRepeat))
        }
    }

    override fun databaseChanged() {
        viewModelScope.launch {
            val r =  withContext(Dispatchers.IO) {
                val k = KnittingsDataSource.getProject(knittingID)
                KnittingsDataSource.getRows(k)
            }
            rows.value = r
        }
    }
}