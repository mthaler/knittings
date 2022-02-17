package com.mthaler.knittings

interface ApplicationSettings {

    fun getFileProviderAuthority(): String

    fun emptyCategoryListBackground(): Int

    fun categoryListBackground(): Int
}