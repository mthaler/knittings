package com.mthaler.knittings.compressphotos

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.Button
import android.widget.Toast
import androidx.core.app.NavUtils
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.mthaler.knittings.BaseActivity
import com.mthaler.knittings.R
import com.mthaler.knittings.service.JobStatus
import kotlinx.android.synthetic.main.activity_compress_photos.*

class CompressPhotosActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_compress_photos)

        setSupportActionBar(toolbar)

        // enable up navigation
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        CompressPhotosServiceManager.getInstance().jobStatus.observe(this, Observer { status ->
            when(status) {
                is JobStatus.Success ->
                    Toast.makeText(this, "Compress photo service finished", Toast.LENGTH_LONG).show()
            }
        })

        val buttonStart = findViewById<Button>(R.id.buttonStart)
        buttonStart.setOnClickListener {
            CompressPhotosService.startService(this, "Foreground Service is running...")
        }

        val buttonStop = findViewById<Button>(R.id.buttonStop)
        buttonStop.setOnClickListener {
            CompressPhotosService.stopService(this)
        }

        val viewModel = ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory.getInstance(this.application)).get(CompressPhotosViewModel::class.java)
        viewModel.statistics.observe(this, Observer { statistics ->
            number_of_photos.setText(statistics.photos.toString())
            total_size.setText(Format.humanReadableByteCountBin(statistics.totalSize))
            will_be_compressed.setText(statistics.photosToCompress.toString())
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
