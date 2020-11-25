package com.mthaler.knittings.needle

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.core.app.NavUtils
import android.view.MenuItem
import com.mthaler.dbapp.BaseActivity
import com.mthaler.knittings.R
import com.mthaler.knittings.databinding.ActivityNeedleListBinding
import com.mthaler.knittings.model.Needle

class NeedleListActivity : BaseActivity(), NeedleListFragment.OnFragmentInteractionListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityNeedleListBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        setSupportActionBar(binding.toolbar)

        // enable up navigation
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        if (savedInstanceState == null) {
            val f = NeedleListFragment()
            val fm = supportFragmentManager
            val ft = fm.beginTransaction()
            ft.add(R.id.needle_list_container, f)
            ft.commit()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        android.R.id.home -> {
            // Respond to the action bar's Up/Home button
            val upIntent: Intent? = NavUtils.getParentActivityIntent(this)
            if (upIntent == null) {
                throw IllegalStateException("No Parent Activity Intent")
            } else {
                val f = supportFragmentManager.findFragmentById(R.id.needle_list_container)
                if (f is EditNeedleFragment) {
                    f.onBackPressed()
                } else {
                    NavUtils.navigateUpTo(this, upIntent)
                }
            }
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        val f = supportFragmentManager.findFragmentById(R.id.needle_list_container)
        if (f is EditNeedleFragment) {
            f.onBackPressed()
        } else {
            super.onBackPressed()
        }
    }

    override fun createNeedle() {
        val f = EditNeedleFragment.newInstance(Needle.EMPTY.id)
        val ft = supportFragmentManager.beginTransaction()
        ft.replace(R.id.needle_list_container, f)
        ft.addToBackStack(null)
        ft.commit()
    }

    override fun needleClicked(needleID: Long) {
        val f = EditNeedleFragment.newInstance(needleID)
        val ft = supportFragmentManager.beginTransaction()
        ft.replace(R.id.needle_list_container, f)
        ft.addToBackStack(null)
        ft.commit()
    }

    companion object {

        fun newIntent(context: Context): Intent = Intent(context, NeedleListActivity::class.java)
    }
}
