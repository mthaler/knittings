package com.mthaler.knittings.model

import android.content.Context
import android.os.Environment
import org.json.JSONObject
import java.text.SimpleDateFormat
import org.json.JSONArray
import java.io.File

private val dateFormat = SimpleDateFormat("yyyy-MM-dd")

/**
 * Converts a knitting to a JSON object
 */
fun Knitting.toJSON(): JSONObject {
    val result = JSONObject()
    result.put("id", id)
    result.put("title", title)
    result.put("description", description)
    result.put("started", dateFormat.format(started))
    if (finished != null) {
        result.put("finished", dateFormat.format(finished))
    }
    result.put("needleDiameter", needleDiameter)
    result.put("size", size)
    result.put("rating", rating)
    if (defaultPhoto != null) {
        result.put("defaultPhoto", defaultPhoto.id)
    }
    return result
}

/**
 * Converts a photo to a JSON object
 */
fun Photo.toJSON(): JSONObject {
    val result = JSONObject()
    result.put("id", id)
    result.put("filename", filename.absolutePath)
    result.put("knittingID", knittingID)
    result.put("description", description)
    return result
}

/**
 * Converts a JSON object to a knitting. The method actually returns a pair of the knitting and
 * and the optional default photo id
 */
fun JSONObject.toKnitting(): Pair<Knitting, Long?> {
    val id = getLong("id")
    val title = getString("title")
    val description = getString("description")
    val started = dateFormat.parse(getString("started"))
    val finished = if (has("finished")) dateFormat.parse(getString("finished")) else null
    val needleDiameter = getDouble("needleDiameter")
    val size = getDouble("size")
    val defaultPhoto = if (has("defaultPhoto")) getLong("defaultPhoto") else null
    val rating = getDouble("rating")
    return Pair(Knitting(id, title, description, started, finished, needleDiameter, size, null, rating), defaultPhoto)
}

/**
 * Converts a JSON array to a list of knittings
 */
fun JSONArray.toKnittings(): List<Pair<Knitting, Long?>> {
    val result = ArrayList<Pair<Knitting, Long?>>()
    for(i in 0 until length()) {
        val item = getJSONObject(i)
        val knittingAndDefaultPhoto = item.toKnitting()
        result.add(knittingAndDefaultPhoto)
    }
    return result
}

/**
 * Converts a JSON object to a photo
 */
fun JSONObject.toPhoto(): Photo {
    val id = getLong("id")
    val filename = File(getString("filename"))
    val knittingID = getLong("knittingID")
    val description = getString("description")
    return Photo(id, filename, knittingID, description)
}

/**
 * Converts a JSON array to a list of photos
 */
fun JSONArray.toPhotos(): List<Photo> {
    val result = ArrayList<Photo>()
    for(i in 0 until length()) {
        val item = getJSONObject(i)
        val photo = item.toPhoto()
        result.add(photo)
    }
    return result
}

fun knittingsToJSON(knittings: List<Knitting>): JSONArray {
    val result = JSONArray()
    for(knitting in knittings) {
        result.put(knitting.toJSON())
    }
    return result
}

fun photosToJSON(photos: List<Photo>): JSONArray {
    val result = JSONArray()
    for(photo in photos) {
        result.put(photo.toJSON())
    }
    return result
}

/**
 * Converts a database to JSON
 */
fun Database.toJSON(): JSONObject {
    val result = JSONObject()
    result.put("knittings", knittingsToJSON(knittings))
    result.put("photos", photosToJSON(photos))
    return result
}

/**
 * Converts a JSON object to a database object
 */
fun JSONObject.toDatabase(context: Context): Database {

    val externalFilesDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)

    fun updateKnitting(knitting: Knitting, defaultPhotoID: Long?, idToPhoto: Map<Long, Photo>): Knitting {
        if (defaultPhotoID != null) {
            if (idToPhoto.containsKey(defaultPhotoID)) {
                return knitting.copy(defaultPhoto = idToPhoto[defaultPhotoID])
            } else {
                return knitting
            }
        } else {
            return knitting
        }
    }
    
    fun updatePhotoFilename(photo: Photo): Photo {
        val oldFilename = photo.filename
        val newFilename = File(externalFilesDir, oldFilename.name)
        return photo.copy(filename = newFilename)
    }

    // list of knittings and optional default photo
    val knittingsAndDefaultPhotos = getJSONArray("knittings").toKnittings()
    val photos = getJSONArray("photos").toPhotos()
    // we need to update the filename of the photo because the external storage location might be different
    val photosWithUpdatedFilename = photos.map { it -> updatePhotoFilename(it) }

    // create a map from photo id to photo
    val idToPhoto = photosWithUpdatedFilename.map { it.id to it }.toMap()
    // update the list of knittings with default photos
    val knittings = knittingsAndDefaultPhotos.map { updateKnitting(it.first, it.second, idToPhoto) }
    return Database(knittings, photosWithUpdatedFilename)
}
