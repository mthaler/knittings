package com.mthaler.knittings.utils

import android.os.Looper
import androidx.lifecycle.MutableLiveData

private fun isMainThread(): Boolean {
    return Looper.myLooper() == Looper.getMainLooper()
}

fun <T> MutableLiveData<T>.setMutVal(value: T) {
    if (isMainThread()) setValue(value) else postValue(value)
}