package com.mthaler.knittings.service

import java.lang.Exception

sealed class JobStatus {

    object Initialized : JobStatus()
    data class Progress(val value: Int) : JobStatus()
    data class Success(val msg: String = "", val errors: List<Exception>? = null) : JobStatus()
    data class Error(val exception: Exception) : JobStatus()
    data class Cancelled(val msg: String = "", val data: Any? = null) : JobStatus()
}