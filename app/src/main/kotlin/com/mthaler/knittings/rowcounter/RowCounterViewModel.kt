package com.mthaler.knittings.rowcounter

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData

class RowCounterViewModel(application: Application) : AndroidViewModel(application) {

    val rows = MutableLiveData<Int>(0)
}