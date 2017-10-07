package com.mthaler.knittings

import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import org.jetbrains.anko.alert
import java.util.ArrayList

/**
 * KnittingActivity displays a single knitting using KnittingFragment
 *
 * The activity is displayed when a new knitting is added or if a knitting is clicked
 * in the knittings list.
 *
 * The id of the knitting that should be displayed must be passed when the activity is started
 */
class KnittingActivity : AppCompatActivity() {

    private var knittingFragment: KnittingFragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(LOG_TAG, "onCreate called")

        setContentView(R.layout.activity_knitting)
        val toolbar = findViewById(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        // get the id of the knitting that should be displayed.
        val id = intent.getLongExtra(EXTRA_KNITTING_ID, -1L)
        if (id != -1L) {
            Log.d(LOG_TAG, "Got knitting id from extra: " + id)
        } else {
            Log.e(LOG_TAG, "Got invalid knitting id -1")
        }
        val knitting = KnittingsDataSource.getInstance(this.applicationContext).getKnitting(id)

        val viewPager = findViewById(R.id.viewpager) as ViewPager
        setupViewPager(viewPager, knitting)

        val tabLayout = findViewById(R.id.tabs) as TabLayout
        tabLayout.setupWithViewPager(viewPager)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.knitting, menu)
        return true
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_item_delete_knitting -> {
                // show alert asking user to confirm that knitting should be deleted
                alert {
                    title = resources.getString(R.string.delete_knitting_dialog_title)
                    message = resources.getString(R.string.delete_knitting_dialog_question)
                    positiveButton(resources.getString(R.string.delete_knitting_dialog_delete_button)) {
                        knittingFragment!!.deleteKnitting()
                        finish()
                    }
                    negativeButton(resources.getString(R.string.delete_knitting_dialog_cancel_button)) {}
                }.show()
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    private fun setupViewPager(viewPager: ViewPager, knitting: Knitting) {
        val adapter = ViewPagerAdapter(supportFragmentManager)
        knittingFragment = KnittingFragment.newInstance(knitting)
        adapter.addFragment(knittingFragment!!, "Details")
        val photoGalleryFragment = PhotoGalleryFragment.newInstance(knitting)
        adapter.addFragment(photoGalleryFragment, "Photos")
        viewPager.adapter = adapter
        viewPager.addOnPageChangeListener(object : ViewPager.SimpleOnPageChangeListener() {
            override fun onPageSelected(position: Int) {
                Log.d(LOG_TAG, "Page selected: " + position)
            }
        })

    }

    internal inner class ViewPagerAdapter(manager: FragmentManager) : FragmentPagerAdapter(manager) {
        private val fragmentList = ArrayList<Fragment>()
        private val fragmentTitleList = ArrayList<String>()

        override fun getItem(position: Int): Fragment {
            return fragmentList[position]
        }

        override fun getCount(): Int {
            return fragmentList.size
        }

        fun addFragment(fragment: Fragment, title: String) {
            fragmentList.add(fragment)
            fragmentTitleList.add(title)
        }

        override fun getPageTitle(position: Int): CharSequence {
            return fragmentTitleList[position]
        }
    }

    companion object {

        val EXTRA_KNITTING_ID = "com.mthaler.knitting.KNITTING_ID"
        private val LOG_TAG = KnittingActivity::class.java.simpleName
    }
}
