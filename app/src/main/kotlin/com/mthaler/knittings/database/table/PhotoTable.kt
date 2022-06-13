package com.mthaler.knittings.database.table

object PhotoTable {

    const val PHOTOS = "photos"

    object Cols {
        val ID = "_id"
        val FILENAME = "filename"
        val PREVIEW = "preview"
        val DESCRIPTION = "description"
        val KNITTING_ID = "knitting_id"
    }
}