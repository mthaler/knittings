package com.mthaler.knittings.compressphotos

import java.lang.Exception

sealed class Status {

    object Success : Status()
    data class Error(val exception: Exception) : Status()
    data class Progress(val value: Int) : Status()
}