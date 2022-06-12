package com.mthaler.knittings.service

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.mthaler.knittings.utils.setMutVal
import java.util.concurrent.atomic.AtomicBoolean

abstract class AbstractServiceManager {

    private val _jobStatus: MutableLiveData<JobStatus> = MutableLiveData(JobStatus.Initialized)
    private val _serviceStatus: MutableLiveData<ServiceStatus> = MutableLiveData(ServiceStatus.Stopped)
    private val _cancelled = AtomicBoolean(false)

    val jobStatus: LiveData<JobStatus>
        @Synchronized
        get() = _jobStatus

    val serviceStatus: LiveData<ServiceStatus>
        @Synchronized
        get() = _serviceStatus

    @Synchronized
    fun updateJobStatus(jobStatus: JobStatus) {
        this._jobStatus.setMutVal(jobStatus)
    }

    @Synchronized
    fun updateServiceStatus(serviceStatus: ServiceStatus) {
        if (serviceStatus == ServiceStatus.Started) {
            _cancelled.set(false)
        }
        this._serviceStatus.setMutVal(serviceStatus)
    }

    var cancelled: Boolean
        @Synchronized
        get() = _cancelled.get()
        @Synchronized
        set(value) = _cancelled.set(value)
}