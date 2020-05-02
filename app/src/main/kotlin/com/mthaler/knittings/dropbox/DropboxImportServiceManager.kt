package com.mthaler.knittings.dropbox

import androidx.lifecycle.MutableLiveData
import com.mthaler.knittings.service.JobStatus
import com.mthaler.knittings.utils.setMutVal

class DropboxImportServiceManager {

    val jobStatus: MutableLiveData<JobStatus> = MutableLiveData(JobStatus.Progress(0))

    fun statusUpdated(jobStatus: JobStatus) {
        this.jobStatus.setMutVal(jobStatus)
    }

    companion object {

        private var sServiceManager: DropboxImportServiceManager? = null

        @Synchronized
        fun getInstance(): DropboxImportServiceManager {
            if (sServiceManager == null) {
                sServiceManager = DropboxImportServiceManager()
            }
            return sServiceManager!!
        }
    }
}