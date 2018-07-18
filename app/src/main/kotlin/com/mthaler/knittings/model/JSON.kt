package com.mthaler.knittings.model

import org.json.JSONObject
import java.text.SimpleDateFormat
import org.json.JSONArray

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
    return result
}

fun Photo.toJSON(): JSONObject {
    val result = JSONObject()
    result.put("id", id)
    result.put("filename", filename.absolutePath)
    result.put("knittingID", knittingID)
    result.put("description", description)
    return result
}

fun JSONObject.toKnitting(): Knitting {
    val id = getLong("id")
    val title = getString("title")
    val description = getString("description")
    val started = dateFormat.parse(getString("started"))
    val finished = if (has("finished")) dateFormat.parse(getString("finished")) else null
    val needleDiameter = getDouble("needleDiameter")
    val size = getDouble("size")
    val defaultPhoto = if (has("defaultPhoto")) getLong("defaultPhoto") else null
    val rating = getDouble("rating")
    return Knitting(id, title, description, started, finished, needleDiameter, size, null, rating)
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

fun dbToJSON(knittings: List<Knitting>, photos: List<Photo>): JSONObject {
    val result = JSONObject()
    result.put("knittings", knittingsToJSON(knittings))
    result.put("photos", photosToJSON(photos))
    return result
}
