package com.mthaler.knittings.utils

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream

object SerializeUtils {

    fun serialize(obj: Any): ByteArray {
        ByteArrayOutputStream().use { out ->
            val dout = ObjectOutputStream(out)
            dout.writeObject(obj)
            return out.toByteArray()
        }
    }

    fun <T> deserialize(bytes: ByteArray): T {
        ByteArrayInputStream(bytes).use { i ->
            val din = ObjectInputStream(i)
            val obj = din.readObject()
            return obj as T
        }
    }
}