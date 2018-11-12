package com.mthaler.knittings.dropbox

import android.os.Bundle
import com.mthaler.knittings.R
import kotlinx.android.synthetic.main.activity_dropbox_export.*
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import org.jetbrains.anko.alert
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread

/**
 * Activity that handles Dropbox export
 */
class DropboxExportActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dropbox_export)

        setSupportActionBar(toolbar)

        // enable up navigation
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

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
                doAsync {
                    try {
                        // remove auth token from Dropbox server
                        DropboxClientFactory.getClient().auth().tokenRevoke()
                    } catch (ex: Exception) {
                    }
                    uiThread {
                        // delete auth token from shared preferences
                        val prefs = getSharedPreferences(AbstractDropboxFragment.SharedPreferencesName, AppCompatActivity.MODE_PRIVATE)
                        val editor = prefs.edit()
                        editor.remove("access-token")
                        editor.commit()
                        // clear client so that it is not reused next time we connect to Dropbox
                        DropboxClientFactory.clearClient()
                        // replace the Dropbox export fragment with a new one
                        val ft = supportFragmentManager.beginTransaction()
                        val f = DropboxExportFragment()
                        ft.replace(R.id.frament_container, f)
                        ft.addToBackStack(null)
                        ft.commit()
                        alert {
                            title = resources.getString(R.string.dropbox_export)
                            message = "Logged out of Dropbox"
                            positiveButton("OK") {}
                        }.show()
                    }
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    companion object {
        const val DropboxExportFragmentTag = "dropbox_export_fragment"
    }
}
