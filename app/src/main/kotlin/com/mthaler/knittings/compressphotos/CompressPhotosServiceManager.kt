package com.mthaler.knittings.compressphotos

import com.mthaler.knittings.service.AbstractServiceManager

class CompressPhotosServiceManager : AbstractServiceManager() {

    companion object {

        private var sServiceManager: CompressPhotosServiceManager? = null

        @Synchronized
        fun getInstance(): CompressPhotosServiceManager {
            if (sServiceManager == null) {
                sServiceManager = CompressPhotosServiceManager()
            }
            return sServiceManager!!
        }
    }
}