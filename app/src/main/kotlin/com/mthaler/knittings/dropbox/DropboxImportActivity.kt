package com.mthaler.knittings.dropbox

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.mthaler.knittings.R
import kotlinx.android.synthetic.main.activity_dropbox_import.*

/**
 * Activity that handles Dropbox import
 */
class DropboxImportActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dropbox_import)

        setSupportActionBar(toolbar)

        // enable up navigation
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val f = supportFragmentManager.findFragmentByTag(DropboxImportActivity.DropboxImportFragmentTag)

        // If the Fragment is non-null, then it is currently being
        // retained across a configuration change.
        if (f == null) {
            val ff = DropboxImportFragment()
            supportFragmentManager.beginTransaction().add(R.id.frament_container, ff, DropboxImportActivity.DropboxImportFragmentTag).commit()
        }
    }

    companion object {
        const val DropboxImportFragmentTag = "dropbox_import_fragment"
    }
}
