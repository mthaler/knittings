package com.mthaler.knittings

import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import com.mthaler.knittings.database.datasource
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.alert
import org.jetbrains.anko.debug
import org.jetbrains.anko.error
import kotlinx.android.synthetic.main.activity_photo.*

/**
 * Activity that displays a photo and allow to delete the photo and use photo as preview in knitting list
 */
class PhotoActivity : AppCompatActivity(), AnkoLogger {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_photo)
        setSupportActionBar(toolbar)
        //supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // get the id of the knitting that should be displayed.
        val id = intent.getLongExtra(EXTRA_PHOTO_ID, -1)
        val photo = datasource.getPhoto(id)

        val knitting = datasource.getKnitting(photo.knittingID)
        val photos = datasource.getAllPhotos(knitting)
        val currentPhoto = photos.indexOfFirst { p -> p.id == id }

        pager.offscreenPageLimit = 3
        val pagerAdapter = PhotoPagerAdapter(supportFragmentManager, datasource.getAllPhotos(knitting))
        pager.adapter = pagerAdapter
        if (currentPhoto >= 0) {
            pager.currentItem = currentPhoto
        }


    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.photo, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_item_delete_photo -> {
                showDeletePhotoDialog()
                true
            }
            R.id.menu_item_set_main_photo -> {
                setDefaultPhoto()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    /**
     * how alert asking user to confirm that photo should be deleted
     */
    private fun showDeletePhotoDialog() {
        alert {
            title = resources.getString(R.string.delete_photo_dialog_title)
            message = resources.getString(R.string.delete_photo_dialog_question)
            positiveButton(resources.getString(R.string.delete_photo_dialog_delete_button)) {
                val adapter = pager.adapter as PhotoPagerAdapter
                if (adapter != null) {
                    val frag = adapter.getPhotoFragment()
                    if (frag != null) {
                        frag.deletePhoto()
                    } else {
                        error("Photo fragment null")
                    }
                } else {
                    error("Photo fragment null")
                }
                finish()
            }
            negativeButton(resources.getString(R.string.dialog_button_cancel)) {}
        }.show()
    }

    /**
     * Sets the current photo as the default photo used as preview for knittings list
     */
    private fun setDefaultPhoto() {
        val adapter = pager.adapter as PhotoPagerAdapter
        if (adapter != null) {
            val frag = adapter.getPhotoFragment()
            if (frag != null) {
                val photo = frag.photo
                val knitting = datasource.getKnitting(photo!!.knittingID)
                datasource.updateKnitting(knitting.copy(defaultPhoto = photo))
                debug("Set $photo as default photo")
                Snackbar.make(photo_activity_layout, "Used as main photo", Snackbar.LENGTH_SHORT).show()
            } else {
                error("Photo fragment null")
            }
        } else {
            error("Adapter null")
        }

    }



    companion object {
        val EXTRA_PHOTO_ID = "com.mthaler.knitting.PHOTO_ID"
        val EXTRA_KNITTING_ID = "com.mthaler.knitting.KNITTING_ID"
    }
}
