package com.mthaler.knittings.projectcount

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.mthaler.knittings.databinding.ActivityProjectCountBinding

class ProjectCountActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityProjectCountBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        // enable up navigation
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    companion object {

        fun newIntent(context: Context): Intent = Intent(context, ProjectCountActivity::class.java)
    }
}