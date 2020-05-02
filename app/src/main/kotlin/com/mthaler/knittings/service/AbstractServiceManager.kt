package com.mthaler.knittings.service

import androidx.lifecycle.MutableLiveData
import com.mthaler.knittings.utils.setMutVal

abstract class AbstractServiceManager {

    val jobStatus: MutableLiveData<JobStatus> = MutableLiveData(JobStatus.Progress(0))

    val serviceStatus: MutableLiveData<ServiceStatus> = MutableLiveData(ServiceStatus.Stopped)

    fun updateJobStatus(jobStatus: JobStatus) {
        this.jobStatus.setMutVal(jobStatus)
    }

    fun updateServiceStatus(serviceStatus: ServiceStatus) {
        this.serviceStatus.setMutVal(serviceStatus)
    }
}