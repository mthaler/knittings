package com.mthaler.knittings.settings

import android.os.Bundle
import androidx.preference.*
import com.mthaler.knittings.R

class SettingsFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.app_preferences)
    }
}