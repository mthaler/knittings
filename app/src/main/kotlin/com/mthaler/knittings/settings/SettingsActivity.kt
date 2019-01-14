package com.mthaler.knittings.settings

import android.os.Bundle
import android.preference.PreferenceFragment
import android.support.v7.app.AppCompatActivity
import com.mthaler.knittings.R

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if ( fragmentManager.findFragmentById(android.R.id.content) == null) {
            fragmentManager.beginTransaction()
                    .add(android.R.id.content, SettingsFragment()).commit()
        }
    }


    class SettingsFragment : PreferenceFragment() {
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            addPreferencesFromResource(R.xml.preferences)
        }
    }
}