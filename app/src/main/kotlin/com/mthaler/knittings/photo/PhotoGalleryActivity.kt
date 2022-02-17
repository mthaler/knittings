package com.mthaler.knittings.photo

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.core.app.NavUtils
import android.view.MenuItem
import com.mthaler.knittings.BaseActivity
import com.mthaler.knittings.R
import com.mthaler.knittings.database.Extras.EXTRA_OWNER_ID
import com.mthaler.knittings.databinding.ActivityPhotoGalleryBinding

class PhotoGalleryActivity : BaseActivity(), PhotoGalleryFragment.OnFragmentInteractionListener {

    private var ownerID: Long = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityPhotoGalleryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        // enable up navigation
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val id = if (savedInstanceState != null) savedInstanceState.getLong(EXTRA_OWNER_ID) else intent.getLongExtra(EXTRA_OWNER_ID, -1L)
        if (id != -1L) {
            ownerID = id
        } else {
            error("Could not get owner id")
        }

        if (savedInstanceState == null) {
            val f = PhotoGalleryFragment.newInstance(ownerID)
            val fm = supportFragmentManager
            val ft = fm.beginTransaction()
            ft.add(R.id.photo_gallery_container, f)
            ft.commit()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putLong(EXTRA_OWNER_ID, ownerID)
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
                    super.onBackPressed()
                }
            }
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        val f = supportFragmentManager.findFragmentById(R.id.photo_gallery_container)
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

        fun newIntent(context: Context, ownerID: Long): Intent {
            val intent = Intent(context, PhotoGalleryActivity::class.java)
            intent.putExtra(EXTRA_OWNER_ID, ownerID)
            return intent
        }
    }
}