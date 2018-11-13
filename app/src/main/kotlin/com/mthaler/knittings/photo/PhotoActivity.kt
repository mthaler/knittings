package com.mthaler.knittings.photo

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.NavUtils
import android.view.MenuItem
import com.mthaler.knittings.R
import com.mthaler.knittings.database.datasource
import org.jetbrains.anko.AnkoLogger
import com.mthaler.knittings.Extras.EXTRA_KNITTING_ID
import com.mthaler.knittings.Extras.EXTRA_PHOTO_ID
import kotlinx.android.synthetic.main.activity_photo.*

/**
 * Activity that displays a photo and allow to delete the photo and use photo as preview in knitting list
 */
class PhotoActivity : AppCompatActivity(), AnkoLogger {

    private var knittingID = -1L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_photo)

        setSupportActionBar(toolbar)

        // enable up navigation
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // get the id of the knitting that should be displayed.
        val id = intent.getLongExtra(EXTRA_PHOTO_ID, -1)
        val photo = datasource.getPhoto(id)

        val knitting = datasource.getKnitting(photo.knittingID)
        val photos = datasource.getAllPhotos(knitting)
        val currentPhoto = photos.indexOfFirst { p -> p.id == id }

        knittingID = knitting.id

        pager.offscreenPageLimit = 3
        val pagerAdapter = PhotoPagerAdapter(supportFragmentManager, datasource.getAllPhotos(knitting))
        pager.adapter = pagerAdapter
        if (currentPhoto >= 0) {
            pager.currentItem = currentPhoto
        }
    }

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
}
