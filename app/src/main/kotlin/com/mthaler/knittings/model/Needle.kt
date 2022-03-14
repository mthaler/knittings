package com.mthaler.knittings.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity
data class Needle(
    @PrimaryKey val id: Long = -1,
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