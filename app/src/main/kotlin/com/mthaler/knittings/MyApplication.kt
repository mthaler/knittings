package com.mthaler.knittings

import androidx.multidex.MultiDexApplication
import com.mthaler.knittings.settings.Theme
import com.mthaler.knittings.settings.ThemeRepository

class MyApplication : MultiDexApplication() {

    override fun onCreate() {
        super.onCreate()
        Theme.setThemes(ThemeRepository.themes)
    }
}
