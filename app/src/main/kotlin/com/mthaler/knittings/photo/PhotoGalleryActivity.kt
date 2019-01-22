package com.mthaler.knittings.photo

import android.app.Activity
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.NavUtils
import android.view.Menu
import android.view.MenuItem
import com.mthaler.knittings.R
import com.mthaler.knittings.database.datasource
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.error
import java.io.File
import com.mthaler.knittings.Extras.EXTRA_KNITTING_ID
import kotlinx.android.synthetic.main.activity_photo_gallery.*

class PhotoGalleryActivity : AppCompatActivity(), CanTakePhoto, AnkoLogger {

    private var knittingID: Long = -1
    override var currentPhotoPath: File? = null

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
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putLong(EXTRA_KNITTING_ID, knittingID)
        super.onSaveInstanceState(outState)
    }

    override fun onResume() {
        super.onResume()
        if (knittingID != -1L) {
            val fragment = supportFragmentManager.findFragmentById(R.id.fragment_photo_gallery) as PhotoGalleryFragment
            val knitting = datasource.getKnitting(knittingID)
            fragment.init(knitting)
        } else {
            error("Could not get knitting id")
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
        menuInflater.inflate(R.menu.photo_gallery, menu)
        return true
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

        R.id.menu_item_add_photo -> {
            takePhoto(this, layoutInflater, knittingID)
        }
        else -> super.onOptionsItemSelected(item)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode != Activity.RESULT_OK) {
            return
        }
        if (requestCode == CanTakePhoto.REQUEST_IMAGE_CAPTURE || requestCode == CanTakePhoto.REQUEST_IMAGE_IMPORT) {
            onActivityResult(this, knittingID, requestCode, resultCode, data)
        }
    }
}
