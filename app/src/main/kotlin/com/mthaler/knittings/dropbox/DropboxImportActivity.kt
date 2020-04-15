package com.mthaler.knittings.dropbox

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.mthaler.knittings.BaseActivity
import com.mthaler.knittings.R
import kotlinx.android.synthetic.main.activity_dropbox_import.*

/**
 * Activity that handles Dropbox import
 */
class DropboxImportActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dropbox_import)

        setSupportActionBar(toolbar)

        // enable up navigation
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val f = supportFragmentManager.findFragmentByTag(DropboxImportFragmentTag)

        // If the Fragment is non-null, then it is currently being
        // retained across a configuration change.
        if (f == null) {
            val ff = DropboxImportFragment()
            supportFragmentManager.beginTransaction().add(R.id.frament_container, ff, DropboxImportFragmentTag).commit()
        }
    }

    companion object {
        const val DropboxImportFragmentTag = "dropbox_import_fragment"

        fun newIntent(context: Context): Intent = Intent(context, DropboxImportActivity::class.java)
    }
}
