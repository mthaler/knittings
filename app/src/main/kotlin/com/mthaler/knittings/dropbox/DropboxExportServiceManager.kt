package com.mthaler.knittings.dropbox

import androidx.lifecycle.MutableLiveData
import com.mthaler.knittings.service.JobStatus
import com.mthaler.knittings.utils.setMutVal

class DropboxExportServiceManager {

    val jobStatus: MutableLiveData<JobStatus> = MutableLiveData(JobStatus.Progress(0))

    fun statusUpdated(jobStatus: JobStatus) {
        this.jobStatus.setMutVal(jobStatus)
    }

    companion object {

        private var sServiceManager: DropboxExportServiceManager? = null

        @Synchronized
        fun getInstance(): DropboxExportServiceManager {
            if (sServiceManager == null) {
                sServiceManager = DropboxExportServiceManager()
            }
            return sServiceManager!!
        }
    }
}