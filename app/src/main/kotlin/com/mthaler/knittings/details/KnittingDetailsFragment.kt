package com.mthaler.knittings.details

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.*
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.NavUtils
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.mthaler.knittings.DeleteDialog
import com.mthaler.knittings.Extras.EXTRA_KNITTING_ID
import com.mthaler.knittings.MainActivity
import com.mthaler.knittings.R
import com.mthaler.knittings.database.KnittingsDataSource
import com.mthaler.knittings.databinding.FragmentKnittingDetailsBinding
import com.mthaler.knittings.model.Knitting
import com.mthaler.knittings.model.Status
import com.mthaler.knittings.needle.EditNeedleFragment
import com.mthaler.knittings.photo.PhotoGalleryActivity
import com.mthaler.knittings.photo.TakePhotoDialog
import com.mthaler.knittings.rowcounter.RowCounterActivity
import com.mthaler.knittings.stopwatch.StopwatchActivity
import com.mthaler.knittings.utils.PictureUtils
import com.mthaler.knittings.utils.TimeUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.DateFormat

/**
 * Fragment that displays knitting details (name, description, start time etc.)
 */
class KnittingDetailsFragment : Fragment() {

    private var knittingID: Long = Knitting.EMPTY.id
    private lateinit var viewModel: KnittingDetailsViewModel
    private var currentPhotoPath: File? = null
    private var editOnly: Boolean = false

    private var _binding: FragmentKnittingDetailsBinding? = null
    private val binding get() = _binding!!

