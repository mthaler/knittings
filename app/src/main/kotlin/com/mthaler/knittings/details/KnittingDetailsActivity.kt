package com.mthaler.knittings.details

import android.app.Activity
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import com.mthaler.knittings.photo.CanTakePhoto
import com.mthaler.knittings.photo.PhotoGalleryActivity
import com.mthaler.knittings.R
import com.mthaler.knittings.database.datasource
import com.mthaler.knittings.stopwatch.StopwatchActivity
import org.jetbrains.anko.*
import java.io.File
import com.mthaler.knittings.Extras.EXTRA_KNITTING_ID
import kotlinx.android.synthetic.main.activity_knitting_details.*

/**
 * Activity that displays knitting details (name, description, start time etc.)
 */
class KnittingDetailsActivity : AppCompatActivity(), AnkoLogger, CanTakePhoto {

    // id of the displayed knitting
    private var knittingID: Long = -1
    override var currentPhotoPath: File? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_knitting_details)

        setSupportActionBar(toolbar)

        // enable up navigation
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // get the id of the knitting that should be displayed. If the application was destroyed because e.g. the device configuration changed
        // because the device was rotated we use the knitting id from the saved instance state. Otherwise we use the id passed to the intent
        val id = if (savedInstanceState != null) savedInstanceState.getLong(EXTRA_KNITTING_ID) else intent.getLongExtra(EXTRA_KNITTING_ID, -1L)
        if (id != -1L) {
            // start edit knitting details fragment if the user clicks the floating action button
            edit_knitting_details.setOnClickListener {
                startActivity<EditKnittingDetailsActivity>(EXTRA_KNITTING_ID to id)
            }
            knittingID = id
        } else {
            error("Could not get knitting id")
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

    override fun onResume() {
        super.onResume()
        if (knittingID != -1L) {
            // initialize the knitting details fragment with the knitting it should display
            val fragment = supportFragmentManager.findFragmentById(R.id.fragment_knitting_details) as KnittingDetailsFragment
            val knitting = datasource.getKnitting(knittingID)
            fragment.init(knitting)
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
        menuInflater.inflate(R.menu.knitting_details, menu)
        return true
    }

    /**
     * This hook is called whenever an item in your options menu is selected.
     *
     * @param item the menu item that was selected.
     * @return return false to allow normal menu processing to proceed, true to consume it here.
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_item_show_stopwatch -> {
                startActivity<StopwatchActivity>(EXTRA_KNITTING_ID to knittingID)
                return true
            }
            R.id.menu_item_show_gallery -> {
                startActivity<PhotoGalleryActivity>(EXTRA_KNITTING_ID to knittingID)
                return true
            }
            R.id.menu_item_add_photo -> {
                return takePhoto(this, layoutInflater, knittingID)
            }
            R.id.menu_item_delete_knitting -> {
                showDeleteDialog()
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode != Activity.RESULT_OK) {
            return
        }
        if (requestCode == CanTakePhoto.REQUEST_IMAGE_CAPTURE || requestCode == CanTakePhoto.REQUEST_IMAGE_IMPORT) {
            onActivityResult(this, knittingID, requestCode, resultCode, data)
        }
    }

    /**
     * Displays a dialog that asks the user to confirm that the knitting should be deleted
     */
    private fun showDeleteDialog() {
        // show alert asking user to confirm that knitting should be deleted
        alert {
            title = resources.getString(R.string.delete_knitting_dialog_title)
            message = resources.getString(R.string.delete_knitting_dialog_question)
            positiveButton(resources.getString(R.string.delete_knitting_dialog_delete_button)) {
                val knitting = datasource.getKnitting(knittingID)
                // delete database entry
                datasource.deleteKnitting(knitting)
                finish()
            }
            negativeButton(resources.getString(R.string.dialog_button_cancel)) {}
        }.show()
    }

    companion object {
        const val CURRENT_PHOTO_PATH = "com.mthaler.knitting.CURRENT_PHOTO_PATH"
    }
}
