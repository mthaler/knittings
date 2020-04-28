package com.mthaler.knittings.compressphotos

import androidx.lifecycle.MutableLiveData
import com.mthaler.knittings.utils.setMutVal

object ServiceManager {

    val status: MutableLiveData<Status> = MutableLiveData(Status.Progress(0))

    fun statusUpdated(status: Status) {
        this.status.setMutVal(status)
    }

}