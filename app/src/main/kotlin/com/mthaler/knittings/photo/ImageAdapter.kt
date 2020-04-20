package com.mthaler.knittings.photo

import android.content.Context
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.ImageView
import androidx.lifecycle.LifecycleCoroutineScope
import com.mthaler.knittings.model.Photo
import com.mthaler.knittings.utils.PictureUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ImageAdapter(val context: Context, private val lifecycleScope: LifecycleCoroutineScope, private val data: List<Photo>) : PagerAdapter() {

    override fun getCount(): Int = data.size

    override fun isViewFromObject(view: View, obj: Any): Boolean = view === obj as ImageView

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val imageView = ImageView(context)
        imageView.scaleType = ImageView.ScaleType.CENTER_INSIDE
        val viewTreeObserver = imageView.viewTreeObserver
        viewTreeObserver.addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {
            override fun onPreDraw(): Boolean {
                imageView.viewTreeObserver.removeOnPreDrawListener(this)
                val width = imageView.measuredWidth
                val height = imageView.measuredHeight
                val filename = data[position].filename.absolutePath
                lifecycleScope.launch {
                    val rotatedPhoto = withContext(Dispatchers.IO) {
                        val orientation = PictureUtils.getOrientation(filename)
                        val photo = PictureUtils.decodeSampledBitmapFromPath(filename, width, height)
                        PictureUtils.rotateBitmap(photo, orientation)
                    }
                    imageView.setImageBitmap(rotatedPhoto)
                }
                return true
            }
        })
        (container as ViewPager).addView(imageView, 0)
        return imageView
    }

    override fun destroyItem(container: ViewGroup, position: Int, obj: Any) {
        (container as ViewPager).removeView(obj as ImageView)
    }
}