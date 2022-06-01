package com.mthaler.knittings.database

import com.mthaler.knittings.model.Photo
import java.util.ArrayList

interface PhotoDataSource {

    val allPhotos: List<Photo>

    fun getPhoto(id: Long): Photo

    fun addPhoto(photo: Photo): Photo

    fun updatePhoto(photo: Photo): Photo

    fun deletePhoto(photo: Photo)

    fun deleteAllPhotos()

    fun getAllPhotos(id: Long): List<Photo>

    fun setDefaultPhoto(ownerID: Long, photo: Photo)
}