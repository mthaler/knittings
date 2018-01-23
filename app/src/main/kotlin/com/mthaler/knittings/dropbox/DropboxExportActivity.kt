package com.mthaler.knittings.dropbox

import android.os.Bundle
import com.mthaler.knittings.R
import kotlinx.android.synthetic.main.activity_dropbox_export.*
import android.support.v7.app.AppCompatActivity

class DropboxExportActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dropbox_export)

        setSupportActionBar(toolbar)
    }
}
