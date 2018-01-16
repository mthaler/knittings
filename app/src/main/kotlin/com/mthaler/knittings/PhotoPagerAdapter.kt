package com.mthaler.knittings

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import android.view.ViewGroup
import com.mthaler.knittings.model.Photo

class PhotoPagerAdapter(fm: FragmentManager, val photos: List<Photo>) : FragmentStatePagerAdapter(fm) {

    private var _photoFragment: PhotoFragment? = null

    override fun getItem(position: Int): Fragment {
        val photo = photos[position]
        return PhotoFragment.newInstance(photo.id)
    }

    override fun getCount(): Int = photos.size

    override fun setPrimaryItem(container: ViewGroup, position: Int, item: Any) {
        _photoFragment = item as PhotoFragment
        super.setPrimaryItem(container, position, item)
    }

    fun getPhotoFragment(): PhotoFragment? = _photoFragment
}