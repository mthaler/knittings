package com.mthaler.knittings

import androidx.multidex.MultiDexApplication
import com.mthaler.dbapp.DatabaseApplication
import com.mthaler.dbapp.database.CategoryRepository
import com.mthaler.dbapp.database.ObservableDatabase
import com.mthaler.dbapp.database.PhotoRepository
import com.mthaler.dbapp.settings.Theme
import com.mthaler.knittings.database.KnittingsDataSource
import com.mthaler.knittings.settings.ThemeRepository

class MyApplication : MultiDexApplication(), DatabaseApplication {

    override fun onCreate() {
        super.onCreate()
        KnittingsDataSource.init(this)
        ObservableDatabase.init(KnittingsDataSource)
        CategoryRepository.init(KnittingsDataSource)
        PhotoRepository.init(KnittingsDataSource)
        Theme.setThemes(ThemeRepository.themes)
    }

    override fun getFileProviderAuthority(): String = "com.mthaler.knittings.fileprovider"
}
