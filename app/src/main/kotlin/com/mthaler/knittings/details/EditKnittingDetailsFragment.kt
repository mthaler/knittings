package com.mthaler.knittings.details

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.app.NavUtils
import com.mthaler.knittings.R
import com.mthaler.knittings.model.Knitting
import com.mthaler.knittings.utils.TimeUtils
import java.text.DateFormat
import com.mthaler.knittings.durationpicker.DurationPickerDialog
import com.mthaler.knittings.Extras.EXTRA_KNITTING_ID
import com.mthaler.knittings.SaveChangesDialog
import com.mthaler.knittings.category.SelectCategoryActivity
import com.mthaler.knittings.database.Extras.EXTRA_CATEGORY_ID
import com.mthaler.knittings.database.KnittingsDataSource
import com.mthaler.knittings.databinding.FragmentEditKnittingDetailsBinding
import com.mthaler.knittings.model.Category
import com.mthaler.knittings.model.Status
import com.mthaler.knittings.utils.DatePickerUtils
import java.util.*

class EditKnittingDetailsFragment : Fragment() {

    private var knittingID: Long = Knitting.EMPTY.id
    private var editOnly: Boolean = false

    private var _binding: FragmentEditKnittingDetailsBinding? = null
    private val binding get() = _binding!!

    private lateinit var started: Date
    private var finished: Date? = null
    private var duration = 0L
    private var category: Category? = null

