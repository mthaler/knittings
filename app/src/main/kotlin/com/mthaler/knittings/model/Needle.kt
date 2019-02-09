package com.mthaler.knittings.model

import java.io.Serializable

data class Needle(val id: Long, val name: String, val description: String, val size: String, val length: String, val material: String, val inUse: Boolean, val type: String) : Serializable