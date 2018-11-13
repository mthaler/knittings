package com.mthaler.knittings.details

import android.app.Activity
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.NavUtils
import android.view.Menu
import android.view.MenuItem
import com.mthaler.knittings.R
import com.mthaler.knittings.database.datasource
import kotlinx.android.synthetic.main.activity_edit_knitting_details.*
import com.mthaler.knittings.Extras.EXTRA_KNITTING_ID
import com.mthaler.knittings.photo.CanTakePhoto
import com.mthaler.knittings.photo.PhotoGalleryActivity
import org.jetbrains.anko.startActivity
import java.io.File

/**
 * EditKnittingDetailsActivity is used to edit knitting details
 */
class EditKnittingDetailsActivity : AppCompatActivity(), CanTakePhoto {

    // id of the displayed knitting
    private var knittingID: Long = -1
    override var currentPhotoPath: File? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_knitting_details)

        setSupportActionBar(toolbar)

        // enable up navigation
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // get the id of the knitting that should be displayed.
        val id = intent.getLongExtra(EXTRA_KNITTING_ID, -1L)
        if (id != -1L) {
            // initialize the edit knitting details fragment with the knitting it should display
            val fragment = supportFragmentManager.findFragmentById(R.id.fragment_edit_knitting_details) as EditKnittingDetailsFragment
            val knitting = datasource.getKnitting(id)
            fragment.init(knitting)
            knittingID = id
        }

        // restore current photo path
        if (savedInstanceState != null && savedInstanceState.containsKey(CURRENT_PHOTO_PATH)) {
            currentPhotoPath = File(savedInstanceState.getString(CURRENT_PHOTO_PATH))
        }
    }

    /**
     * This method is called if the activity gets destroyed because e.g. the device configuration changes because the device is rotated
     * We need to store instance variables because they are not automatically restored
     *
     * @param savedInstanceState saved instance state
     */
    override fun onSaveInstanceState(savedInstanceState: Bundle) {
        savedInstanceState.putLong(EXTRA_KNITTING_ID, knittingID)
        val p = currentPhotoPath
        if (p != null) {
            savedInstanceState.putString(CURRENT_PHOTO_PATH, p.absolutePath)
        }
        super.onSaveInstanceState(savedInstanceState)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.edit_knitting_details, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        android.R.id.home -> {
            // Respond to the action bar's Up/Home button
            val upIntent: Intent? = NavUtils.getParentActivityIntent(this)
            if (upIntent == null) {
                throw IllegalStateException("No Parent Activity Intent")
            } else {
                val fragment = supportFragmentManager.findFragmentById(R.id.fragment_edit_knitting_details) as EditKnittingDetailsFragment
                fragment.getKnittingID()?.let { upIntent.putExtra(EXTRA_KNITTING_ID, it) }
                NavUtils.navigateUpTo(this, upIntent)
            }
            true
        }
        R.id.menu_item_show_gallery -> {
            startActivity<PhotoGalleryActivity>(EXTRA_KNITTING_ID to knittingID)
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

    companion object {
        const val CURRENT_PHOTO_PATH = "com.mthaler.knitting.CURRENT_PHOTO_PATH"
    }
}
