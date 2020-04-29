package com.mthaler.knittings.compressphotos

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.lifecycle.Observer
import com.mthaler.knittings.BaseActivity
import com.mthaler.knittings.R
import kotlinx.android.synthetic.main.activity_stopwatch.*

class CompressPhotosActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_compress_photos)

        setSupportActionBar(toolbar)

        // enable up navigation
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        ServiceManager.getInstance().status.observe(this, Observer { status ->
            when(status) {
                is Status.Success ->
                    Toast.makeText(this, "Compress photo service finished", Toast.LENGTH_LONG).show()
            }
        })

        val button = findViewById<Button>(R.id.button)
        button.setOnClickListener {
            val intent = Intent(this@CompressPhotosActivity, CompressPhotosService::class.java)
            startService(intent);
        }
    }

    companion object {
        fun newIntent(context: Context): Intent = Intent(context, CompressPhotosActivity::class.java)
    }
}
