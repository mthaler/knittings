package com.mthaler.knittings.settings

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.mthaler.knittings.R
import com.mthaler.knittings.databinding.ActivitySettingsBinding

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        // enable up navigation
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        if (savedInstanceState == null) {
            val preferenceFragment = SettingsFragment()
            val ft = supportFragmentManager.beginTransaction()
            ft.add(R.id.settings_container, preferenceFragment)
            ft.commit()
        }
    }

    companion object {

        fun newIntent(context: Context): Intent = Intent(context, SettingsActivity::class.java)
    }
}
