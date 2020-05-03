package com.mthaler.knittings.photo

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.core.app.NavUtils
import android.view.MenuItem
import com.mthaler.knittings.BaseActivity
import com.mthaler.knittings.R
import com.mthaler.knittings.Extras.EXTRA_KNITTING_ID
import kotlinx.android.synthetic.main.activity_photo_gallery.*

class PhotoGalleryActivity : BaseActivity(), PhotoGalleryFragment.OnFragmentInteractionListener {

    private var knittingID: Long = -1

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

        if (savedInstanceState == null) {
            val f = PhotoGalleryFragment.newInstance(knittingID)
            val fm = supportFragmentManager
            val ft = fm.beginTransaction()
            ft.add(R.id.photo_gallery_container, f)
            ft.commit()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putLong(EXTRA_KNITTING_ID, knittingID)
        super.onSaveInstanceState(outState)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        android.R.id.home -> {
            // Respond to the action bar's Up/Home button
            val upIntent: Intent? = NavUtils.getParentActivityIntent(this)
            if (upIntent == null) {
                throw IllegalStateException("No Parent Activity Intent")
            } else {
                val f = supportFragmentManager.findFragmentById(R.id.photo_gallery_container)
                if (f is PhotoFragment) {
                    f.onBackPressed()
                } else {
                    upIntent.putExtra(EXTRA_KNITTING_ID, knittingID)
                    NavUtils.navigateUpTo(this, upIntent)
                }
            }
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        val f = supportFragmentManager.findFragmentById(R.id.category_list_container)
        if (f is PhotoFragment) {
            f.onBackPressed()
        } else {
            super.onBackPressed()
        }
    }

    override fun photoClicked(photoID: Long) {
        val f = PhotoFragment.newInstance(photoID)
        val ft = supportFragmentManager.beginTransaction()
        ft.replace(R.id.photo_gallery_container, f)
        ft.addToBackStack(null)
        ft.commit()
    }

    companion object {

        fun newIntent(context: Context, knittingID: Long): Intent {
            val intent = Intent(context, PhotoGalleryActivity::class.java)
            intent.putExtra(EXTRA_KNITTING_ID, knittingID)
            return intent
        }
    }
}
