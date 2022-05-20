package com.mthaler.knittings.details

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.mthaler.knittings.database.DataSourceViewModel
import com.mthaler.knittings.database.KnittingsDataSource
import com.mthaler.knittings.model.Knitting
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class KnittingDetailsViewModel : DataSourceViewModel() {

    private var knittingID = Knitting.EMPTY.id
    private var deleted = false
    val _knitting = MutableLiveData<Knitting>()
    val knitting: LiveData<Knitting> = _knitting

    fun init(id: Long) {
        if (id != knittingID) {
            knittingID = id
            viewModelScope.launch {
                val k =  withContext(Dispatchers.IO) {
                    KnittingsDataSource.getProject(id)
                }
                _knitting.value = k
            }
        }
    }

    fun delete() {
        deleted = true
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val k = KnittingsDataSource.getProject(knittingID)
                KnittingsDataSource.deleteProject(k)
            }
        }
    }

    override fun databaseChanged() {
        if (!deleted) {
            viewModelScope.launch {
                val k =  withContext(Dispatchers.IO) {
                    KnittingsDataSource.getProject(knittingID)
                }
                _knitting.value = k
            }
        }
    }
}