    private val launchImageCapture = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.let {

            }
        }
    }

    private val launchImageImport = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.let {

            }
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val a = arguments
        if (a != null) {
            knittingID = a.getLong(EXTRA_KNITTING_ID)
            editOnly = a.getBoolean(EXTRA_EDIT_ONLY)
        }

        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                requireActivity().supportFragmentManager.popBackStack()
            }
        }

        // This callback will only be called when MyFragment is at least Started.
        requireActivity().onBackPressedDispatcher.addCallback(this, callback)
    }

    override fun onSaveInstanceState(savedInstanceState: Bundle) {
        savedInstanceState.putLong(EXTRA_KNITTING_ID, knittingID)
        savedInstanceState.putBoolean(EXTRA_EDIT_ONLY, editOnly)
        currentPhotoPath?.let { savedInstanceState.putString(CURRENT_PHOTO_PATH, it.absolutePath) }
        super.onSaveInstanceState(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {

        setHasOptionsMenu(true)

        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(CURRENT_PHOTO_PATH)) {
                currentPhotoPath = File(savedInstanceState.getString(CURRENT_PHOTO_PATH)!!)
            }
        }

        val metrics = resources.displayMetrics
        val ratio = metrics.heightPixels / metrics.widthPixels.toDouble()


        _binding = FragmentKnittingDetailsBinding.inflate(inflater, container, false)
        val view = binding.root
        val imageView = binding.image

        if (ratio >= 1.9) {
            // make the image smaller on devices like the Samsung Galaxy A9 that has a 18:9 aspect ratio
            val layoutParams = imageView.layoutParams as LinearLayout.LayoutParams
            layoutParams.weight = 3.0f
            imageView.layoutParams = layoutParams
        }

        binding.editKnittingDetails.setOnClickListener {
            editKnitting()
        }

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val imageView = binding.image
        imageView.setOnClickListener {
            startActivity(PhotoGalleryActivity.newIntent(requireContext(), knittingID))
        }

        viewModel = ViewModelProvider(requireActivity()).get(KnittingDetailsViewModel::class.java)
        viewModel.init(knittingID)
        viewModel.knitting.observe(viewLifecycleOwner, { knitting ->
            updateDetails(knitting)
        })
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.knitting_details, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_item_add_photo -> {
                requestMultiplePermissions.launch(arrayOf(Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE))
                true
            }
            R.id.menu_item_show_gallery -> {
                startActivity(PhotoGalleryActivity.newIntent(requireContext(), knittingID))
                true
            }
            R.id.menu_item_show_stopwatch -> {
                startActivity(StopwatchActivity.newIntent(requireContext(), knittingID))
                true
            }
            R.id.menu_item_delete_knitting -> {
                val knitting = KnittingsDataSource.getProject(knittingID)
                DeleteDialog.create(requireContext(), knitting.title) {
                    viewModel.delete()
                    requireActivity().finish()
                }.show()
                return true
            }
            R.id.menu_item_row_counter -> {
                startActivity(RowCounterActivity.newIntent(requireContext(), knittingID))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

     fun editKnitting() {
        val f = EditKnittingDetailsFragment.newInstance(knittingID, editOnly)
        val ft = requireActivity().supportFragmentManager.beginTransaction()
        ft.replace(R.id.knitting_list_container, f)
        ft.addToBackStack(null)
        ft.commit()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode != Activity.RESULT_OK) {
            return
        }
        when (requestCode) {
            REQUEST_IMAGE_CAPTURE -> {
                currentPhotoPath?.let {
                    lifecycleScope.launch {
                        withContext(Dispatchers.IO) {
                            TakePhotoDialog.handleTakePhotoResult(requireContext(), knittingID, it) }
                    }
                }
            }
            REQUEST_IMAGE_IMPORT -> {
                val f = currentPhotoPath
                if (f != null && data != null) {
                    lifecycleScope.launch {
                        withContext(Dispatchers.IO) {
                            TakePhotoDialog.handleImageImportResult(requireContext(), knittingID, f, data)
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

    private fun updateDetails(knitting: Knitting) {
        view?.let {
            binding.image.setImageBitmap(null)
            updateImageView(binding.image, knitting, 50)
            binding.knittingTitle.text = knitting.title
            binding.knittingDescription.text = knitting.description
            binding.knittingStarted.text = DateFormat.getDateInstance().format(knitting.started)
            binding.knittingFinished.text = if (knitting.finished != null) DateFormat.getDateInstance().format(knitting.finished) else ""
            binding.knittingNeedleDiameter.text = knitting.needleDiameter
            binding.knittingSize.text = knitting.size
            binding.knittingDuration.text = TimeUtils.formatDuration(knitting.duration)
            val c = knitting.category
            binding.knittingCategory.text = c?.name ?: ""
            binding.knittingStatusImage.setImageResource(Status.getDrawableResource(knitting.status))
            binding.knittingStatus.text = Status.format(it.context, knitting.status)
            binding.ratingBar.rating = knitting.rating.toFloat()
        }
    }

    private fun updateImageView(imageView: ImageView, knitting: Knitting, delayMillis: Long) {
        if (delayMillis < 5000) {
            val viewTreeObserver = imageView.viewTreeObserver
            viewTreeObserver.addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {
                override fun onPreDraw(): Boolean {
                    imageView.viewTreeObserver.removeOnPreDrawListener(this)
                    val width = imageView.measuredWidth
                    val height = imageView.measuredHeight
                    if (width > 0 && height > 0) {
                        if (knitting.defaultPhoto != null) {
                            viewLifecycleOwner.lifecycleScope.launch {
                                val result = withContext(Dispatchers.Default) {
                                    val file = knitting.defaultPhoto.filename
                                    if (file.exists()) {
                                        val orientation = PictureUtils.getOrientation(file.absolutePath.toUri(), requireContext())
                                        val photo = PictureUtils.decodeSampledBitmapFromPath(file.absolutePath, width, height)
                                        val rotatedPhoto = PictureUtils.rotateBitmap(photo, orientation)
                                        rotatedPhoto
                                    } else {
                                        null
                                    }
                                }
                                if (result != null) {
                                    imageView.setImageBitmap(result)
                                }
                            }
                        } else {
                            imageView.setImageResource(R.drawable.categories)
                        }
                    } else {
                        val handler = Handler(Looper.myLooper()!!)
                        handler.postDelayed({
                            updateImageView(imageView, knitting, delayMillis * 2)
                        }, delayMillis)
                    }
                    return true
                }
            })
        }
    }

    companion object {

        private const val REQUEST_IMAGE_CAPTURE = 0
        private const val REQUEST_IMAGE_IMPORT = 1
        private const val CURRENT_PHOTO_PATH = "com.mthaler.knittings.CURRENT_PHOTO_PATH"
        private const val EXTRA_EDIT_ONLY = "com.mthaler.knittings.edit_only"

        @JvmStatic
        fun newInstance(knittingID: Long) =
            KnittingDetailsFragment().apply {
                arguments = Bundle().apply {
                    putLong(EXTRA_KNITTING_ID, knittingID)
                }
            }
    }
}
