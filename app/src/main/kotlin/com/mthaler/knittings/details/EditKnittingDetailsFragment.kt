package com.mthaler.knittings.details

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.appcompat.content.res.AppCompatResources
import android.widget.*
import androidx.core.app.NavUtils
import com.mthaler.knittings.R
import com.mthaler.knittings.category.SelectCategoryActivity
import com.mthaler.knittings.database.datasource
import com.mthaler.knittings.datepicker.DatePickerFragment
import com.mthaler.knittings.model.Knitting
import com.mthaler.knittings.utils.TimeUtils
import java.text.DateFormat
import com.mthaler.knittings.durationpicker.DurationPickerDialog
import com.mthaler.knittings.Extras.EXTRA_CATEGORY_ID
import com.mthaler.knittings.Extras.EXTRA_KNITTING_ID
import com.mthaler.knittings.SaveChangesDialog
import com.mthaler.knittings.model.Category
import com.mthaler.knittings.model.Status
import java.util.Date

class EditKnittingDetailsFragment : Fragment() {

    private var knittingID: Long = -1
    private var editOnly: Boolean = false
    private lateinit var editTextTitle: EditText
    private lateinit var editTextDescription: EditText
    private lateinit var textViewStarted: TextView
    private lateinit var started: Date
    private lateinit var textViewFinished: TextView
    private var finished: Date? = null
    private lateinit var textViewDuration: TextView
    private var duration = 0L
    private lateinit var editTextNeedleDiameter: EditText
    private lateinit var editTextSize: EditText
    private lateinit var buttonCategory: Button
    private var category: Category? = null
    private lateinit var spinnerStatus: Spinner
    private lateinit var ratingBar: RatingBar

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
                category = datasource.getCategory(categoryID)
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        setHasOptionsMenu(true)

        // Inflate the layout for this fragment
        val v = inflater.inflate(R.layout.fragment_edit_knitting_details, container, false)

        editTextTitle = v.findViewById(R.id.knitting_title)
        editTextDescription = v.findViewById(R.id.knitting_description)
        textViewStarted = v.findViewById(R.id.knitting_started)
        // we need to set this in code because older android versions (API <21) do not support vector drawables with drawableLeft
        textViewStarted.setCompoundDrawablesWithIntrinsicBounds(AppCompatResources.getDrawable(inflater.context, R.drawable.ic_date_range_black_24dp), null, null, null)
        textViewFinished = v.findViewById(R.id.knitting_finished)
        // we need to set this in code because older android versions (API <21) do not support vector drawables with drawableLeft
        textViewFinished.setCompoundDrawablesWithIntrinsicBounds(AppCompatResources.getDrawable(inflater.context, R.drawable.ic_date_range_black_24dp), null, null, null)
        editTextNeedleDiameter = v.findViewById(R.id.knitting_needle_diameter)
        editTextSize = v.findViewById(R.id.knitting_size)
        textViewDuration = v.findViewById(R.id.knitting_duration)
        buttonCategory = v.findViewById(R.id.knitting_category)
        spinnerStatus = v.findViewById(R.id.knitting_status)
        spinnerStatus.adapter = StatusAdapter(requireContext())
        ratingBar = v.findViewById(R.id.ratingBar)

        if (savedInstanceState == null) {
            val knitting = if (knittingID != -1L) datasource.getKnitting(knittingID) else Knitting()
            editTextTitle.setText(knitting.title)
            editTextDescription.setText(knitting.description)
            started = knitting.started
            finished = knitting.finished
            editTextNeedleDiameter.setText(knitting.needleDiameter)
            editTextSize.setText(knitting.size)
            duration = knitting.duration
            category = knitting.category
            val statusList = Status.formattedValues(requireContext())
            val index = statusList.indexOf(Status.format(requireContext(), knitting.status))
            if (index >= 0) {
                spinnerStatus.setSelection(index)
            } else {
                spinnerStatus.setSelection(0)
            }
            ratingBar.rating = knitting.rating.toFloat()
        }
        textViewStarted.text = DateFormat.getDateInstance().format(started)
        textViewFinished.text = if (finished != null) DateFormat.getDateInstance().format(finished) else ""
        textViewDuration.text = TimeUtils.formatDuration(duration)
        category?.let {
            buttonCategory.text = it.name
        }

        textViewStarted.setOnClickListener {
            val dialog = DatePickerFragment.newInstance(started)
            dialog.setTargetFragment(this, REQUEST_STARTED)
            dialog.show(parentFragmentManager, DIALOG_DATE)
        }
        textViewFinished.setOnClickListener {
            val dialog = DatePickerFragment.newInstance(if (finished != null) finished else Date())
            dialog.setTargetFragment(this, REQUEST_FINISHED)
            dialog.show(parentFragmentManager, DIALOG_DATE)
        }
        textViewDuration.setOnClickListener {
            val d = DurationPickerDialog(requireContext(), { durationPicker, d ->
                textViewDuration.text = TimeUtils.formatDuration(d)
                duration = d
            }, duration)
            d.show()
        }
        buttonCategory.setOnClickListener {
            val i = Intent(context, SelectCategoryActivity::class.java)
            // add the knitting ID which is required to make up navigation work correctly
            i.putExtra(EXTRA_KNITTING_ID, knittingID)
            startActivityForResult(i, REQUEST_SELECT_CATEGORY)
        }

        // update knitting if user changes the rating
        ratingBar = v.findViewById(R.id.ratingBar)

        return v
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
                val oldKnitting = if (knittingID != -1L) datasource.getKnitting(knittingID) else null
                val newKnitting = if (oldKnitting != null) createKnitting().copy(defaultPhoto = oldKnitting.defaultPhoto) else createKnitting()
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
        if (requestCode == REQUEST_STARTED) {
            val date = data!!.getSerializableExtra(DatePickerFragment.EXTRA_DATE) as Date
            if (date != started) {
                textViewStarted.text = DateFormat.getDateInstance().format(date)
                started = date
            }
        } else if (requestCode == REQUEST_FINISHED) {
            val date = data!!.getSerializableExtra(DatePickerFragment.EXTRA_DATE) as Date
            if (date != finished) {
                textViewFinished.text = DateFormat.getDateInstance().format(date)
                finished = date
            }
        } else if (requestCode == REQUEST_SELECT_CATEGORY) {
            data?.let {
                val categoryID = it.getLongExtra(EXTRA_CATEGORY_ID, -1L)
                if (categoryID != -1L) {
                    val c = datasource.getCategory(categoryID)
                    buttonCategory.text = c.name
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
                val oldKnitting = if (knittingID != -1L) datasource.getKnitting(knittingID) else null
                val newKnitting = if (oldKnitting != null) createKnitting().copy(defaultPhoto = oldKnitting.defaultPhoto) else createKnitting()
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
        val status = Status.values()[spinnerStatus.selectedItemPosition]
        return Knitting(knittingID, editTextTitle.text.toString(), editTextDescription.text.toString(), started, finished, editTextNeedleDiameter.text.toString(),
        editTextSize.text.toString(), null, ratingBar.rating.toDouble(), duration, category, status)
    }

    private fun saveKnitting(knitting: Knitting) {
        if (knitting.id == -1L) {
            datasource.addKnitting(knitting)
        } else {
            datasource.updateKnitting(knitting)
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
