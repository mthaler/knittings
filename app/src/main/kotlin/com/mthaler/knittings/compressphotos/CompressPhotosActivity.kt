package com.mthaler.knittings.compressphotos

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.core.app.NavUtils
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.mthaler.dbapp.BaseActivity
import com.mthaler.knittings.R
import com.mthaler.dbapp.service.JobStatus
import com.mthaler.dbapp.service.ServiceStatus
import com.mthaler.dbapp.utils.Format
import com.mthaler.knittings.databinding.ActivityCompressPhotosBinding

class CompressPhotosActivity : BaseActivity() {

    private lateinit var binding: ActivityCompressPhotosBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCompressPhotosBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        setSupportActionBar(binding.toolbar)

        // enable up navigation
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.buttonStart.setOnClickListener {
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

        binding.cancelButton.setOnClickListener {
            CompressPhotosServiceManager.getInstance().cancelled = true
        }

        val viewModel = ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory.getInstance(this.application)).get(CompressPhotosViewModel::class.java)
        viewModel.statistics.observe(this, Observer { statistics ->
            binding.numberOfPhotos.text = statistics.photos.toString()
            binding.totalSize.text = Format.humanReadableByteCountBin(statistics.totalSize)
            binding.willBeCompressed.text = statistics.photosToCompress.toString()
        })

        CompressPhotosServiceManager.getInstance().jobStatus.observe(this, Observer { jobStatus->
            when(jobStatus) {
                is JobStatus.Initialized -> {
                    binding.buttonStart.isEnabled = true
                    binding.compressingPhotosTitle.visibility = View.GONE
                    binding.progressBar.visibility = View.GONE
                    binding.cancelButton.visibility = View.GONE
                    binding.result.visibility = View.GONE
                }
                is JobStatus.Progress -> {
                    binding.buttonStart.isEnabled = false
                    binding.compressingPhotosTitle.visibility = View.VISIBLE
                    binding.progressBar.visibility = View.VISIBLE
                    binding.cancelButton.visibility = View.VISIBLE
                    binding.progressBar.progress = jobStatus.value
                    binding.result.visibility = View.GONE
                }
                is JobStatus.Success -> {
                    binding.buttonStart.isEnabled = true
                    binding.compressingPhotosTitle.visibility = View.VISIBLE
                    binding.progressBar.visibility = View.GONE
                    binding.cancelButton.visibility = View.GONE
                    binding.result.visibility = View.VISIBLE
                    binding.result.text = jobStatus.msg
                }
                is JobStatus.Cancelled -> {
                    CompressPhotosServiceManager.getInstance().cancelled = false
                    binding.buttonStart.isEnabled = true
                    binding.compressingPhotosTitle.visibility = View.VISIBLE
                    binding.progressBar.visibility = View.GONE
                    binding.cancelButton.visibility = View.GONE
                    binding.result.visibility = View.VISIBLE
                    binding.result.text = jobStatus.msg
                }
            }
        })

        CompressPhotosServiceManager.getInstance().serviceStatus.observe(this, Observer { serviceStatus ->
            when (serviceStatus) {
                ServiceStatus.Stopped -> binding.buttonStart.isEnabled = true
                ServiceStatus.Started -> binding.buttonStart.isEnabled = false
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
                val sm = CompressPhotosServiceManager.getInstance()
                if (sm.jobStatus.value is JobStatus.Success || sm.jobStatus.value is JobStatus.Cancelled) {
                    sm.updateJobStatus(JobStatus.Initialized)
                }
                NavUtils.navigateUpTo(this, upIntent)
            }
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        val sm = CompressPhotosServiceManager.getInstance()
        if (sm.jobStatus.value is JobStatus.Success || sm.jobStatus.value is JobStatus.Cancelled) {
            sm.updateJobStatus(JobStatus.Initialized)
        }
        super.onBackPressed()
    }

    companion object {
        fun newIntent(context: Context): Intent = Intent(context, CompressPhotosActivity::class.java)
    }
}
