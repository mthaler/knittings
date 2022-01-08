package com.mthaler.knittings.model

import java.util.*

interface Project {

    val id: Long

    val title: String

    val description: String

    val started: Date

    val finished: Date?

    val defaultPhoto: Photo?

    val category: Category?
}