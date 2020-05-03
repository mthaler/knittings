package com.mthaler.knittings.service

import java.lang.Exception

sealed class JobStatus {

    object Initialized : JobStatus()
    data class Progress(val value: Int) : JobStatus()
    data class Success(val msg: String = "") : JobStatus()
    data class Error(val exception: Exception) : JobStatus()
    data class Cancelled(val msg: String = "") : JobStatus()
}