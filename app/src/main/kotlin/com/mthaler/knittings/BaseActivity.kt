package com.mthaler.knittings

import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager

open class BaseActivity : AppCompatActivity() {

    private lateinit var sharedPref: SharedPreferences
    private lateinit var currentTheme: com.mthaler.knittings.settings.Theme

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sharedPref = PreferenceManager.getDefaultSharedPreferences(this)
        val name = sharedPref.getString("theme", "default")!!
        currentTheme = com.mthaler.knittings.settings.Theme.getTheme(name)
        setAppTheme(currentTheme)
    }

    override fun onResume() {
        super.onResume()
        val name = sharedPref.getString("theme", "default")
        if (currentTheme.name != name)
            recreate()
    }

    private fun setAppTheme(theme: com.mthaler.knittings.settings.Theme) {
        setTheme(theme.themeId)
    }
}