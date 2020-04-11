package com.mthaler.knittings.details

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.appcompat.content.res.AppCompatResources
import android.text.Editable
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
    private lateinit var textViewStarted: TextView
    private lateinit var textViewFinished: TextView
    private lateinit var textViewDuration: TextView
    private lateinit var buttonCategory: Button
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

        val editTextTitle = v.findViewById<EditText>(R.id.knitting_title)
        editTextTitle.addTextChangedListener(createTextWatcher { c, knitting -> knitting.copy(title = c.toString()) })

        val editTextDescription = v.findViewById<EditText>(R.id.knitting_description)
        editTextDescription.addTextChangedListener(createTextWatcher { c, knitting -> knitting.copy(description = c.toString()) })

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

        val editTextNeedleDiameter = v.findViewById<EditText>(R.id.knitting_needle_diameter)
        editTextNeedleDiameter.addTextChangedListener(createTextWatcher { c, knitting -> knitting.copy(needleDiameter = c.toString()) })

        val editTextSize = v.findViewById<EditText>(R.id.knitting_size)
        editTextSize.addTextChangedListener(createTextWatcher { c, knitting -> knitting.copy(size = c.toString()) })

        textViewDuration = v.findViewById(R.id.knitting_duration)
        textViewDuration.setOnClickListener {
            context?.let {
                val d = DurationPickerDialog(it, { durationPicker, duration ->
                    textViewDuration.text = TimeUtils.formatDuration(duration)
                    knitting = knitting.copy(duration = duration)
                    datasource.updateKnitting(knitting)
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

        val spinnerStatus = v.findViewById<Spinner>(R.id.knitting_status)
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
                parent?.let {
                    val k0 = knitting
                    if (k0 != null) {
                        val statusStr = statusList[position]
                        val status = Status.parse(it.context, statusStr)
                        val k1 = k0.copy(status = status)
                        datasource.updateKnitting(k1)
                        knitting = k1
                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }

        // update knitting if user changes the rating
        val ratingBar = v.findViewById<RatingBar>(R.id.ratingBar)
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
        viewModel.knitting.observe(viewLifecycleOwner, Observer { needle ->
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
            }
        } else if (requestCode == REQUEST_FINISHED) {
            val date = data!!.getSerializableExtra(DatePickerFragment.EXTRA_DATE) as Date
            val knitting0 = knitting
            if (knitting0 != null) {
                val knitting1 = knitting0.copy(finished = date)
                knitting = knitting1
                datasource.updateKnitting(knitting1)
                textViewFinished.text = DateFormat.getDateInstance().format(date)
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

    override fun onResume() {
        super.onResume()
        knitting = datasource.getKnitting(knitting.id)
        updateDetails()
    }

    private fun updateDetails() {
        view?.let {
            val editTextTitle = it.findViewById<EditText>(R.id.knitting_title)
            editTextTitle.setText(knitting.title)

            val editTextDescription = it.findViewById<EditText>(R.id.knitting_description)
            editTextDescription.setText(knitting.description)

            textViewStarted.text = DateFormat.getDateInstance().format(knitting.started)

            textViewFinished.text = if (knitting.finished != null) DateFormat.getDateInstance().format(knitting.finished) else ""

            val editTextNeedleDiameter = it.findViewById<EditText>(R.id.knitting_needle_diameter)
            editTextNeedleDiameter.setText(knitting.needleDiameter)

            val editTextSize = it.findViewById<EditText>(R.id.knitting_size)
            editTextSize.setText(knitting.size)

            textViewDuration.text = TimeUtils.formatDuration(knitting.duration)

            val c = knitting.category
            if (c != null) {
                val buttonCategory = it.findViewById<Button>(R.id.knitting_category)
                buttonCategory.text = c.name
            }

            val statusList = Status.formattedValues(it.context)
            val spinnerStatus = it.findViewById<Spinner>(R.id.knitting_status)
            val index = statusList.indexOf(Status.format(it.context, knitting.status))
            if (index >= 0) {
                spinnerStatus.setSelection(index)
            } else {
                spinnerStatus.setSelection(0)
            }

            val ratingBar = it.findViewById<RatingBar>(R.id.ratingBar)
            ratingBar.rating = knitting.rating.toFloat()
        }
    }

    private fun createTextWatcher(updateKnitting: (CharSequence, Knitting) -> Knitting): TextWatcher {
        return object : TextWatcher {
            override fun afterTextChanged(c: Editable) {
                val knitting0 = knitting
                if (knitting0 != null) {
                    try {
                        val knitting1 = updateKnitting(c, knitting0)
                        datasource.updateKnitting(knitting1)
                        knitting = knitting1
                    } catch (ex: Exception) {
                    }
                }
            }
        }
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
