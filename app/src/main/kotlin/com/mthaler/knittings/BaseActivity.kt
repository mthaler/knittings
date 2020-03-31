package com.mthaler.knittings

import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceManager
import androidx.appcompat.app.AppCompatActivity
import com.mthaler.knittings.settings.MINT_THEME

open class BaseActivity : AppCompatActivity() {

    private lateinit var currentTheme: String
    private lateinit var sharedPref: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sharedPref = PreferenceManager.getDefaultSharedPreferences(this)
        currentTheme = sharedPref.getString("current_theme", "lilac")
        setAppTheme(currentTheme)
    }

    override fun onResume() {
        super.onResume()
        val theme = sharedPref.getString("current_theme", "lilac")
        if(currentTheme != theme)
            recreate()
    }

    private fun setAppTheme(currentTheme: String) {
        when (currentTheme) {
            MINT_THEME -> setTheme(R.style.Theme_App_Mint)
            else -> setTheme(R.style.AppTheme)
        }
    }
}