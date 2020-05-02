package com.mthaler.knittings.compressphotos

import com.mthaler.knittings.service.AbstractServiceManager

class ServiceManager : AbstractServiceManager() {

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