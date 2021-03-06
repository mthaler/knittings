package com.mthaler.knittings

import android.app.Application
import com.mthaler.knittings.database.*
import com.mthaler.knittings.settings.Theme
import com.mthaler.knittings.model.Database
import com.mthaler.knittings.model.ExportDatabase
import com.mthaler.knittings.model.toDatabase
import com.mthaler.knittings.settings.ThemeRepository
import com.mthaler.knittings.utils.ResourcesProvider
import org.json.JSONObject
import java.io.File

class MyApplication : Application(), DatabaseApplication {

    override fun onCreate() {
        super.onCreate()
        KnittingsDataSource.init(this)
        ResourcesProvider.init(this)
        ObservableDatabase.init(KnittingsDataSource)
        Theme.setThemes(ThemeRepository.themes)
    }

    override val dropboxAppKey: String = "6ybf7tgqdbhf641"

    override fun getApplicationSettings(): ApplicationSettings = object : ApplicationSettings {

        override fun emptyCategoryListBackground(): Int = R.drawable.categories

        override fun categoryListBackground(): Int = R.drawable.categories2
    }
}