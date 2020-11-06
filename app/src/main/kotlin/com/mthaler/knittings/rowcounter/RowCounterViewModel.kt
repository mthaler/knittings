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
    val rowCounter = MutableLiveData<RowCounter>()

    fun init(id: Long) {
        if (id != knittingID) {
            knittingID = id
            viewModelScope.launch {
                val r =  withContext(Dispatchers.IO) {
                    val knitting = KnittingsDataSource.getProject(id)
                    KnittingsDataSource.getRowCounter(knitting)
                }
                if (r != null) {
                    rowCounter.value = r
                } else {
                    val rr = KnittingsDataSource.addRowCounter(RowCounter(-1, 0, 1, knittingID))
                    rowCounter.value = rr
                }
            }
        }
    }

    fun incrementTotalRows() {
        val r0 = rowCounter.value
        if (r0 != null) {
            val r1 = r0.copy(totalRows = r0.totalRows + 1)
            rowCounter.value = r1
            KnittingsDataSource.updateRowCounter(r1)
        }
    }

    fun decrementTotalRows() {
        val r0 = rowCounter.value
        if (r0 != null && r0.totalRows > 0) {
            val r1 = r0.copy(totalRows = r0.totalRows -1)
            rowCounter.value = r1
            KnittingsDataSource.updateRowCounter(r1)
        }
    }

    fun clearTotalRows() {
        val r0 = rowCounter.value
        if (r0 != null) {
            val r1 = r0.copy(totalRows = 0)
            rowCounter.value = r1
            KnittingsDataSource.updateRowCounter(r1)
        }
    }

    fun setRowsPerRepeat(rowsPerRepeat: Int) {
        val r = rowCounter.value
        if (r != null && r.rowsPerRepeat != rowsPerRepeat) {
            KnittingsDataSource.updateRowCounter(r.copy(rowsPerRepeat = rowsPerRepeat))
        }
    }

    override fun databaseChanged() {
        viewModelScope.launch {
            val r =  withContext(Dispatchers.IO) {
                val k = KnittingsDataSource.getProject(knittingID)
                KnittingsDataSource.getRowCounter(k)
            }
            rowCounter.value = r
        }
    }
}