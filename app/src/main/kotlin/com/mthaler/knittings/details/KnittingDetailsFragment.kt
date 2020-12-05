package com.mthaler.knittings.details

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.*
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.mthaler.dbapp.DeleteDialog
import com.mthaler.dbapp.photo.PhotoGalleryActivity
import com.mthaler.dbapp.photo.TakePhotoDialog
import com.mthaler.dbapp.utils.PictureUtils
import com.mthaler.knittings.Extras
import com.mthaler.knittings.R
import com.mthaler.knittings.database.KnittingsDataSource
import com.mthaler.knittings.databinding.FragmentKnittingDetailsBinding
import com.mthaler.knittings.model.Knitting
import com.mthaler.knittings.model.Status
import com.mthaler.knittings.rowcounter.RowCounterActivity
import com.mthaler.knittings.stopwatch.StopwatchActivity
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
    private var listener: OnFragmentInteractionListener? = null

    private var _binding: FragmentKnittingDetailsBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            knittingID = it.getLong(Extras.EXTRA_KNITTING_ID)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {

        setHasOptionsMenu(true)

        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(CURRENT_PHOTO_PATH)) {
                currentPhotoPath = File(savedInstanceState.getString(CURRENT_PHOTO_PATH))
            }
        }

        val metrics = resources.displayMetrics
        val ratio = metrics.heightPixels / metrics.widthPixels.toDouble()


        _binding = FragmentKnittingDetailsBinding.inflate(inflater, container, false)
        val view = binding.root

        val imageView = binding.image
        imageView.setOnClickListener {
            startActivity(PhotoGalleryActivity.newIntent(requireContext(), knittingID))
        }
        if (ratio >= 1.9) {
            // make the image smaller on devices like the Samsung Galaxy A9 that has a 18:9 aspect ratio
            val layoutParams = imageView.layoutParams as LinearLayout.LayoutParams
            layoutParams.weight = 3.0f
            imageView.layoutParams = layoutParams
        }

        binding.editKnittingDetails.setOnClickListener {
            listener?.editKnitting(knittingID)
        }

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().application)).get(KnittingDetailsViewModel::class.java)
        viewModel.init(knittingID)
        viewModel.knitting.observe(viewLifecycleOwner, { knitting ->
            updateDetails(knitting)
        })
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        currentPhotoPath?.let { outState.putString(CURRENT_PHOTO_PATH, it.absolutePath) }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.knitting_details, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_item_add_photo -> {
                val d = TakePhotoDialog.create(requireContext(), "com.mthaler.knittings.fileprovider", layoutInflater, this::takePhoto, this::importPhoto)
                d.show()
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
            binding.knittingStatusImage.setImageResource(Status.getDrawableResource(it.context, knitting.status))
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
                                        val orientation = PictureUtils.getOrientation(file.absolutePath)
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
                        val handler = Handler()
                        handler.postDelayed({
                            updateImageView(imageView, knitting, delayMillis * 2)
                        }, delayMillis)
                    }
                    return true
                }
            })
        }
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

        fun editKnitting(id: Long)
    }

    companion object {

        private const val REQUEST_IMAGE_CAPTURE = 0
        private const val REQUEST_IMAGE_IMPORT = 1
        private const val CURRENT_PHOTO_PATH = "com.mthaler.knittings.CURRENT_PHOTO_PATH"

        @JvmStatic
        fun newInstance(knittingID: Long) =
            KnittingDetailsFragment().apply {
                arguments = Bundle().apply {
                    putLong(Extras.EXTRA_KNITTING_ID, knittingID)
                }
            }
    }
}
