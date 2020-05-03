package com.mthaler.knittings.compressphotos

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.core.app.NavUtils
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.mthaler.knittings.BaseActivity
import com.mthaler.knittings.R
import com.mthaler.knittings.service.JobStatus
import com.mthaler.knittings.service.ServiceStatus
import kotlinx.android.synthetic.main.activity_compress_photos.*

class CompressPhotosActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_compress_photos)

        setSupportActionBar(toolbar)

        // enable up navigation
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val buttonStart = findViewById<Button>(R.id.buttonStart)
        buttonStart.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            with(builder) {
                setTitle(resources.getString(R.string.compress_photos_dialog_title))
                setMessage(resources.getString(R.string.compress_photos_dialog_message))
                setPositiveButton(resources.getString(R.string.compress_photos_dialog_button_compress)) { dialog, which ->
                    CompressPhotosService.startService(this@CompressPhotosActivity, "Foreground Service is running...")
                }
                setNegativeButton(resources.getString(R.string.dialog_button_cancel)) { dialog, which -> }
                show()
            }
        }

        val buttonCancel = findViewById<Button>(R.id.cancel_button)
        buttonCancel.setOnClickListener {
            CompressPhotosServiceManager.getInstance().canceled = true
        }

        val viewModel = ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory.getInstance(this.application)).get(CompressPhotosViewModel::class.java)
        viewModel.statistics.observe(this, Observer { statistics ->
            number_of_photos.text = statistics.photos.toString()
            total_size.text = Format.humanReadableByteCountBin(statistics.totalSize)
            will_be_compressed.text = statistics.photosToCompress.toString()
        })

        CompressPhotosServiceManager.getInstance().jobStatus.observe(this, Observer { jobStatus->
            when(jobStatus) {
                is JobStatus.Initialized -> {
                    buttonStart.isEnabled = true
                    compressing_photos_title.visibility = View.GONE
                    progressBar.visibility = View.GONE
                    cancel_button.visibility = View.GONE
                    result.visibility = View.GONE
                }
                is JobStatus.Progress -> {
                    buttonStart.isEnabled = false
                    compressing_photos_title.visibility = View.VISIBLE
                    progressBar.visibility = View.VISIBLE
                    cancel_button.visibility = View.VISIBLE
                    progressBar.progress = jobStatus.value
                    result.visibility = View.GONE
                }
                is JobStatus.Success -> {
                    buttonStart.isEnabled = true
                    compressing_photos_title.visibility = View.VISIBLE
                    progressBar.visibility = View.GONE
                    cancel_button.visibility = View.GONE
                    result.visibility = View.VISIBLE
                    result.text = jobStatus.msg
                }
            }
        })

        CompressPhotosServiceManager.getInstance().serviceStatus.observe(this, Observer { serviceStatus ->
            when (serviceStatus) {
                ServiceStatus.Stopped -> buttonStart.isEnabled = true
                ServiceStatus.Started -> buttonStart.isEnabled = false
            }
        })
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        android.R.id.home -> {
            // Respond to the action bar's Up/Home button
            val upIntent: Intent? = NavUtils.getParentActivityIntent(this)
            if (upIntent == null) {
                throw IllegalStateException("No Parent Activity Intent")
            } else {
                NavUtils.navigateUpTo(this, upIntent)
            }
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    companion object {
        fun newIntent(context: Context): Intent = Intent(context, CompressPhotosActivity::class.java)
    }
}
