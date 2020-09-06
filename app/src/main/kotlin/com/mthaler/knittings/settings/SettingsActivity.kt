package com.mthaler.knittings.settings

import android.content.Context
import android.content.Intent
import com.mthaler.knittings.R

class SettingsActivity : AbstractSettingsActivity() {

    override fun viewID(): Int = R.id.settings_container

    companion object {

        fun newIntent(context: Context): Intent = Intent(context, SettingsActivity::class.java)
    }
}
