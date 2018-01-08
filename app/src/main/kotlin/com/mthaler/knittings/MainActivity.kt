package com.mthaler.knittings

import android.app.AlertDialog
import android.content.Intent
import android.support.design.widget.FloatingActionButton
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import android.widget.Toast
import com.dropbox.core.v2.users.FullAccount
import com.mthaler.knittings.database.datasource
import com.mthaler.knittings.dropbox.DropboxClient
import com.mthaler.knittings.dropbox.LoginActivity
import com.mthaler.knittings.dropbox.UploadTask
import com.mthaler.knittings.dropbox.UserAccountTask
import org.jetbrains.anko.*

/**
 * The main activity that gets displayed when the app is started.
 * It displays a list of knittings. The user can add new knittings or
 * edit existing ones
 */
class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private var ACCESS_TOKEN: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // add toolbar to activity
        val toolbar = find<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        // set on click handler of floating action button that creates a new knitting
        val fab = find<FloatingActionButton>(R.id.fab_create_add_knitting)
        fab.setOnClickListener {
            val knittingListView = supportFragmentManager.findFragmentById(R.id.fragment_knitting_list) as KnittingListView
            knittingListView.addKnitting()
        }

        val drawer_layout = findViewById<DrawerLayout>(R.id.drawer_layout)
        val toggle = ActionBarDrawerToggle(
                this, drawer_layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()

        val nav_view = findViewById<NavigationView>(R.id.nav_view)
        nav_view.setNavigationItemSelectedListener(this)

        if (!tokenExists()) {
            val intent = Intent(this@MainActivity, LoginActivity::class.java)
            startActivity(intent)
        }

        ACCESS_TOKEN = retrieveAccessToken();
        getUserAccount();
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.knittings_list, menu)
        return true
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_item_about -> {
                showAboutDialog()
                return true
            }
            R.id.menu_item_export_to_dropbox -> {
                val photos = datasource.allPhotos
                if (!photos.isEmpty()) {
                    val photo = photos[0]
                    val file = photo.filename
                    UploadTask(DropboxClient.getClient(ACCESS_TOKEN), file, applicationContext).execute()
                }
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        when (item.itemId) {
            R.id.nav_camera -> {
                // Handle the camera action
            }
            R.id.nav_gallery -> {

            }
            R.id.nav_slideshow -> {

            }
            R.id.nav_manage -> {

            }
            R.id.nav_share -> {

            }
            R.id.nav_send -> {

            }
        }

        val drawer_layout = findViewById<DrawerLayout>(R.id.drawer_layout)
        drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }

    /**
     * Shows the about dialog that displays the app name, version and some additional information
     */
    private fun showAboutDialog() {
        val v = layoutInflater.inflate(R.layout.dialog_about, null)
        val appName = v.find<TextView>(R.id.about_app_name)
        appName.text = (appName.text.toString() + " " + BuildConfig.VERSION_NAME)
        val b = AlertDialog.Builder(this)
        b.setView(v)
        b.setPositiveButton(android.R.string.ok ) { diag, i -> diag.dismiss()}
        b.create().show()
    }

    private fun tokenExists(): Boolean {
        val prefs = getSharedPreferences("com.mthaler.knittings", MODE_PRIVATE)
        val accessToken = prefs.getString("access-token", null)
        return accessToken != null
    }

    private fun retrieveAccessToken(): String? {
        //check if ACCESS_TOKEN is previously stored on previous app launches
        val prefs = getSharedPreferences("com.mthaler.knittings", MODE_PRIVATE)
        val accessToken = prefs.getString("access-token", null)
        if (accessToken == null) {
            Log.d("AccessToken Status", "No token found")
            return null
        } else {
            //accessToken already exists
            Log.d("AccessToken Status", "Token exists")
            return accessToken
        }
    }

    protected fun getUserAccount() {
        if (ACCESS_TOKEN == null) return
        UserAccountTask(DropboxClient.getClient(ACCESS_TOKEN), object : UserAccountTask.TaskDelegate {
            override fun onAccountReceived(account: FullAccount) {
                Log.d("User data", account.email)
                Log.d("User data", account.name.displayName)
                Log.d("User data", account.accountType.name)
                updateUI(account)
            }

            override fun onError(error: Exception) {
                Log.d("User data", "Error receiving account details.")
            }
        }).execute()
    }

    private fun updateUI(account: FullAccount) {
        Toast.makeText(this, "Sucessfully logged in: " + account.email, Toast.LENGTH_SHORT).show();
    }
}
