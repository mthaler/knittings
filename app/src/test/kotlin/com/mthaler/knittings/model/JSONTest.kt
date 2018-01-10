package com.mthaler.knittings.model

import org.junit.Assert.*
import org.junit.Test
import java.util.*

class JSONTest {

    @Test
    fun testToJSON() {
        val started = Date()
        val knitting = Knitting(42, "knitting", "my first knitting", started, null, 3.0, 42.0, null, 5.0)
        val json = knitting.toJSON()
        println(json)
    }
}