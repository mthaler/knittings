package com.mthaler.knittings.photo

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.mthaler.knittings.R
import com.mthaler.knittings.database.Extras.EXTRA_OWNER_ID
import com.mthaler.knittings.databinding.FragmentPhotoGalleryBinding
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

    private val requestMultiplePermissions = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
        if (permissions != null && permissions.size == 3) {
            val d = TakePhotoDialog.create(requireContext(), "com.mthaler.knittings.fileprovider",  layoutInflater, this::takePhoto, this::importPhoto)
            d.show()
        } else {
            Log.e(TAG, "Permissions denied")
        }
    }

    private val launchImageCapture = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
       currentPhotoPath?.let {
            lifecycleScope.launch {
                withContext(Dispatchers.IO) {
                    TakePhotoDialog.handleTakePhotoResult(requireContext(), ownerID, it) }
            }
        }
    }

    private val launchImageImport = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.let {
                val f = currentPhotoPath
                if (f != null) {
                    lifecycleScope.launch {
                        withContext(Dispatchers.IO) {
                            TakePhotoDialog.handleImageImportResult(requireContext(), ownerID, f, it)
                        }
                    }
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            ownerID = it.getLong(EXTRA_OWNER_ID)
        }

        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                requireActivity().supportFragmentManager.popBackStack()
            }
        }

        // This callback will only be called when MyFragment is at least Started.
        requireActivity().onBackPressedDispatcher.addCallback(this, callback)
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

    override fun onSaveInstanceState(savedInstanceState: Bundle) {
        savedInstanceState.putLong(EXTRA_OWNER_ID, ownerID)
        currentPhotoPath?.let { savedInstanceState.putString(CURRENT_PHOTO_PATH, it.absolutePath) }
        super.onSaveInstanceState(savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val photoGalleryAdapter = PhotoGalleryAdapter(requireContext(), viewLifecycleOwner.lifecycleScope) { photo -> photoClicked(photo.id) }
        val orientation = this.resources.configuration.orientation
        val columns = if (orientation == Configuration.ORIENTATION_PORTRAIT) 2 else 3
        val gridLayoutManager = GridLayoutManager(requireContext(), columns)
        binding.gridView.layoutManager = gridLayoutManager
        binding.gridView.adapter = photoGalleryAdapter
        val viewModel = ViewModelProvider(requireActivity()).get(PhotoGalleryViewModel::class.java)
        viewModel.init(ownerID)
        viewModel.photos.observe(viewLifecycleOwner, { photos ->
            // show the newest photos first. The id is incremented for each photo that is added, thus we can sort by id
            photos.sortByDescending { it.id }
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
                when {
                    ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED -> {
                        val d = TakePhotoDialog.create(requireContext(), "com.mthaler.knittings.fileprovider", layoutInflater, this::takePhoto, this::importPhoto)
                        d.show()
                    }
                    ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), Manifest.permission.CAMERA) -> {
                        Log.d(TAG, resources.getString(R.string.permission_required))
                        requestMultiplePermissions.launch(arrayOf(Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE))
                    }
                    else -> {
                        requestMultiplePermissions.launch(arrayOf(Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE))
                    }
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun takePhoto(file: File, intent: Intent) {
        currentPhotoPath = file
        launchImageCapture.launch(intent)
    }

    private fun importPhoto(file: File, intent: Intent) {
        currentPhotoPath = file
        launchImageImport.launch(intent)
    }

    private fun photoClicked(photoID: Long) {
        val f = PhotoFragment.newInstance(photoID)
        val ft = requireActivity().supportFragmentManager.beginTransaction()
        ft.replace(R.id.photo_gallery_container, f)
        ft.addToBackStack(null)
        ft.commit()
    }

    companion object {
        private const val TAG = "PhotoGalleryFragment"

        @JvmStatic
        fun newInstance(ownerID: Long) =
            PhotoGalleryFragment().apply {
                arguments = Bundle().apply {
                    putLong(EXTRA_OWNER_ID, ownerID)
                }
            }

        const val CURRENT_PHOTO_PATH = "com.mthaler.dbapp.CURRENT_PHOTO_PATH"
    }
}