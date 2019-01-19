package com.mthaler.knittings.model

import android.graphics.Color
import com.mthaler.knittings.database.KnittingDatabaseHelper
import com.mthaler.knittings.utils.ColorUtils
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
    result.put("duration", duration)
    if (category != null) {
        result.put("category", category.id)
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

fun Category.toJSON(): JSONObject {
    val result = JSONObject()
    result.put("id", id)
    result.put("name", name)
    if (color != null) {
        result.put("color", ColorUtils.colorToHex(color))
    }
    return result
}

/**
 * Converts a JSON object to a knitting. The method actually returns a pair of the knitting and
 * and the optional default photo id
 */
fun JSONObject.toKnitting(): Triple<Knitting, Long?, Long?> {
    val id = getLong("id")
    val title = getString("title")
    val description = getString("description")
    val started = dateFormat.parse(getString("started"))
    val finished = if (has("finished")) dateFormat.parse(getString("finished")) else null
    val needleDiameter = getString("needleDiameter")
    val size = getString("size")
    val defaultPhoto = if (has("defaultPhoto")) getLong("defaultPhoto") else null
    val rating = getDouble("rating")
    val duration = if (has("duration")) getLong("duration") else 0L
    val category = if (has("category")) getLong("category") else 0L
    return Triple(Knitting(id, title, description, started, finished, needleDiameter, size, null, rating, duration), defaultPhoto, category)
}

/**
 * Converts a JSON array to a list of knittings
 */
fun JSONArray.toKnittings(): List<Triple<Knitting, Long?, Long?>> {
    val result = ArrayList<Triple<Knitting, Long?, Long?>>()
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

/**
 * Converts a JSON object to a category
 */
fun JSONObject.toCategory(): Category {
    val id = getLong("id")
    val name = getString("name")
    val color = if (has("color")) Color.parseColor(getString("color")) else null
    return Category(id, name, color)
}

/**
 * Converts a JSON array to a list of categories
 */
fun JSONArray.toCategories(): List<Category> {
    val result = ArrayList<Category>()
    for(i in 0 until length()) {
        val item = getJSONObject(i)
        val category = item.toCategory()
        result.add(category)
    }
    return result
}

fun knittingsToJSON(knittings: List<Knitting>): JSONArray {
    val result = JSONArray()
    for (knitting in knittings) {
        result.put(knitting.toJSON())
    }
    return result
}

fun photosToJSON(photos: List<Photo>): JSONArray {
    val result = JSONArray()
    for (photo in photos) {
        result.put(photo.toJSON())
    }
    return result
}

fun categoriesToJSON(categories: List<Category>): JSONArray {
    val result = JSONArray()
    for (category in categories) {
        result.put(category.toJSON())
    }
    return result
}

/**
 * Converts a database to JSON
 */
fun Database.toJSON(): JSONObject {
    val result = JSONObject()
    result.put("version", KnittingDatabaseHelper.DB_VERSION)
    result.put("knittings", knittingsToJSON(knittings))
    result.put("photos", photosToJSON(photos))
    result.put("categories", categoriesToJSON(categories))
    return result
}

/**
 * Converts a JSON object to a database object
 */
fun JSONObject.toDatabase(externalFilesDir: File): Database {

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

    fun addCategory(knitting: Knitting, categoryID: Long?, idToCategory: Map<Long, Category>): Knitting {
        if (categoryID != null) {
            if (idToCategory.containsKey(categoryID)) {
                return knitting.copy(category = idToCategory[categoryID])
            } else {
                return knitting
            }
        } else {
            return knitting
        }
    }

    // list of knittings and optional default photo
    val knittingsAndDefaultPhotos = getJSONArray("knittings").toKnittings()
    val photos = getJSONArray("photos").toPhotos()
    val categories = if (has("categories")) getJSONArray("categories").toCategories() else ArrayList()
    // we need to update the filename of the photo because the external storage location might be different
    val photosWithUpdatedFilename = photos.map { it -> updatePhotoFilename(it) }

    // create a map from photo id to photo
    val idToPhoto = photosWithUpdatedFilename.map { it.id to it }.toMap()
    // create a map from category id to category
    val idToCategory = categories.map { it.id to it }.toMap()
    // update the list of knittings with default photos
    val knittings = knittingsAndDefaultPhotos.map { Pair(updateKnitting(it.first, it.second, idToPhoto), it.third) }.map { addCategory(it.first, it.second, idToCategory)  }
    return Database(knittings, photosWithUpdatedFilename, categories)
}
