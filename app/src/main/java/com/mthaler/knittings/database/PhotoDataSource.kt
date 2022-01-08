package com.mthaler.knittings.database

import com.mthaler.knittings.model.Photo
import java.util.ArrayList

interface PhotoDataSource {

    val allPhotos: ArrayList<Photo>

    fun getPhoto(id: Long): Photo

    fun addPhoto(photo: Photo, manualID: Boolean = false): Photo

    fun updatePhoto(photo: Photo): Photo

    fun deletePhoto(photo: Photo)

    fun deleteAllPhotos()

    fun getAllPhotos(id: Long): ArrayList<Photo>

    fun setDefaultPhoto(ownerID: Long, photo: Photo)
}