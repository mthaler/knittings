package com.mthaler.knittings.model

import org.json.JSONObject

fun Knitting.toJSON(): JSONObject {
    val result = JSONObject()
    result.put("id", id)
    result.put("title", title)
    result.put("description", description)
    result.put("started", started.time)
    result.put("needleDiameter", needleDiameter)
    result.put("size", size)
    result.put("rating", rating)
    return result
}