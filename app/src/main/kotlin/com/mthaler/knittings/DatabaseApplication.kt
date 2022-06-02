package com.mthaler.knittings

import com.mthaler.knittings.database.CategoryDataSource
import com.mthaler.knittings.database.PhotoDataSource
import com.mthaler.knittings.database.ProjectsDataSource
import com.mthaler.knittings.model.ExportDatabase
import com.mthaler.knittings.model.Knitting
import org.json.JSONObject
import java.io.File

interface DatabaseApplication {

    val dropboxAppKey: String

    fun getCategoryDataSource(): CategoryDataSource

    fun getPhotoDataSource(): PhotoDataSource

    fun getProjectsDataSource(): ProjectsDataSource

    fun getApplicationSettings(): ApplicationSettings

    fun createExportDatabase(): ExportDatabase

    fun createExportDatabaseFromJSON(json: JSONObject, externalFilesDir: File): ExportDatabase
}