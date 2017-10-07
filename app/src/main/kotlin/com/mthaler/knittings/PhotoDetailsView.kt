package com.mthaler.knittings

internal interface PhotoDetailsView {

    val photo: Photo?

    fun init(photo: Photo)

    fun deletePhoto()
}
