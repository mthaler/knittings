package com.mthaler.knittings.dropbox

import androidx.lifecycle.MutableLiveData
import com.mthaler.knittings.service.Status
import com.mthaler.knittings.utils.setMutVal

class DropboxImportServiceManager {

    val status: MutableLiveData<Status> = MutableLiveData(Status.Progress(0))

    fun statusUpdated(status: Status) {
        this.status.setMutVal(status)
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