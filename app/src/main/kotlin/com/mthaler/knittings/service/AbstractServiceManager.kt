package com.mthaler.knittings.service

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.mthaler.knittings.utils.setMutVal
import java.util.concurrent.atomic.AtomicBoolean

abstract class AbstractServiceManager {

    private val _jobStatus: MutableLiveData<JobStatus> = MutableLiveData(JobStatus.Progress(0))
    private val _serviceStatus: MutableLiveData<ServiceStatus> = MutableLiveData(ServiceStatus.Stopped)
    private val _canceled = AtomicBoolean(false)

    val jobStatus: LiveData<JobStatus>
        get() = _jobStatus

    val serviceStatus: LiveData<ServiceStatus>
        get() = _serviceStatus

    fun updateJobStatus(jobStatus: JobStatus) {
        this._jobStatus.setMutVal(jobStatus)
    }

    fun updateServiceStatus(serviceStatus: ServiceStatus) {
        if (serviceStatus == ServiceStatus.Started) {
            _canceled.set(false)
        }
        this._serviceStatus.setMutVal(serviceStatus)
    }

    var canceled: Boolean
        get() = _canceled.get()
        set(value) = _canceled.set(value)
}