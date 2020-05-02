package com.mthaler.knittings.model

import java.io.Serializable

data class Database(val knittings: List<Knitting>, val photos: List<Photo>, val categories: List<Category>, val needles: List<Needle>) : Serializable