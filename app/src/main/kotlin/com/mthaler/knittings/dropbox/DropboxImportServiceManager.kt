package com.mthaler.knittings.dropbox

import com.mthaler.dbapp.service.AbstractServiceManager

class DropboxImportServiceManager : AbstractServiceManager() {

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