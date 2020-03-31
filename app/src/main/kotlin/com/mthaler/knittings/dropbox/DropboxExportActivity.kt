package com.mthaler.knittings.dropbox

import android.os.Bundle
import com.mthaler.knittings.R
import kotlinx.android.synthetic.main.activity_dropbox_export.*
import androidx.appcompat.app.AppCompatActivity
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

    /**
     * Initialize the contents of the Activity's standard options menu.
     * This is only called once, the first time the options menu is displayed.
     *
     * @param menu The options menu in which you place your items.
     * @return you must return true for the menu to be displayed; if you return false it will not be shown.
     */
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.dropbox_export, menu)
        return true
    }

    /**
     * This hook is called whenever an item in your options menu is selected.
     *
     * @param item the menu item that was selected.
     * @return return false to allow normal menu processing to proceed, true to consume it here.
     */
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
                        // delete auth token from shared pref_sharing
                        val prefs = getSharedPreferences(AbstractDropboxFragment.SharedPreferencesName, MODE_PRIVATE)
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
