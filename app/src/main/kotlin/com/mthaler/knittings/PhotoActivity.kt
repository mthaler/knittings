package com.mthaler.knittings

import android.content.Intent
import android.support.design.widget.CoordinatorLayout
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.alert
import org.jetbrains.anko.debug

/**
 * Activity that displays a photo and allow to delete the photo and use photo as preview in knitting list
 */
class PhotoActivity : AppCompatActivity(), AnkoLogger {

    private var parentKnittingID: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_photo)
        val toolbar = findViewById(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // get the id of the knitting that should be displayed.
        val id = intent.getLongExtra(EXTRA_PHOTO_ID, -1)
        val photo = KnittingsDataSource.getInstance(this).getPhoto(id)

        // save parent knitting id
        parentKnittingID = intent.getLongExtra(KnittingActivity.EXTRA_KNITTING_ID, -1)

        // init photo view
        val photoDetailsView = supportFragmentManager.findFragmentById(R.id.fragment_photo) as PhotoDetailsView
        photoDetailsView.init(photo)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.photo, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_item_delete_photo -> {
                // show alert asking user to confirm that photo should be deleted
                alert {
                    title = resources.getString(R.string.delete_photo_dialog_title)
                    message = resources.getString(R.string.delete_photo_dialog_question)
                    positiveButton(resources.getString(R.string.delete_photo_dialog_delete_button)) {
                        val photoDetailsView = supportFragmentManager.findFragmentById(R.id.fragment_photo) as PhotoDetailsView
                        photoDetailsView.deletePhoto()
                        finish()
                    }
                    negativeButton(resources.getString(R.string.dialog_button_cancel)) {}
                }.show()
                return true
            }
            R.id.menu_item_set_main_photo -> {
                // set as default photo
                val photoDetailsView = supportFragmentManager.findFragmentById(R.id.fragment_photo) as PhotoDetailsView
                val photo = photoDetailsView.photo
                val knitting = KnittingsDataSource.getInstance(this).getKnitting(photo!!.knittingID)
                KnittingsDataSource.getInstance(this).updateKnitting(knitting.copy(defaultPhoto = photo))
                debug("Set $photo as default photo")
                val layout = findViewById(R.id.photo_activity_layout) as CoordinatorLayout
                Snackbar.make(layout, "Used as main photo", Snackbar.LENGTH_SHORT).show()
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    override fun getSupportParentActivityIntent(): Intent? {
        // add the knitting id so the parent activity can properly restore itself
        val intent = super.getSupportParentActivityIntent()
        intent?.putExtra(KnittingActivity.EXTRA_KNITTING_ID, parentKnittingID)
        return intent
    }

    companion object {
        val EXTRA_PHOTO_ID = "com.mthaler.knitting.PHOTO_ID"
    }
}
