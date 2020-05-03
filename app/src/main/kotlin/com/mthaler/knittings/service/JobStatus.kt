package com.mthaler.knittings.service

import java.lang.Exception

sealed class JobStatus {

    object Initialized : JobStatus()
    data class Success(val result: String = "") : JobStatus()
    data class Error(val exception: Exception) : JobStatus()
    data class Progress(val value: Int) : JobStatus()
}