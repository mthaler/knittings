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

    /**
     * Called to do initial creation of a fragment. This is called after onAttach(Activity) and before
     * onCreateView(LayoutInflater, ViewGroup, Bundle). Note that this can be called while the fragment's activity
     * is still in the process of being created. As such, you can not rely on things like the activity's content view
     * hierarchy being initialized at this point. If you want to do work once the activity itself is created,
     * see onActivityCreated(Bundle).
     *
     * Any restored child fragments will be created before the base Fragment.onCreate method returns.
     *
     * @param savedInstanceState If the fragment is being re-created from a previous saved state, this is the state.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            knittingID = it.getLong(EXTRA_KNITTING_ID)
            knitting = datasource.getKnitting(knittingID)
        }

        // Retain this fragment across configuration changes.
        retainInstance = true
    }

    /**
     * Called to have the fragment instantiate its user interface view. This is optional, and non-graphical
     * fragments can return null (which is the default implementation). This will be called between onCreate(Bundle)
     * and onActivityCreated(Bundle).
     *
     * If you return a View from here, you will later be called in onDestroyView() when the view is being released.
     *
     * @param inflater the LayoutInflater object that can be used to inflate any views in the fragment
     * @param container If non-null, this is the parent view that the fragment's UI should be attached to.
     *                  The fragment should not add the view itself, but this can be used to generate the LayoutParams of the view.
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous saved state as given here.
     */
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

            /**
             * Callback method to be invoked when an item in this view has been selected. This callback is invoked only when the
             * newly selected position is different from the previously selected position or if there was no selected item.
             *
             * Implementers can call getItemAtPosition(position) if they need to access the data associated with the selected item.
             *
             * @param parent the AdapterView where the selection happened
             * @param view the view within the AdapterView that was clicked
             * @param position the position of the view in the adapter
             * @param id the row id of the item that is selected
             */
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

            /**
             * Callback method to be invoked when the selection disappears from this view. The selection can disappear
             * for instance when touch is activated or when the adapter becomes empty.
             *
             * @param parent the AdapterView where the selection happened
             */
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

    /**
     * Called when an activity you launched exits, giving you the requestCode you started it with, the resultCode it returned,
     * and any additional data from it. The resultCode will be RESULT_CANCELED if the activity explicitly returned that,
     * didn't return any result, or crashed during its operation.
     *
     * @param requestCode The integer request code originally supplied to startActivityForResult(), allowing you to identify who this result came from.
     * @param resultCode The integer result code returned by the child activity through its setResult().
     * @param data An Intent, which can return result data to the caller (various data can be attached to Intent "extras").
     */
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

    /**
     * Called when the fragment is visible to the user and actively running. This is generally tied to Activity.onResume of the containing Activity's lifecycle.
     *
     * If you override this method you must call through to the superclass implementation.
     */
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

    /**
     * Creates a textwatcher that updates the knitting using the given update function
     *
     * @param updateKnitting function to updated the knitting
     */
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

        /**
         * Use this factory method to create a new instance of this fragment using the provided parameters.
         *
         * @param knittingID id of the knitting project that should be edited
         * @return A new instance of fragment EditKnittingDetailsFragment
         */
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
