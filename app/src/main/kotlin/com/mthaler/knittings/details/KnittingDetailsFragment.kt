package com.mthaler.knittings.details

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.google.android.material.floatingactionbutton.FloatingActionButton
import androidx.fragment.app.Fragment
import android.view.*
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RatingBar
import android.widget.TextView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.mthaler.dbapp.DeleteDialog
import com.mthaler.dbapp.photo.PhotoGalleryActivity
import com.mthaler.dbapp.photo.TakePhotoDialog
import com.mthaler.dbapp.ui.SquareImageView
import com.mthaler.dbapp.utils.PictureUtils
import com.mthaler.knittings.Extras
import com.mthaler.knittings.R
import com.mthaler.knittings.database.KnittingsDataSource
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            knittingID = it.getLong(Extras.EXTRA_KNITTING_ID)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        setHasOptionsMenu(true)

        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(CURRENT_PHOTO_PATH)) {
                currentPhotoPath = File(savedInstanceState.getString(CURRENT_PHOTO_PATH))
            }
        }

        val metrics = resources.displayMetrics
        val ratio = metrics.heightPixels / metrics.widthPixels.toDouble()


        val v = inflater.inflate(R.layout.fragment_knitting_details, container, false)

        val imageView = v.findViewById<SquareImageView>(R.id.image)
        imageView.setOnClickListener {
            startActivity(PhotoGalleryActivity.newIntent(requireContext(), knittingID))
        }
        if (ratio >= 1.9) {
            // make the image smaller on devices like the Samsung Galaxy A9 that has a 18:9 aspect ratio
            val layoutParams = imageView.layoutParams as LinearLayout.LayoutParams
            layoutParams.weight = 3.0f
            imageView.layoutParams = layoutParams
        }

        val fabEdit = v.findViewById<FloatingActionButton>(R.id.edit_knitting_details)
        fabEdit.setOnClickListener {
            listener?.editKnitting(knittingID)
        }

        return v
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().application)).get(KnittingDetailsViewModel::class.java)
        viewModel.init(knittingID)
        viewModel.knitting.observe(viewLifecycleOwner, Observer { knitting ->
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
                val knitting = KnittingsDataSource.getKnitting(knittingID)
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
            val imageView = it.findViewById<SquareImageView>(R.id.image)
            imageView.setImageBitmap(null)
            val viewTreeObserver = imageView.viewTreeObserver
            viewTreeObserver.addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {
                override fun onPreDraw(): Boolean {
                    imageView.viewTreeObserver.removeOnPreDrawListener(this)
                    val width = imageView.measuredWidth
                    val height = imageView.measuredHeight
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
                    return true
                }
            })


            val knittingTitle = it.findViewById<TextView>(R.id.knitting_title)
            knittingTitle.text = knitting.title

            val knittingDescription = it.findViewById<TextView>(R.id.knitting_description)
            knittingDescription.text = knitting.description

            val knittingStarted = it.findViewById<TextView>(R.id.knitting_started)
            knittingStarted.text = DateFormat.getDateInstance().format(knitting.started)

            val knittingFinished = it.findViewById<TextView>(R.id.knitting_finished)
            knittingFinished.text = if (knitting.finished != null) DateFormat.getDateInstance().format(knitting.finished) else ""

            val knittingNeedleDiameter = it.findViewById<TextView>(R.id.knitting_needle_diameter)
            knittingNeedleDiameter.text = knitting.needleDiameter

            val knittingSize = it.findViewById<TextView>(R.id.knitting_size)
            knittingSize.text = knitting.size

            val knittingDuration = it.findViewById<TextView>(R.id.knitting_duration)
            knittingDuration.text = TimeUtils.formatDuration(knitting.duration)

            val knittingCategory = it.findViewById<TextView>(R.id.knitting_category)
            val c = knitting.category
            knittingCategory.text = c?.name ?: ""

            val knittingStatusImage = it.findViewById<ImageView>(R.id.knitting_status_image)
            knittingStatusImage.setImageResource(Status.getDrawableResource(it.context, knitting.status))

            val knittingStatus = it.findViewById<TextView>(R.id.knitting_status)
            knittingStatus.text = Status.format(it.context, knitting.status)

            val ratingBar = it.findViewById<RatingBar>(R.id.ratingBar)
            ratingBar.rating = knitting.rating.toFloat()
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
