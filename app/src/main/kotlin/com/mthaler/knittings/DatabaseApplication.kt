package com.mthaler.knittings

import com.mthaler.knittings.database.CategoryDataSource
import com.mthaler.knittings.database.PhotoDataSource
import com.mthaler.knittings.database.ProjectsDataSource
import com.mthaler.knittings.model.ExportDatabase
import com.mthaler.knittings.model.Project
import org.json.JSONObject
import java.io.File

interface DatabaseApplication<T : Project> {

    val dropboxAppKey: String

    fun getCategoryDataSource(): CategoryDataSource

    fun getPhotoDataSource(): PhotoDataSource

    fun getProjectsDataSource(): ProjectsDataSource<T>

    fun getApplicationSettings(): ApplicationSettings

    fun createExportDatabase(): ExportDatabase<T>

    fun createExportDatabaseFromJSON(json: JSONObject, externalFilesDir: File): ExportDatabase<T>
}