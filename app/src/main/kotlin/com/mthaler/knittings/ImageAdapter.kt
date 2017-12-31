package com.mthaler.knittings

import android.content.Context
import android.net.Uri
import android.support.v4.view.PagerAdapter
import android.support.v4.view.ViewPager
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.mthaler.knittings.model.Photo

class ImageAdapter(val context: Context, private val data: List<Photo>) : PagerAdapter() {

    override fun getCount(): Int {
        return data.size
    }

    override fun isViewFromObject(view: View, obj: Any): Boolean {
        return view === obj as ImageView
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val imageView = ImageView(context)
        imageView.setImageURI(Uri.fromFile(data.get(position).filename))
        imageView.scaleType = ImageView.ScaleType.CENTER_INSIDE
        (container as ViewPager).addView(imageView, 0)
        return imageView

    }

    override fun destroyItem(container: ViewGroup, position: Int, obj: Any) {
        (container as ViewPager).removeView(obj as ImageView)
    }
}

