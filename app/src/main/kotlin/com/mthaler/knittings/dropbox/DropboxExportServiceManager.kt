package com.mthaler.knittings.dropbox

import com.mthaler.dbapp.service.AbstractServiceManager

class DropboxExportServiceManager : AbstractServiceManager() {

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