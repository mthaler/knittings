package com.mthaler.knittings

import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceManager
import androidx.appcompat.app.AppCompatActivity

open class BaseActivity : AppCompatActivity() {

    private lateinit var sharedPref: SharedPreferences
    private var useMintTheme: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sharedPref = PreferenceManager.getDefaultSharedPreferences(this)
        useMintTheme = sharedPref.getBoolean(resources.getString(R.string.key_mint_theme), false)
        setAppTheme()
    }

    override fun onResume() {
        super.onResume()
        val useMint = sharedPref.getBoolean(resources.getString(R.string.key_mint_theme), false)
        if (useMintTheme != useMint)
            recreate()
    }

    private fun setAppTheme() {
        val useMint = sharedPref.getBoolean(resources.getString(R.string.key_mint_theme), false)
        if (useMint) {
            setTheme(R.style.Theme_App_Mint)
        } else {
            setTheme(R.style.AppTheme_NoActionBar)
        }
    }
}