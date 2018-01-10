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
    } else {
        result.put("finished", JSONObject.NULL)
    }
    result.put("needleDiameter", needleDiameter)
    result.put("size", size)
    result.put("rating", rating)
    if (defaultPhoto != null) {
        result.put("defaultPhoto", defaultPhoto.toJSON())
    } else {
        result.put("defaultPhoto", JSONObject.NULL)
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