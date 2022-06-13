package com.mthaler.knittings.photo

import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import com.google.android.material.snackbar.Snackbar
import androidx.fragment.app.Fragment
import android.view.*
import android.widget.ImageView
import com.mthaler.knittings.database.Extras.EXTRA_PHOTO_ID
import androidx.lifecycle.lifecycleScope
import com.mthaler.knittings.DatabaseApplication
import com.mthaler.knittings.DeleteDialog
import com.mthaler.knittings.R
import com.mthaler.knittings.SaveChangesDialog
import com.mthaler.knittings.database.PhotoDataSource
import com.mthaler.knittings.databinding.FragmentPhotoBinding
import com.mthaler.knittings.model.Photo
import com.mthaler.knittings.utils.PictureUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * PhotoFragment displays a photo and the description
 */
class PhotoFragment : Fragment() {

    private lateinit var ds: PhotoDataSource
    private lateinit var photo: Photo
    private var _binding: FragmentPhotoBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        ds = (requireContext().applicationContext as DatabaseApplication).getPhotoDataSource()
        arguments?.let {
            val photoID = it.getLong(EXTRA_PHOTO_ID)
            photo = ds.getPhoto(photoID)
        }
        savedInstanceState?.let {
            if (it.containsKey(EXTRA_PHOTO_ID)) {
                val photoID = it.getLong(EXTRA_PHOTO_ID)
                photo = ds.getPhoto(photoID)
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {

        // we need to call setHasOptionsMenu(true) to indicate that we are interested in participating in the action bar
        setHasOptionsMenu(true)

        _binding = FragmentPhotoBinding.inflate(inflater, container, false)
        val view = binding.root

        updateImageView(binding.image, photo, 50)

        if (savedInstanceState == null) {
            binding.photoDescription.setText(photo.description)
        }

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun updateImageView(imageView: ImageView, photo: Photo, delayMillis: Long) {
        if (delayMillis < 5000) {
            // we use a view tree observer to get the width and the height of the image view and scale the image accordingly reduce memory usage
            val viewTreeObserver = imageView.viewTreeObserver
            viewTreeObserver.addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {
                override fun onPreDraw(): Boolean {
                    imageView.viewTreeObserver.removeOnPreDrawListener(this)
                    val width = imageView.measuredWidth
                    val height = imageView.measuredHeight
                    if (width > 0 && height > 0) {
                        // loading and scaling the bitmap is expensive, use async task to do the work
                        val path = photo.filename.absolutePath
                        viewLifecycleOwner.lifecycleScope.launch {
                            val rotated = withContext(Dispatchers.Default) {
                                val orientation = PictureUtils.getOrientation(Uri.parse(path), requireContext())
                                val scaled = PictureUtils.decodeSampledBitmapFromPath(path, width, height)
                                PictureUtils.rotateBitmap(scaled, orientation)
                            }
                            imageView.setImageBitmap(rotated)
                        }
                    } else {
                        val handler = Handler(Looper.myLooper()!!)
                        handler.postDelayed({
                            updateImageView(imageView, photo, delayMillis * 2)
                        }, delayMillis)
                    }
                    return true
                }
            })
        }
    }

    override fun onSaveInstanceState(savedInstanceState: Bundle) {
        savedInstanceState.putLong(EXTRA_PHOTO_ID, photo.id)
        super.onSaveInstanceState(savedInstanceState)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.photo, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_item_delete_photo -> {
                DeleteDialog.create(requireContext(), binding.photoDescription.text.toString()) {
                    deletePhoto()
                    parentFragmentManager.popBackStack()
                }.show()
                true
            }
            R.id.menu_item_set_main_photo -> {
                setDefaultPhoto()
                true
            }
            R.id.menu_item_rotate_photo -> {
                true
            }
            R.id.menu_item_save_photo -> {
                if (binding.photoDescription.text.toString() != photo.description) {
                    savePhoto(photo.copy(description = binding.photoDescription.text.toString()))
                }
                parentFragmentManager.popBackStack()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    fun onBackPressed() {
        if (binding.photoDescription.text.toString() != photo.description) {
            SaveChangesDialog.create(requireContext(), {
                savePhoto(photo.copy(description = binding.photoDescription.text.toString()))
                parentFragmentManager.popBackStack()
            }, {
                parentFragmentManager.popBackStack()
            }).show()
        } else {
            parentFragmentManager.popBackStack()
        }
    }

    private fun setDefaultPhoto() {
        ds.setDefaultPhoto(photo.ownerID, photo)
        view?.let {
            Snackbar.make(it, "Used as main photo", Snackbar.LENGTH_SHORT).show()
        }
    }

    private fun deletePhoto() {
        ds.deletePhoto(photo)
    }

    private fun savePhoto(photo: Photo) {
        if (photo.id == -1L) {
            ds.addPhoto(photo)
        } else {
            ds.updatePhoto(photo)
        }
    }

    companion object {

        fun newInstance(photoID: Long): PhotoFragment {
            val bundle = Bundle()
            bundle.putLong(EXTRA_PHOTO_ID, photoID)

            val fragment = PhotoFragment()
            fragment.arguments = bundle

            return fragment
        }
    }
}