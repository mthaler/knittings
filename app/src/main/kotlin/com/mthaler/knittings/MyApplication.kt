package com.mthaler.knittings

import androidx.multidex.MultiDexApplication
import com.mthaler.dbapp.ApplicationSettings
import com.mthaler.dbapp.DatabaseApplication
import com.mthaler.dbapp.database.CategoryDataSource
import com.mthaler.dbapp.database.ObservableDatabase
import com.mthaler.dbapp.database.PhotoDataSource
import com.mthaler.dbapp.database.ProjectsDataSource
import com.mthaler.dbapp.model.ExportDatabase
import com.mthaler.dbapp.settings.Theme
import com.mthaler.knittings.database.KnittingsDataSource
import com.mthaler.knittings.model.Database
import com.mthaler.knittings.model.Knitting
import com.mthaler.knittings.model.toDatabase
import com.mthaler.knittings.settings.ThemeRepository
import org.json.JSONObject
import java.io.File

class MyApplication : MultiDexApplication(), DatabaseApplication<Knitting> {

    override fun onCreate() {
        super.onCreate()
        KnittingsDataSource.init(this)
        ObservableDatabase.init(KnittingsDataSource)
        Theme.setThemes(ThemeRepository.themes)
    }

    override val dropboxAppKey: String = "6ybf7tgqdbhf641"

    override fun getCategoryDataSource(): CategoryDataSource = KnittingsDataSource

    override fun getPhotoDataSource(): PhotoDataSource = KnittingsDataSource

    override fun getProjectsDataSource(): ProjectsDataSource<Knitting> = KnittingsDataSource

    override fun getApplicationSettings(): ApplicationSettings = object : ApplicationSettings {

        override fun getFileProviderAuthority(): String = "com.mthaler.knittings.fileprovider"

        override fun emptyCategoryListBackground(): Int = R.drawable.categories

        override fun categoryListBackground(): Int = R.drawable.categories2
    }

    override fun createExportDatabase(): ExportDatabase<Knitting> = Database.createDatabase()

    override fun createExportDatabaseFromJSON(json: JSONObject, externalFilesDir: File): ExportDatabase<Knitting> = json.toDatabase(this, externalFilesDir)
}
