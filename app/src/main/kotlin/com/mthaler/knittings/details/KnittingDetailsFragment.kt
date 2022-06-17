package com.mthaler.knittings.details

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.*
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.mthaler.knittings.DeleteDialog
import com.mthaler.knittings.Extras.EXTRA_KNITTING_ID
import com.mthaler.knittings.R
import com.mthaler.knittings.database.KnittingsDataSource
import com.mthaler.knittings.databinding.FragmentKnittingDetailsBinding
import com.mthaler.knittings.model.Knitting
import com.mthaler.knittings.model.Photo
import com.mthaler.knittings.model.Status
import com.mthaler.knittings.photo.PhotoGalleryActivity
import com.mthaler.knittings.photo.TakePhotoDialog
import com.mthaler.knittings.rowcounter.RowCounterActivity
import com.mthaler.knittings.stopwatch.StopwatchActivity
import com.mthaler.knittings.utils.PictureUtils
import com.mthaler.knittings.utils.TimeUtils
import com.mthaler.knittings.utils.toBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.DateFormat
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * Fragment that displays knitting details (name, description, start time etc.)
 */
class KnittingDetailsFragment : Fragment() {

    private var knittingID: Long = Knitting.EMPTY.id
    private lateinit var viewModel: KnittingDetailsViewModel
    private var currentPhotoPath: File? = null
    private var editOnly: Boolean = false

    /** Blocking camera operations are performed using this executor */
    private lateinit var cameraExecutor: ExecutorService

    // Get a stable reference of the modifiable image capture use case
    private var imageCapture: ImageCapture? = null

    private var _binding: FragmentKnittingDetailsBinding? = null
    private val binding get() = _binding!!

    private val launchImageImport = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.let {
                val f = currentPhotoPath
                if (f != null) {
                    lifecycleScope.launch {
                        withContext(Dispatchers.IO) {
                            TakePhotoDialog.handleImageImportResult(requireContext(), knittingID, f, it)
                        }
                    }
                }
            }
        }
    }

    private val requestMultiplePermissions = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
        if (permissions != null && permissions.size == 2) {
            val d = TakePhotoDialog.create(requireContext(), layoutInflater, this::takePhoto, this::importPhoto)
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

         // Initialize our background executor
        cameraExecutor = Executors.newSingleThreadExecutor()

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

        // Shut down our background executor
        cameraExecutor.shutdown()
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

    override fun onResume() {
        super.onResume()

        startCamera()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.knitting_details, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_item_add_photo -> {
                requestMultiplePermissions.launch(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE))
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

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
       if (requestCode == REQUEST_CODE_PERMISSIONS) {
           if (allPermissionsGranted()) {
               startCamera()
           } else {
               Toast.makeText(requireContext(), "Permissions not granted by the user.", Toast.LENGTH_SHORT).show()
               requireActivity().finish()
           }
       }
    }

      private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
          ContextCompat.checkSelfPermission(requireContext(), it) == PackageManager.PERMISSION_GRANTED
      }


    fun editKnitting() {
        val f = EditKnittingDetailsFragment.newInstance(knittingID, editOnly)
        val ft = requireActivity().supportFragmentManager.beginTransaction()
        ft.replace(R.id.knitting_details_container, f)
        ft.addToBackStack(null)
        ft.commit()
    }

    private fun takePhoto(file: File) {
        currentPhotoPath = file

        val callback = object : ImageCapture.OnImageCapturedCallback() {

            @androidx.camera.core.ExperimentalGetImage
            override fun onCaptureSuccess(image: ImageProxy) {

                lifecycleScope.launch {
                    withContext(Dispatchers.IO) {
                        val f = file
                        val fos = FileOutputStream(f)
                        fos.write(Photo.getBytes(image.image?.let {  it.toBitmap() } ) )
                    }
                    TakePhotoDialog.handleTakePhotoResult(requireContext(), knittingID, file)
                }
            }

            override fun onError(exception: ImageCaptureException) {
                Log.e(TAG, "Photo capture failed: ${exception.message}", exception)
            }
        }

        imageCapture?.let{ it.takePicture(cameraExecutor, callback) }
    }

    private fun importPhoto(file: File, intent: Intent) {
        currentPhotoPath = file
        launchImageImport.launch(intent)
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

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())

        cameraProviderFuture.addListener({
           // Used to bind the lifecycle of cameras to the lifecycle owner
           val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

           imageCapture = ImageCapture.Builder().build()

           // Select back camera as a default
           val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

           try {
               // Unbind use cases before rebinding
               cameraProvider.unbindAll()

               // Bind use cases to camera
               cameraProvider.bindToLifecycle(
                   this, cameraSelector, imageCapture)

           } catch(exc: Exception) {
               Log.e(TAG, "Use case binding failed", exc)
           }
        }, ContextCompat.getMainExecutor(requireContext()))
    }

    companion object {

        private const val TAG = "KnittingDetailsFragment"

        private const val CURRENT_PHOTO_PATH = "com.mthaler.knittings.CURRENT_PHOTO_PATH"
        private const val EXTRA_EDIT_ONLY = "com.mthaler.knittings.edit_only"
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS =
           mutableListOf (
               Manifest.permission.CAMERA,
               Manifest.permission.RECORD_AUDIO
           ).apply {
               if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                   add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
               }
           }.toTypedArray()


        @JvmStatic
        fun newInstance(knittingID: Long) =
            KnittingDetailsFragment().apply {
                arguments = Bundle().apply {
                    putLong(EXTRA_KNITTING_ID, knittingID)
                }
            }
    }
}
