package com.mthaler.knittings.details

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.app.NavUtils
import com.mthaler.dbapp.SaveChangesDialog
import com.mthaler.dbapp.category.SelectCategoryActivity
import com.mthaler.knittings.R
import com.mthaler.dbapp.datepicker.DatePickerFragment
import com.mthaler.knittings.model.Knitting
import com.mthaler.knittings.utils.TimeUtils
import java.text.DateFormat
import com.mthaler.knittings.durationpicker.DurationPickerDialog
import com.mthaler.dbapp.Extras.EXTRA_CATEGORY_ID
import com.mthaler.knittings.Extras.EXTRA_KNITTING_ID
import com.mthaler.dbapp.model.Category
import com.mthaler.dbapp.utils.DatePickerUtils
import com.mthaler.knittings.database.KnittingsDataSource
import com.mthaler.knittings.databinding.FragmentEditKnittingDetailsBinding
import com.mthaler.knittings.model.Status
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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

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
        binding.knittingFinished.text = if (finished != null) DateFormat.getDateInstance().format(finished) else ""
        binding.knittingDuration.text = TimeUtils.formatDuration(duration)
        category?.let {
            binding.knittingCategory.text = it.name
        }

        binding.knittingStarted.setOnClickListener {
            val dialog = DatePickerUtils.create(requireContext(), started) { view, date ->
                if (date != started) {
                    binding.knittingStarted.text = DateFormat.getDateInstance().format(date)
                    started = date
                }
            }
            dialog.show()
        }
        binding.knittingFinished.setOnClickListener {
            val dialog = DatePickerFragment.newInstance(if (finished != null) finished else Date())
            dialog.setTargetFragment(this, REQUEST_FINISHED)
            dialog.show(parentFragmentManager, DIALOG_DATE)
        }
        binding.knittingDuration.setOnClickListener {
            val d = DurationPickerDialog(requireContext(), { durationPicker, d ->
                binding.knittingDuration.text = TimeUtils.formatDuration(d)
                duration = d
            }, duration)
            d.show()
        }
        binding.knittingCategory.setOnClickListener {
            val i = SelectCategoryActivity.newIntent(requireContext(), knittingID)
            startActivityForResult(i, REQUEST_SELECT_CATEGORY)
        }

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
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
                    val upIntent: Intent? = NavUtils.getParentActivityIntent(requireActivity())
                    if (upIntent == null) {
                        throw IllegalStateException("No Parent Activity Intent")
                    }
                    NavUtils.navigateUpTo(requireActivity(), upIntent)
                } else {
                    parentFragmentManager.popBackStack()
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != Activity.RESULT_OK) {
            return
        }
        if (requestCode == REQUEST_FINISHED) {
            val date = data!!.getSerializableExtra(DatePickerFragment.EXTRA_DATE) as Date
            if (date != finished) {
                binding.knittingFinished.text = DateFormat.getDateInstance().format(date)
                finished = date
            }
        } else if (requestCode == REQUEST_SELECT_CATEGORY) {
            data?.let {
                val categoryID = it.getLongExtra(EXTRA_CATEGORY_ID, Category.EMPTY.id)
                if (categoryID != Category.EMPTY.id) {
                    val c = KnittingsDataSource.getCategory(categoryID)
                    binding.knittingCategory.text = c.name
                    category = c
                }
            }
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
        private const val DIALOG_DATE = "date"
        private const val REQUEST_STARTED = 0
        private const val REQUEST_FINISHED = 1
        private const val REQUEST_SELECT_CATEGORY = 2

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
