package com.mthaler.knittings.compressphotos

import androidx.lifecycle.MutableLiveData
import com.mthaler.knittings.service.JobStatus
import com.mthaler.knittings.utils.setMutVal

class ServiceManager {

    val jobStatus: MutableLiveData<JobStatus> = MutableLiveData(JobStatus.Progress(0))

    fun statusUpdated(jobStatus: JobStatus) {
        this.jobStatus.setMutVal(jobStatus)
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