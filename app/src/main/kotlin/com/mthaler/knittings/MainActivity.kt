package com.mthaler.knittings

import android.os.Bundle
import androidx.core.view.GravityCompat
import com.mthaler.knittings.databinding.ActivityMainBinding

class MainActivity : BaseActivity() {

    lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState == null) {
            val f = MainFragment()
            val fm = supportFragmentManager
            val ft = fm.beginTransaction()
            ft.add(R.id.knitting_list_container, f)
            ft.commit()
        }

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        // enable up navigation
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

//    override fun onBackPressed() {
//        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
//            binding.drawerLayout.closeDrawer(GravityCompat.START)
//        } else {
//
//        }
//
//    }
}