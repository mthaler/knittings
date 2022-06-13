package com.mthaler.knittings.photo

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.mthaler.knittings.R
import com.mthaler.knittings.database.Extras.EXTRA_OWNER_ID
import com.mthaler.knittings.databinding.FragmentPhotoGalleryBinding
import com.mthaler.knittings.utils.AndroidViewModelFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

/**
 * A fragment that displays a list of photos using a grid
 */
class PhotoGalleryFragment : Fragment() {

    private var ownerID: Long = -1
    private var currentPhotoPath: File? = null
    private var _binding: FragmentPhotoGalleryBinding? = null
    private val binding get() = _binding!!
    private var listener: OnFragmentInteractionListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            ownerID = it.getLong(EXTRA_OWNER_ID)
        }
    }

    private val requestMultiplePermissions = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
        if (permissions != null && permissions.size == 3) {
            val d = TakePhotoDialog.create(requireContext(), "com.mthaler.knittings.fileprovider", layoutInflater, this::takePhoto, this::importPhoto)
            d.show()
        } else {
            Toast.makeText(requireContext(), "Permissions denied", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {

        setHasOptionsMenu(true)

        _binding = FragmentPhotoGalleryBinding.inflate(inflater, container, false)
        val view = binding.root

        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(EXTRA_OWNER_ID)) {
                ownerID = savedInstanceState.getLong(EXTRA_OWNER_ID)
            }
            if (savedInstanceState.containsKey(CURRENT_PHOTO_PATH)) {
                currentPhotoPath = File(savedInstanceState.getString(CURRENT_PHOTO_PATH)!!)
            }
        }

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onSaveInstanceState(savedInstanceState: Bundle) {
        savedInstanceState.putLong(EXTRA_OWNER_ID, ownerID)
        currentPhotoPath?.let { savedInstanceState.putString(CURRENT_PHOTO_PATH, it.absolutePath) }
        super.onSaveInstanceState(savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val photoGalleryAdapter = PhotoGalleryAdapter(requireContext(), viewLifecycleOwner.lifecycleScope) { photo -> listener?.photoClicked(photo.id) }
        val orientation = this.resources.configuration.orientation
        val columns = if (orientation == Configuration.ORIENTATION_PORTRAIT) 2 else 3
        val gridLayoutManager = GridLayoutManager(requireContext(), columns)
        binding.gridView.layoutManager = gridLayoutManager
        binding.gridView.adapter = photoGalleryAdapter
        val viewModel = AndroidViewModelFactory(requireActivity().application).create(PhotoGalleryViewModel::class.java)
        viewModel.init(ownerID)
        viewModel.photos.observe(viewLifecycleOwner, { photos ->
            // show the newest photos first. The id is incremented for each photo that is added, thus we can sort by id
            val photos = photos.sortedByDescending { it.id }
            photoGalleryAdapter.setPhotos(photos)
        })
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.photo_gallery, menu)
    }

     override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_item_add_photo -> {
                requestMultiplePermissions.launch(arrayOf(Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode != Activity.RESULT_OK) {
            return
        }
        when (requestCode) {
            REQUEST_IMAGE_CAPTURE -> {
                currentPhotoPath?.let {
                    viewLifecycleOwner.lifecycleScope.launch {
                        withContext(Dispatchers.IO) {
                            TakePhotoDialog.handleTakePhotoResult(requireContext(), ownerID, it) }
                    }
                }
            }
            REQUEST_IMAGE_IMPORT -> {
                val f = currentPhotoPath
                if (f != null && data != null) {
                    viewLifecycleOwner.lifecycleScope.launch {
                        withContext(Dispatchers.IO) {
                            TakePhotoDialog.handleImageImportResult(requireContext(), ownerID, f, data)
                        }
                    }
                }
            }
            else -> super.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun takePhoto(file: File, intent: Intent) {
        currentPhotoPath = file
        startActivityForResult(intent, REQUEST_IMAGE_CAPTURE)
    }

    private fun importPhoto(file: File, intent: Intent) {
        currentPhotoPath = file
        startActivityForResult(intent, REQUEST_IMAGE_IMPORT)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnFragmentInteractionListener) {
            listener = context
        } else {
            throw RuntimeException("$context must implement OnFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    interface OnFragmentInteractionListener {

        fun photoClicked(photoID: Long)
    }

    companion object {

        @JvmStatic
        fun newInstance(ownerID: Long) =
            PhotoGalleryFragment().apply {
                arguments = Bundle().apply {
                    putLong(EXTRA_OWNER_ID, ownerID)
                }
            }

        const val CURRENT_PHOTO_PATH = "com.mthaler.dbapp.CURRENT_PHOTO_PATH"
        const val REQUEST_IMAGE_CAPTURE = 0
        const val REQUEST_IMAGE_IMPORT = 1
    }
}