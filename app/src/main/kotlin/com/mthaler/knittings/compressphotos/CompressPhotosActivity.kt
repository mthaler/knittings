package com.mthaler.knittings.compressphotos

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.mthaler.knittings.BaseActivity
import com.mthaler.knittings.R
import kotlinx.android.synthetic.main.activity_stopwatch.*

class CompressPhotosActivity : BaseActivity() {

    private val broadcastReceiver = CompressPhotosBroadcastReceiver()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_compress_photos)

        setSupportActionBar(toolbar)

        // enable up navigation
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, IntentFilter("MY_ACTION"))

        val button = findViewById<Button>(R.id.button)
        button.setOnClickListener {
            val intent = Intent(this@CompressPhotosActivity, CompressPhotosService::class.java)
            startService(intent);
        }
    }

    override fun onStop() {
        super.onStop()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver)
    }

    private fun sendBroadcast(view: View?) {
        val intent = Intent("MY_ACTION")
        intent.putExtra("data", "Hello World!")
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

    companion object {
        fun newIntent(context: Context): Intent = Intent(context, CompressPhotosActivity::class.java)
    }
}
