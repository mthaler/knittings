package com.mthaler.knittings.settings

import android.os.Bundle
import com.mthaler.knittings.BaseActivity
import com.mthaler.knittings.R
import kotlinx.android.synthetic.main.activity_settings.*

abstract class AbstractSettingsActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        setSupportActionBar(toolbar)

        // enable up navigation
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        if (savedInstanceState == null) {
            val preferenceFragment = SettingsFragment()
            val ft = supportFragmentManager.beginTransaction()
            ft.add(viewID(), preferenceFragment)
            ft.commit()
        }
    }

    abstract protected fun viewID(): Int
}
