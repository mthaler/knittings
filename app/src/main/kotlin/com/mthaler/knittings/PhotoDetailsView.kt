package com.mthaler.knittings

import com.mthaler.knittings.model.Photo

internal interface PhotoDetailsView {

    val photo: Photo?

    fun init(photo: Photo)

    fun deletePhoto()
}
