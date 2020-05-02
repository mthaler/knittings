package com.mthaler.knittings.compressphotos

import androidx.lifecycle.MutableLiveData
import com.mthaler.knittings.service.Status
import com.mthaler.knittings.utils.setMutVal

class ServiceManager {

    val status: MutableLiveData<Status> = MutableLiveData(Status.Progress(0))

    fun statusUpdated(status: Status) {
        this.status.setMutVal(status)
    }

    companion object {

        private var sServiceManager: ServiceManager? = null

        @Synchronized
        fun getInstance(): ServiceManager {
            if (sServiceManager == null) {
                sServiceManager = ServiceManager()
            }
            return sServiceManager!!
        }
    }
}