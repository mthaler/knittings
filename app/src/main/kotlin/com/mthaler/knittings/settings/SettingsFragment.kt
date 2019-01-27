package com.mthaler.knittings.settings

import android.os.Bundle
import android.support.v7.preference.PreferenceFragmentCompat
import com.mthaler.knittings.R

class SettingsFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(bundle: Bundle, s: String) {
        // Load the Preferences from the XML file
        addPreferencesFromResource(R.xml.app_preferences)
    }

}