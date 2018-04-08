package com.mthaler.knittings.dropbox

import android.os.Bundle
import com.mthaler.knittings.R
import kotlinx.android.synthetic.main.activity_dropbox_export.*
import android.support.v7.app.AppCompatActivity

/**
 * Activity that handles Dropbox export
 */
class DropboxExportActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dropbox_export)

        setSupportActionBar(toolbar)

        val f = supportFragmentManager.findFragmentByTag(DropboxExportFragmentTag)

        // If the Fragment is non-null, then it is currently being
        // retained across a configuration change.
        if (f == null) {
            val ff = DropboxExportFragment()
            supportFragmentManager.beginTransaction().add(R.id.frament_container, ff, DropboxExportFragmentTag).commit()
        }
    }

    companion object {
        val DropboxExportFragmentTag = "dropbox_export_fragment"
    }
}
