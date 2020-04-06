package com.mthaler.knittings.needle

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.mthaler.knittings.DatasourceViewModel
import com.mthaler.knittings.model.Needle
import com.mthaler.knittings.utils.setMutVal

class EditNeedleViewModel(application: Application) : DatasourceViewModel(application) {

    private var needleID = -1L
    val needle = MutableLiveData<Needle>()

    fun init(id: Long) {
        if (id != needleID) {
            needleID = id
            val n = datasource.getNeedle(id)
            needle.setMutVal(n)
        }
    }

    override fun databaseChanged() {
        val c = datasource.getNeedle(needleID)
        needle.setMutVal(c)
    }
}