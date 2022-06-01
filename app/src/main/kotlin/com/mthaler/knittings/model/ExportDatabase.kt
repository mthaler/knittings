package com.mthaler.knittings.model

import android.content.Context
import android.os.Parcelable
import com.dropbox.core.v2.DbxClientV2
import org.json.JSONObject
import java.io.Serializable

interface ExportDatabase  : Serializable {

    val projects: List<Knitting>

    val photos: List<Photo>

    val categories: List<Category>

    fun checkDatabase(): ExportDatabase

    fun checkValidity()

    fun removeMissingPhotos(missingPhotos: Set<Long>): ExportDatabase

    fun write(context: Context, dbxClient: DbxClientV2, directory: String, photoDownloaded: (Int) -> Unit)

    fun toJSON(): JSONObject
}