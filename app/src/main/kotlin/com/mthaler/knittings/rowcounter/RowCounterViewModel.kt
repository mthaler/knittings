package com.mthaler.knittings.rowcounter

import android.app.Application
import androidx.lifecycle.AndroidViewModel
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
                val k =  withContext(Dispatchers.IO) {
                    datasource.getKnitting(id)
                }
                // knitting.value = k
            }
        }
    }

    fun incrementTotalRows() {
//        val k = knitting.value
//        if (k != null) {
//            datasource.updateKnitting(k.copy(totalRows = k.totalRows + 1))
//        }
    }

    fun decrementTotalRows() {
//        val k = knitting.value
//        if (k != null) {
//            datasource.updateKnitting(k.copy(totalRows = k.totalRows - 1))
//        }
    }

    override fun databaseChanged() {
//        viewModelScope.launch {
//            val k =  withContext(Dispatchers.IO) {
//                datasource.getKnitting(knittingID)
//            }
//            knitting.value = k
//        }
    }
}