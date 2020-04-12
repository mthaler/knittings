package com.mthaler.knittings.photo

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.core.app.NavUtils
import android.view.MenuItem
import com.mthaler.knittings.BaseActivity
import com.mthaler.knittings.R
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.error
import com.mthaler.knittings.Extras.EXTRA_KNITTING_ID
import kotlinx.android.synthetic.main.activity_photo_gallery.*

class PhotoGalleryActivity : BaseActivity(), AnkoLogger {

    private var knittingID: Long = -1

    /**
     * Called when the activity is starting. This is where most initialization should go: calling setContentView(int)
     * to inflate the activity's UI, using findViewById(int) to programmatically interact with widgets in the UI,
     * calling managedQuery(android.net.Uri, String[], String, String[], String) to retrieve cursors for data being displayed, etc.
     *
     * @param savedInstanceState Bundle: If the activity is being re-initialized after previously being shut down then this Bundle contains
     *                           the data it most recently supplied in onSaveInstanceState(Bundle). Note: Otherwise it is null.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_photo_gallery)

        setSupportActionBar(toolbar)

        // enable up navigation
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // get the id of the knitting that should be displayed.
        val id = if (savedInstanceState != null) savedInstanceState.getLong(EXTRA_KNITTING_ID) else intent.getLongExtra(EXTRA_KNITTING_ID, -1L)
        if (id != -1L) {
            knittingID = id
        } else {
            error("Could not get knitting id")
        }

        if (savedInstanceState == null) {
            val f = PhotoGalleryFragment.newInstance(knittingID)
            val fm = supportFragmentManager
            val ft = fm.beginTransaction()
            ft.add(R.id.photo_gallery_container, f)
            ft.commit()
        }
    }

    /**
     * Called to ask the fragment to save its current dynamic state, so it can later be reconstructed in a
     * new instance of its process is restarted. If a new instance of the fragment later needs to be created,
     * the data you place in the Bundle here will be available in the Bundle given to onCreate(Bundle),
     * onCreateView(LayoutInflater, ViewGroup, Bundle), and onActivityCreated(Bundle).
     *
     * @param outState If the fragment is being re-created from a previous saved state, this is the state.
     */
    override fun onSaveInstanceState(outState: Bundle) {
        outState.putLong(EXTRA_KNITTING_ID, knittingID)
        super.onSaveInstanceState(outState)
    }

    /**
     * This hook is called whenever an item in your options menu is selected.
     *
     * @param item the menu item that was selected.
     * @return return false to allow normal menu processing to proceed, true to consume it here.
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        android.R.id.home -> {
            // Respond to the action bar's Up/Home button
            val upIntent: Intent? = NavUtils.getParentActivityIntent(this)
            if (upIntent == null) {
                throw IllegalStateException("No Parent Activity Intent")
            } else {
                upIntent.putExtra(EXTRA_KNITTING_ID, knittingID)
                NavUtils.navigateUpTo(this, upIntent)
            }
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    companion object {

        fun newIntent(context: Context, knittingID: Long): Intent {
            val intent = Intent(context, PhotoGalleryActivity::class.java)
            intent.putExtra(EXTRA_KNITTING_ID, knittingID)
            return intent
        }
    }
}
