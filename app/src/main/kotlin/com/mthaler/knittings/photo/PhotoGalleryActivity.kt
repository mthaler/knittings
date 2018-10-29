package com.mthaler.knittings.photo

import android.app.Activity
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import com.mthaler.knittings.R
import com.mthaler.knittings.database.datasource
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.error
import java.io.File
import kotlinx.android.synthetic.main.activity_photo_gallery.*

class PhotoGalleryActivity : AppCompatActivity(), CanTakePhoto, AnkoLogger {

    private var knittingID: Long = -1
    override var currentPhotoPath: File? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_photo_gallery)

        setSupportActionBar(toolbar)

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

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.photo_gallery, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_item_add_photo -> {
                return takePhoto(this, layoutInflater, knittingID)
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

    companion object {
        val EXTRA_KNITTING_ID = "com.mthaler.knitting.KNITTING_ID"
    }
}
