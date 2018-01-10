package com.mthaler.knittings.model

import org.json.JSONObject
import java.text.SimpleDateFormat
import android.util.Base64

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