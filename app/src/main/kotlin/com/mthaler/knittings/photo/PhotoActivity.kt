package com.mthaler.knittings.photo

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.mthaler.knittings.R
import com.mthaler.knittings.database.datasource
import org.jetbrains.anko.AnkoLogger
import kotlinx.android.synthetic.main.activity_photo.*

/**
 * Activity that displays a photo and allow to delete the photo and use photo as preview in knitting list
 */
class PhotoActivity : AppCompatActivity(), AnkoLogger {

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

        pager.offscreenPageLimit = 3
        val pagerAdapter = PhotoPagerAdapter(supportFragmentManager, datasource.getAllPhotos(knitting))
        pager.adapter = pagerAdapter
        if (currentPhoto >= 0) {
            pager.currentItem = currentPhoto
        }
    }

    companion object {
        const val EXTRA_PHOTO_ID = "com.mthaler.knitting.PHOTO_ID"
        const val EXTRA_KNITTING_ID = "com.mthaler.knitting.KNITTING_ID"
    }
}
