package com.mthaler.knittings

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import com.mthaler.knittings.database.datasource
import org.jetbrains.anko.find

class PhotoGalleryActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_photo_gallery)

        val toolbar = find<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        //supportActionBar!!.setDisplayHomeAsUpEnabled(true)

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

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.photo_gallery, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_item_add_photo -> {
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    companion object {
        val EXTRA_KNITTING_ID = "com.mthaler.knitting.KNITTING_ID"
    }
}
