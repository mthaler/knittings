package com.mthaler.knittings

import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceManager
import androidx.appcompat.app.AppCompatActivity
import com.mthaler.knittings.settings.Theme

open class BaseActivity : AppCompatActivity() {

    private lateinit var sharedPref: SharedPreferences
    private lateinit var currentTheme: Theme

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sharedPref = PreferenceManager.getDefaultSharedPreferences(this)
        val name = sharedPref.getString("theme", "default")
        currentTheme = Theme.getTheme(name)
        setAppTheme(currentTheme)
    }

    override fun onResume() {
        super.onResume()
        val name = sharedPref.getString("theme", "default")
        if (currentTheme.name != name)
            recreate()
    }

    private fun setAppTheme(theme: Theme) {
        setTheme(theme.themeId)
    }
}