package com.mthaler.knittings.filter

import java.io.Serializable

interface Filter<T> : Serializable {

    fun filter(items: List<T>): List<T>
}