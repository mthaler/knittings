package com.mthaler.knittings.model

import java.io.Serializable

data class Needle(
    val id: Long = -1,
    val name: String = "",
    val description: String = "",
    val size: String = "",
    val length: String = "",
    val material: NeedleMaterial = NeedleMaterial.OTHER,
    val inUse: Boolean = false,
    val type: NeedleType = NeedleType.OTHER
) : Serializable {

    companion object {

        val EMPTY = Needle()
    }
}