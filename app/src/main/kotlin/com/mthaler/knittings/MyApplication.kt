package com.mthaler.knittings

import androidx.multidex.MultiDexApplication
import com.mthaler.dbapp.settings.Theme
import com.mthaler.knittings.database.KnittingsDataSource
import com.mthaler.knittings.settings.ThemeRepository

class MyApplication : MultiDexApplication() {

    override fun onCreate() {
        super.onCreate()
        KnittingsDataSource.init(this)
        Theme.setThemes(ThemeRepository.themes)
    }
}
