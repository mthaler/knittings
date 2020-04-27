package com.mthaler.knittings.database

import com.mthaler.knittings.model.Knitting
import com.mthaler.knittings.model.Photo
import java.io.File
import java.util.ArrayList

interface PhotoRepository {

    /**
     * Returns all knittings from the database
     *
     * @return all knittings from database
     */
    val allPhotos: ArrayList<Photo>

    fun getPhotoFile(knitting: Knitting): File?

    /**
     * Gets the photo with the given id from the database
     *
     * @param id id of the photo that should be read from database
     * @return photo for the given id
     */
    fun getPhoto(id: Long): Photo

    /**
     * Get all photos for the given knitting
     *
     * @param knitting knitting to get photos for
     * @return list of photos for the given knitting
     */
    fun getAllPhotos(knitting: Knitting): ArrayList<Photo>

    /**
     * Adds the given photo to the database
     *
     * @param photo photo that should be added to the database
     * @param manualID: use photo ID instead of auto-imcremented id
     */
    fun addPhoto(photo: Photo, manualID: Boolean = false): Photo

    /**
     * Updates a photo in the database
     *
     * @param photo photo that should be updated
     * @return updated photo
     */
    fun updatePhoto(photo: Photo): Photo

    /**
     * Deletes the given photo from the database. The photo file is also deleted.
     *
     * @param photo photo that should be deleted
     */
    fun deletePhoto(photo: Photo)

    /**
     * Deletes all photos from the database. Photo files are also deleted.
     */
    fun deleteAllPhotos()
}