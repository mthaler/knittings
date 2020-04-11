package com.mthaler.knittings.details

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.appcompat.content.res.AppCompatResources
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.mthaler.knittings.R
import com.mthaler.knittings.TextWatcher
import com.mthaler.knittings.category.SelectCategoryActivity
import com.mthaler.knittings.database.datasource
import com.mthaler.knittings.datepicker.DatePickerFragment
import com.mthaler.knittings.model.Knitting
import com.mthaler.knittings.utils.TimeUtils
import java.text.DateFormat
import com.mthaler.knittings.durationpicker.DurationPickerDialog
import com.mthaler.knittings.Extras.EXTRA_CATEGORY_ID
import com.mthaler.knittings.Extras.EXTRA_KNITTING_ID
import com.mthaler.knittings.model.Status
import java.util.Date

class EditKnittingDetailsFragment : Fragment() {

    private lateinit var knitting: Knitting
    private var knittingID: Long = -1
    private lateinit var viewModel: EditKnittingDetailsViewModel
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
    private lateinit var spinnerStatus: Spinner
    private lateinit var ratingBar: RatingBar
    private var modified = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            knittingID = it.getLong(EXTRA_KNITTING_ID)
            knitting = datasource.getKnitting(knittingID)
        }

        // Retain this fragment across configuration changes.
        retainInstance = true
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val v = inflater.inflate(R.layout.fragment_edit_knitting_details, container, false)

        editTextTitle = v.findViewById(R.id.knitting_title)
        editTextTitle.addTextChangedListener(createTextWatcher())

        editTextDescription = v.findViewById(R.id.knitting_description)
        editTextDescription.addTextChangedListener(createTextWatcher())

        textViewStarted = v.findViewById(R.id.knitting_started)
        // we need to set this in code because older android versions (API <21) do not support vector drawables with drawableLeft
        textViewStarted.setCompoundDrawablesWithIntrinsicBounds(AppCompatResources.getDrawable(inflater.context, R.drawable.ic_date_range_black_24dp), null, null, null)
        textViewStarted.setOnClickListener {
            fragmentManager?.let {
                val dialog = DatePickerFragment.newInstance(knitting.started)
                dialog.setTargetFragment(this, REQUEST_STARTED)
                dialog.show(it, DIALOG_DATE)
            }
        }

        textViewFinished = v.findViewById(R.id.knitting_finished)
        // we need to set this in code because older android versions (API <21) do not support vector drawables with drawableLeft
        textViewFinished.setCompoundDrawablesWithIntrinsicBounds(AppCompatResources.getDrawable(inflater.context, R.drawable.ic_date_range_black_24dp), null, null, null)
        textViewFinished.setOnClickListener {
            fragmentManager?.let {
                val dialog = DatePickerFragment.newInstance(if (knitting.finished != null) knitting.finished else Date())
                dialog.setTargetFragment(this, REQUEST_FINISHED)
                dialog.show(it, DIALOG_DATE)
            }
        }

        editTextNeedleDiameter = v.findViewById(R.id.knitting_needle_diameter)
        editTextNeedleDiameter.addTextChangedListener(createTextWatcher())

        editTextSize = v.findViewById(R.id.knitting_size)
        editTextSize.addTextChangedListener(createTextWatcher())

        textViewDuration = v.findViewById(R.id.knitting_duration)
        textViewDuration.setOnClickListener {
            context?.let {
                val d = DurationPickerDialog(it, { durationPicker, d ->
                    textViewDuration.text = TimeUtils.formatDuration(d)
                    duration = d
                }, knitting.duration)
                d.show()
            }
        }

        buttonCategory = v.findViewById(R.id.knitting_category)
        buttonCategory.setOnClickListener {
            val i = Intent(context, SelectCategoryActivity::class.java)
            // add the knitting ID which is required to make up navigation work correctly
            i.putExtra(EXTRA_KNITTING_ID, knitting.id)
            startActivityForResult(i, REQUEST_SELECT_CATEGORY)
        }

        spinnerStatus = v.findViewById(R.id.knitting_status)
        val statusList = Status.formattedValues(v.context)
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter(context, android.R.layout.simple_spinner_item, statusList).also { adapter ->
            // Specify the layout to use when the list of choices appears
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            // Apply the adapter to the spinner
            spinnerStatus.adapter = adapter
        }
        spinnerStatus.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                modified = true
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }

        // update knitting if user changes the rating
        ratingBar = v.findViewById(R.id.ratingBar)
        ratingBar.onRatingBarChangeListener = RatingBar.OnRatingBarChangeListener { ratingBar, rating, fromUser ->
            knitting = knitting.copy(rating = rating.toDouble())
            datasource.updateKnitting(knitting)
        }

        return v
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory.getInstance(activity!!.application)).get(EditKnittingDetailsViewModel::class.java)
        viewModel.init(knittingID)
        viewModel.knitting.observe(viewLifecycleOwner, Observer { knitting ->
            editTextTitle.setText(knitting.title)
            editTextDescription.setText(knitting.description)
            textViewStarted.text = DateFormat.getDateInstance().format(knitting.started)
            started = knitting.started
            textViewFinished.text = if (knitting.finished != null) DateFormat.getDateInstance().format(knitting.finished) else ""
            finished = knitting.finished
            editTextNeedleDiameter.setText(knitting.needleDiameter)
            editTextSize.setText(knitting.size)
            textViewDuration.text = TimeUtils.formatDuration(knitting.duration)
            val c = knitting.category
            if (c != null) {
                buttonCategory.text = c.name
            }
            val statusList = Status.formattedValues(context!!)
            val index = statusList.indexOf(Status.format(context!!, knitting.status))
            if (index >= 0) {
                spinnerStatus.setSelection(index)
            } else {
                spinnerStatus.setSelection(0)
            }
            ratingBar.rating = knitting.rating.toFloat()
            modified = false
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != Activity.RESULT_OK) {
            return
        }
        if (requestCode == REQUEST_STARTED) {
            val date = data!!.getSerializableExtra(DatePickerFragment.EXTRA_DATE) as Date
            val knitting0 = knitting
            if (knitting0 != null) {
                val knitting1 = knitting0.copy(started = date)
                knitting = knitting1
                datasource.updateKnitting(knitting1)
                textViewStarted.text = DateFormat.getDateInstance().format(date)
                started = date
            }
        } else if (requestCode == REQUEST_FINISHED) {
            val date = data!!.getSerializableExtra(DatePickerFragment.EXTRA_DATE) as Date
            val knitting0 = knitting
            if (knitting0 != null) {
                val knitting1 = knitting0.copy(finished = date)
                knitting = knitting1
                datasource.updateKnitting(knitting1)
                textViewFinished.text = DateFormat.getDateInstance().format(date)
                finished = date
            }

            knitting = knitting.copy(finished = date)
            textViewFinished.text = DateFormat.getDateInstance().format(date)
            datasource.updateKnitting(knitting)
        } else if (requestCode == REQUEST_SELECT_CATEGORY) {
            data?.let {
                val categoryID = it.getLongExtra(EXTRA_CATEGORY_ID, -1L)
                if (categoryID != -1L) {
                    val c = datasource.getCategory(categoryID)
                    buttonCategory.text = c.name
                    knitting = knitting.copy(category = c)
                    datasource.updateKnitting(knitting)
                }
            }
        }
    }

    private fun createTextWatcher(): TextWatcher {
        return object : TextWatcher {
            override fun onTextChanged(c: CharSequence, start: Int, before: Int, count: Int) {
                modified = true
            }
        }
    }

    private fun createKnitting(): Knitting {
        val status = Status.values()[spinnerStatus.selectedItemPosition]
        return Knitting(knittingID, editTextTitle.text.toString(), editTextDescription.text.toString(), started, finished, editTextNeedleDiameter.text.toString(),
        editTextSize.toString(), null, ratingBar.rating.toDouble(), duration, null, status)
    }

    companion object {

        @JvmStatic
        fun newInstance(knittingID: Long) =
            EditKnittingDetailsFragment().apply {
                arguments = Bundle().apply {
                    putLong(EXTRA_KNITTING_ID, knittingID)
                }
            }

        private const val DIALOG_DATE = "date"
        private const val REQUEST_STARTED = 0
        private const val REQUEST_FINISHED = 1
        private const val REQUEST_SELECT_CATEGORY = 2
    }
}
