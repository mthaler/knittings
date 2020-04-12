package com.mthaler.knittings

import android.content.Context
import android.content.Intent
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_project_count.*

class ProjectCountActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_project_count)

        setSupportActionBar(toolbar)

        // enable up navigation
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    companion object {

        fun newIntent(context: Context): Intent {
            val intent = Intent(context, ProjectCountActivity::class.java)
            return intent
        }
    }
}
