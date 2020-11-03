package com.mthaler.knittings.model

import android.content.Context
import com.mthaler.dbapp.model.Category
import com.mthaler.dbapp.model.Photo
import com.mthaler.knittings.database.KnittingDatabaseHelper
import org.json.JSONObject
import java.text.SimpleDateFormat
import org.json.JSONArray
import java.io.File
import java.lang.Exception
import com.mthaler.dbapp.model.toCategories
import com.mthaler.dbapp.model.categoriesToJSON

private val dateFormat = SimpleDateFormat("yyyy-MM-dd")

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
    result.put("status", status)
    return result
}

fun JSONObject.toKnitting(context: Context): Triple<Knitting, Long?, Long?> {
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
    val status = if (has("status")) {
        val statusStr = getString("status")
        try {
            Status.valueOf(statusStr)
        } catch (ex: Exception) {
            Status.parse(context, statusStr)
        }
    } else {
        Status.PLANNED
    }
    return Triple(Knitting(id, title, description, started, finished, needleDiameter, size, null, rating, duration, status = status), defaultPhoto, category)
}

fun Photo.toJSON(): JSONObject {
    val result = JSONObject()
    result.put("id", id)
    result.put("filename", filename.absolutePath)
    result.put("knittingID", ownerID)
    result.put("description", description)
    return result
}

fun JSONObject.toPhoto(): Photo {
    val id = getLong("id")
    val filename = File(getString("filename"))
    val knittingID = getLong("knittingID")
    val description = getString("description")
    return Photo(id, filename, knittingID, description)
}

fun Needle.toJSON(): JSONObject {
    val result = JSONObject()
    result.put("id", id)
    result.put("name", name)
    result.put("description", description)
    result.put("size", size)
    result.put("length", length)
    result.put("material", material)
    result.put("inUse", inUse)
    result.put("type", type)
    return result
}

fun JSONObject.toNeedle(context: Context): Needle {
    val id = getLong("id")
    val name = getString("name")
    val description = getString("description")
    val size = getString("size")
    val length = getString("length")
    val material = if (has("material")) {
        val matgerialStr = getString("material")
        try {
            NeedleMaterial.valueOf(matgerialStr)
        } catch (ex: Exception) {
            NeedleMaterial.parse(context, matgerialStr)
        }
    } else NeedleMaterial.OTHER
    val inUse = getBoolean("inUse")
    val type = if (has("type")) {
        val typeStr = getString("type")
        try {
            NeedleType.valueOf(typeStr)
        } catch (ex: Exception) {
            NeedleType.parse(context, typeStr)
        }
    } else NeedleType.OTHER
    return Needle(id, name, description, size, length, material, inUse, type)
}

fun RowCounter.toJson(): JSONObject {
    val result = JSONObject()
    result.put("id", id)
    result.put("totalRows", totalRows)
    result.put("rowsPerRepeat", rowsPerRepeat)
    result.put("knittingID", knittingID)
    return result
}

fun JSONObject.toRows(): RowCounter {
    val id = getLong("id")
    val totalRows = getInt("totalRows")
    val rowsPerRepeat = getInt("rowsPerRepeat")
    val knittingID = getLong("knittingID")
    return RowCounter(id, totalRows, rowsPerRepeat, knittingID)
}

/**
 * Converts a JSON array to a list of knittings
 */
fun JSONArray.toKnittings(context: Context): List<Triple<Knitting, Long?, Long?>> {
    val result = ArrayList<Triple<Knitting, Long?, Long?>>()
    for (i in 0 until length()) {
        val item = getJSONObject(i)
        val knittingAndDefaultPhoto = item.toKnitting(context)
        result.add(knittingAndDefaultPhoto)
    }
    return result
}

/**
 * Converts a JSON array to a list of photos
 */
fun JSONArray.toPhotos(): List<Photo> {
    val result = ArrayList<Photo>()
    for (i in 0 until length()) {
        val item = getJSONObject(i)
        val photo = item.toPhoto()
        result.add(photo)
    }
    return result
}

/**
 * Converts a JSON array to a list of needles
 */
fun JSONArray.toNeedles(context: Context): List<Needle> {
    val result = ArrayList<Needle>()
    for (i in 0 until length()) {
        val item = getJSONObject(i)
        val needle = item.toNeedle(context)
        result.add(needle)
    }
    return result
}

fun JSONArray.toRows(): List<RowCounter> {
    val result = ArrayList<RowCounter>()
    for (i in 0 until length()) {
        val item = getJSONObject(i)
        val rows = item.toRows()
        result.add(rows)
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

fun needlesToJSON(needles: List<Needle>): JSONArray {
    val result = JSONArray()
    for (needle in needles) {
        result.put(needle.toJSON())
    }
    return result
}

fun rowsToJSON(rows: List<RowCounter>): JSONArray {
    val result = JSONArray()
    for (r in rows) {
        result.put(r.toJson())
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
    result.put("needles", needlesToJSON(needles))
    result.put("rows", rowsToJSON(rows))
    return result
}

/**
 * Converts a JSON object to a database object
 */
fun JSONObject.toDatabase(context: Context, externalFilesDir: File): Database {

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
    val knittingsAndDefaultPhotos = getJSONArray("knittings").toKnittings(context)
    val photos = getJSONArray("photos").toPhotos()
    val categories = if (has("categories")) getJSONArray("categories").toCategories() else ArrayList()
    val needles = if (has("needles")) getJSONArray("needles").toNeedles(context) else ArrayList()
    val rows = if(has("rows")) getJSONArray("rows").toRows() else ArrayList()
    // we need to update the filename of the photo because the external storage location might be different
    val photosWithUpdatedFilename = photos.map { updatePhotoFilename(it) }

    // create a map from photo id to photo
    val idToPhoto = photosWithUpdatedFilename.map { it.id to it }.toMap()
    // create a map from category id to category
    val idToCategory = categories.map { it.id to it }.toMap()
    // update the list of knittings with default photos
    val knittings = knittingsAndDefaultPhotos.map { Pair(updateKnitting(it.first, it.second, idToPhoto), it.third) }.map { addCategory(it.first, it.second, idToCategory) }
    return Database(knittings, photosWithUpdatedFilename, categories, needles, rows)
}
