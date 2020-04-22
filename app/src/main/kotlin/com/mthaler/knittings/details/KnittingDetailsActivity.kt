package com.mthaler.knittings.details

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.core.app.NavUtils
import android.view.Menu
import android.view.MenuItem
import androidx.lifecycle.lifecycleScope
import com.mthaler.knittings.BaseActivity
import com.mthaler.knittings.photo.PhotoGalleryActivity
import com.mthaler.knittings.R
import com.mthaler.knittings.database.datasource
import com.mthaler.knittings.stopwatch.StopwatchActivity
import java.io.File
import com.mthaler.knittings.Extras.EXTRA_KNITTING_ID
import com.mthaler.knittings.photo.TakePhotoDialog
import kotlinx.android.synthetic.main.activity_knitting_details.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Activity that displays knitting details (name, description, start time etc.)
 */
class KnittingDetailsActivity : BaseActivity(), KnittingDetailsFragment.OnFragmentInteractionListener {

    // id of the displayed knitting
    private var knittingID: Long = -1
    private var editOnly: Boolean = false
    private var currentPhotoPath: File? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_knitting_details)

        setSupportActionBar(toolbar)

        // enable up navigation
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // get the id of the knitting that should be displayed. If the application was destroyed because e.g. the device configuration changed
        // because the device was rotated we use the knitting id from the saved instance state. Otherwise we use the id passed to the intent
        knittingID = if (savedInstanceState != null) savedInstanceState.getLong(EXTRA_KNITTING_ID) else intent.getLongExtra(EXTRA_KNITTING_ID, -1L)
        editOnly = intent.getBooleanExtra(EXTRA_EDIT_ONLY, false)
        if (savedInstanceState == null) {
            if (editOnly) {
                val f = EditKnittingDetailsFragment.newInstance(knittingID, editOnly)
                val fm = supportFragmentManager
                val ft = fm.beginTransaction()
                ft.add(R.id.knitting_details_container, f)
                ft.commit()
            } else {
                val f = KnittingDetailsFragment.newInstance(knittingID)
                val fm = supportFragmentManager
                val ft = fm.beginTransaction()
                ft.add(R.id.knitting_details_container, f)
                ft.commit()
            }
        } else {
            if (savedInstanceState.containsKey(CURRENT_PHOTO_PATH)) {
                currentPhotoPath = File(savedInstanceState.getString(CURRENT_PHOTO_PATH))
            }
        }
    }

    override fun onSaveInstanceState(savedInstanceState: Bundle) {
        savedInstanceState.putLong(EXTRA_KNITTING_ID, knittingID)
        savedInstanceState.putBoolean(EXTRA_EDIT_ONLY, editOnly)
        currentPhotoPath?.let { savedInstanceState.putString(CURRENT_PHOTO_PATH, it.absolutePath) }
        super.onSaveInstanceState(savedInstanceState)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.knitting_details, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        android.R.id.home -> {
            // Respond to the action bar's Up/Home button
            val upIntent: Intent? = NavUtils.getParentActivityIntent(this)
            if (upIntent == null) {
                throw IllegalStateException("No Parent Activity Intent")
            } else {
                val fm = supportFragmentManager
                val f = fm.findFragmentById(R.id.knitting_details_container)
                if (f is EditKnittingDetailsFragment) {
                    f.onBackPressed()
                } else {
                    NavUtils.navigateUpTo(this, upIntent)
                }
            }
            true
        }
        R.id.menu_item_show_gallery -> {
            startActivity(PhotoGalleryActivity.newIntent(this, knittingID))
            true
        }
        R.id.menu_item_add_photo -> {
            val d = TakePhotoDialog.create(this, layoutInflater, knittingID, this::takePhoto, this::importPhoto)
            d.show()
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        val fm = supportFragmentManager
        val f = fm.findFragmentById(R.id.knitting_details_container)
        if (f is EditKnittingDetailsFragment) {
            f.onBackPressed()
        } else {
            super.onBackPressed()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode != Activity.RESULT_OK) {
            return
        }
        when (requestCode) {
            REQUEST_IMAGE_CAPTURE -> {
                currentPhotoPath?.let {
                    lifecycleScope.launch {
                        withContext(Dispatchers.IO) {
                            TakePhotoDialog.handleTakePhotoResult(this@KnittingDetailsActivity, knittingID, it) }
                        }
                    }
            }
            REQUEST_IMAGE_IMPORT -> {
                val f = currentPhotoPath
                if (f != null && data != null) {
                    lifecycleScope.launch {
                        withContext(Dispatchers.IO) {
                            TakePhotoDialog.handleImageImportResult(this@KnittingDetailsActivity, knittingID, f, data)
                        }
                    }
                }
            }
            else -> super.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun takePhoto(file: File, intent: Intent) {
        currentPhotoPath = file
        startActivityForResult(intent, REQUEST_IMAGE_CAPTURE)
    }

    private fun importPhoto(file: File, intent: Intent) {
        currentPhotoPath = file
        startActivityForResult(intent, REQUEST_IMAGE_IMPORT)
    }

    override fun editKnitting(id: Long) {
        val f = EditKnittingDetailsFragment.newInstance(knittingID, editOnly)
        val fm = supportFragmentManager
        val ft = fm.beginTransaction()
        ft.replace(R.id.knitting_details_container, f)
        ft.addToBackStack(null)
        ft.commit()
    }

    companion object {

        const val EXTRA_EDIT_ONLY = "com.mthaler.knittings.edit_only"
        private const val CURRENT_PHOTO_PATH = "com.mthaler.knittings.CURRENT_PHOTO_PATH"
        private const val REQUEST_IMAGE_CAPTURE = 0
        private const val REQUEST_IMAGE_IMPORT = 1

        fun newIntent(context: Context, knittingID: Long, editOnly: Boolean): Intent {
            val intent = Intent(context, KnittingDetailsActivity::class.java)
            intent.putExtra(EXTRA_KNITTING_ID, knittingID)
            intent.putExtra(EXTRA_EDIT_ONLY, editOnly)
            return intent
        }
    }
}
