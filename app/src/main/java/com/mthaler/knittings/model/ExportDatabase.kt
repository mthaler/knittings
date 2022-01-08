package com.mthaler.knittings.model

import android.os.Parcelable
import com.dropbox.core.v2.DbxClientV2
import org.json.JSONObject
import java.io.Serializable

interface ExportDatabase<T : Project>  : Serializable, Parcelable {

    val projects: List<T>

    val photos: List<Photo>

    val categories: List<Category>

    fun checkDatabase(): ExportDatabase<T>

    fun checkValidity()

    fun removeMissingPhotos(missingPhotos: Set<Long>): ExportDatabase<T>

    fun write(dbxClient: DbxClientV2, directory: String, photoDownloaded: (Int) -> Unit)

    fun toJSON(): JSONObject
}