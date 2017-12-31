package com.mthaler.knittings

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.Toolbar
import com.mthaler.knittings.database.datasource
import org.jetbrains.anko.find

class PhotoGalleryActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_photo_gallery)

        val toolbar = find<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        // get the id of the knitting that should be displayed.
        val id = intent.getLongExtra(EXTRA_KNITTING_ID, -1)

        if (id != -1L) {
            val fragment = supportFragmentManager.findFragmentById(R.id.fragment_photo_gallery) as PhotoGalleryFragment
            if (fragment != null) {
                val knitting = datasource.getKnitting(id)
                fragment.init(knitting)
            } else {
                error("Could not get photo gallery fragment")
            }
        }
    }

    companion object {
        val EXTRA_KNITTING_ID = "com.mthaler.knitting.KNITTING_ID"
    }
}
