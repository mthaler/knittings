package com.mthaler.knittings

import com.mthaler.knittings.model.ExportDatabase
import org.json.JSONObject
import java.io.File

interface DatabaseApplication {

    val dropboxAppKey: String

    fun getApplicationSettings(): ApplicationSettings
}