    private val launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {
             result.data?.let {
                val categoryID = it.getLongExtra(EXTRA_CATEGORY_ID, Category.EMPTY.id)
                if (categoryID != Category.EMPTY.id) {
                    val c = KnittingsDataSource.getCategory(categoryID)
                    binding.knittingCategory.text = c?.name ?: resources.getString(R.string.edit_knitting_details_category_hint)
                    category = c
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            knittingID = it.getLong(EXTRA_KNITTING_ID)
            editOnly = it.getBoolean(EXTRA_EDIT_ONLY)
        }
        savedInstanceState?.let {
            if (it.containsKey(EXTRA_KNITTING_ID)) {
                knittingID = it.getLong(EXTRA_KNITTING_ID)
            }
            if (it.containsKey(EXTRA_EDIT_ONLY)) {
                editOnly = it.getBoolean(EXTRA_EDIT_ONLY)
            }
            if (it.containsKey(EXTRA_STARTED)) {
                started = Date(it.getLong(EXTRA_STARTED))
            }
            if (it.containsKey(EXTRA_FINISHED)) {
                finished = Date(it.getLong(EXTRA_FINISHED))
            }
            if (it.containsKey(EXTRA_DURATION)) {
                duration = it.getLong(EXTRA_DURATION)
            }
            if (it.containsKey(EXTRA_CATEGORY)) {
                val categoryID = it.getLong(EXTRA_CATEGORY)
                category = KnittingsDataSource.getCategory(categoryID)
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {

        setHasOptionsMenu(true)

        _binding = FragmentEditKnittingDetailsBinding.inflate(inflater, container, false)
        val view = binding.root

        // we need to set this in code because older android versions (API <21) do not support vector drawables with drawableLeft
        binding.knittingStarted.setCompoundDrawablesWithIntrinsicBounds(AppCompatResources.getDrawable(inflater.context, R.drawable.ic_date_range_black_24dp), null, null, null)
        // we need to set this in code because older android versions (API <21) do not support vector drawables with drawableLeft
        binding.knittingFinished.setCompoundDrawablesWithIntrinsicBounds(AppCompatResources.getDrawable(inflater.context, R.drawable.ic_date_range_black_24dp), null, null, null)
        binding.knittingStatus.adapter = StatusAdapter(requireContext())

        if (savedInstanceState == null) {
            val knitting = if (knittingID != Knitting.EMPTY.id) KnittingsDataSource.getProject(knittingID) else Knitting.EMPTY
            binding.knittingTitle.setText(knitting.title)
            binding.knittingDescription.setText(knitting.description)
            started = knitting.started
            finished = knitting.finished
            binding.knittingNeedleDiameter.setText(knitting.needleDiameter)
            binding.knittingSize.setText(knitting.size)
            duration = knitting.duration
            category = knitting.category
            val statusList = Status.formattedValues(requireContext())
            val index = statusList.indexOf(Status.format(requireContext(), knitting.status))
            if (index >= 0) {
                binding.knittingStatus.setSelection(index)
            } else {
                binding.knittingStatus.setSelection(0)
            }
            binding.ratingBar.rating = knitting.rating.toFloat()
        }
        binding.knittingStarted.text = DateFormat.getDateInstance().format(started)
        val f = finished
        binding.knittingFinished.text = if (f != null) DateFormat.getDateInstance().format(f) else ""
        binding.knittingDuration.text = TimeUtils.formatDuration(duration)
        category?.let {
            binding.knittingCategory.text = it.name
        }

        binding.knittingStarted.setOnClickListener {
            val dialog = DatePickerUtils.create(requireContext(), started) { _, date ->
                if (date != started) {
                    binding.knittingStarted.text = DateFormat.getDateInstance().format(date)
                    started = date
                }
            }
            dialog.show()
        }

        binding.knittingFinished.setOnClickListener {
            val fin = finished ?: Date()
            val dialog = DatePickerUtils.create(requireContext(), fin) { _, date ->
                if (date != finished) {
                    binding.knittingFinished.text = DateFormat.getDateInstance().format(date)
                    finished = date
                }
            }
            dialog.show()
        }

        binding.knittingDuration.setOnClickListener {
            val d = DurationPickerDialog(requireContext(), { _, d ->
                binding.knittingDuration.text = TimeUtils.formatDuration(d)
                duration = d
            }, duration)
            d.show()
        }
        binding.knittingCategory.setOnClickListener {
            val i = SelectCategoryActivity.newIntent(requireContext(), knittingID)
            launcher.launch(i)
        }

        return view
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    override fun onResume() {
        super.onResume()
        // make sure the category still exists
        category?.let {
            if (KnittingsDataSource.getCategory(it.id) == null) {
                binding.knittingCategory.text = resources.getString(R.string.edit_knitting_details_category_hint)
                category = null

            }
        }
    }



    override fun onSaveInstanceState(savedInstanceState: Bundle) {
        savedInstanceState.putLong(EXTRA_KNITTING_ID, knittingID)
        savedInstanceState.putBoolean(EXTRA_EDIT_ONLY, editOnly)
        savedInstanceState.putLong(EXTRA_STARTED, started.time)
        finished?.let {
            savedInstanceState.putLong(EXTRA_FINISHED, it.time)
        }
        savedInstanceState.putLong(EXTRA_DURATION, duration)
        category?.let {
            savedInstanceState.putLong(EXTRA_CATEGORY, it.id)
        }
        super.onSaveInstanceState(savedInstanceState)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.edit_knitting_details, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_item_save_knitting -> {
                val oldKnitting = if (knittingID != Knitting.EMPTY.id) KnittingsDataSource.getProject(knittingID) else Knitting.EMPTY
                val newKnitting = createKnitting().copy(defaultPhoto = oldKnitting.defaultPhoto)
                if (newKnitting != oldKnitting) {
                    saveKnitting(newKnitting)
                }
                if (editOnly) {
                    val upIntent: Intent = NavUtils.getParentActivityIntent(requireActivity()) ?: throw IllegalStateException("No Parent Activity Intent")
                    NavUtils.navigateUpTo(requireActivity(), upIntent)
                } else {
                    parentFragmentManager.popBackStack()
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }


    fun onBackPressed() {
        activity?.let {
            // Respond to the action bar's Up/Home button
            val upIntent: Intent? = NavUtils.getParentActivityIntent(it)
            if (upIntent == null) {
                throw IllegalStateException("No Parent Activity Intent")
            } else {
                val oldKnitting = if (knittingID != Knitting.EMPTY.id) KnittingsDataSource.getProject(knittingID) else Knitting.EMPTY
                val newKnitting = createKnitting().copy(defaultPhoto = oldKnitting.defaultPhoto)
                if (newKnitting != oldKnitting) {
                    SaveChangesDialog.create(it, {
                        saveKnitting(newKnitting)
                        if (editOnly) {
                            NavUtils.navigateUpTo(it, upIntent)
                        } else {
                            parentFragmentManager.popBackStack()
                        }
                    }, {
                        if (editOnly) {
                            NavUtils.navigateUpTo(it, upIntent)
                        } else {
                            parentFragmentManager.popBackStack()
                        }
                    }).show()
                } else {
                    if (editOnly) {
                        NavUtils.navigateUpTo(it, upIntent)
                    } else {
                        parentFragmentManager.popBackStack()
                    }
                }
            }
        }
    }

    private fun createKnitting(): Knitting {
        val status = Status.values()[binding.knittingStatus.selectedItemPosition]
        return Knitting(knittingID, binding.knittingTitle.text.toString(), binding.knittingDescription.text.toString(), started, finished, binding.knittingNeedleDiameter.text.toString(),
            binding.knittingSize.text.toString(), null, binding.ratingBar.rating.toDouble(), duration, category, status)
    }

    private fun saveKnitting(knitting: Knitting) {
        if (knitting.id == Knitting.EMPTY.id) {
            KnittingsDataSource.addProject(knitting)
        } else {
            KnittingsDataSource.updateProject(knitting)
        }
    }

    companion object {

        private const val EXTRA_STARTED = "com.mthaler.knittings.needle.STARTED"
        private const val EXTRA_FINISHED = "com.mthaler.knittings.needle.FINISHED"
        private const val EXTRA_DURATION = "com.mthaler.knittings.needle.DURATION"
        private const val EXTRA_CATEGORY = "com.mthaler.knittings.needle.CATEGORY"
        private const val EXTRA_EDIT_ONLY = "com.mthaler.knittings.edit_only"
        private const val REQUEST_SELECT_CATEGORY = 1

        @JvmStatic
        fun newInstance(knittingID: Long, editOnly: Boolean) =
            EditKnittingDetailsFragment().apply {
                arguments = Bundle().apply {
                    putLong(EXTRA_KNITTING_ID, knittingID)
                    putBoolean(EXTRA_EDIT_ONLY, editOnly)
                }
            }
    }
}
