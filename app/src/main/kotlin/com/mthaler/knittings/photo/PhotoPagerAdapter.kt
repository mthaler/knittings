package com.mthaler.knittings.photo

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import com.mthaler.knittings.model.Photo

class PhotoPagerAdapter(fm: FragmentManager, val photos: List<Photo>) : FragmentStatePagerAdapter(fm) {

    /**
     * Returns the Fragment associated with a specified position.
     *
     * @param position position
     * @return fragment at position
     */
    override fun getItem(position: Int): Fragment {
        val photo = photos[position]
        return PhotoFragment.newInstance(photo.id)
    }

    /**
     * Returns the number of views available.
     *
     * @return number of views available
     */
    override fun getCount(): Int = photos.size
}