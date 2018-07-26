package com.mthaler.knittings.dropbox

import android.os.Bundle
import com.mthaler.knittings.R
import kotlinx.android.synthetic.main.activity_dropbox_export.*
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem

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

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.dropbox_export, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_item_dropbox_logout -> {
                DropboxClientFactory.getClient().auth().tokenRevoke()
                val prefs = getSharedPreferences(AbstractDropboxFragment.SharedPreferencesName, AppCompatActivity.MODE_PRIVATE)
                prefs.getString("access-token", null)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    companion object {
        val DropboxExportFragmentTag = "dropbox_export_fragment"
    }
}
