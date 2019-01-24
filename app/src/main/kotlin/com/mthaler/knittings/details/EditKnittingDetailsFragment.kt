package com.mthaler.knittings.details

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.text.Editable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.RatingBar
import android.widget.TextView
import com.mthaler.knittings.R
import com.mthaler.knittings.TextWatcher
import com.mthaler.knittings.category.SelectCategoryActivity
import com.mthaler.knittings.database.KnittingsDataSource
import com.mthaler.knittings.database.datasource
import com.mthaler.knittings.datepicker.DatePickerFragment
import com.mthaler.knittings.model.Knitting
import com.mthaler.knittings.utils.TimeUtils
import java.text.DateFormat
import java.util.*
import com.mthaler.knittings.durationpicker.DurationPickerDialog

class EditKnittingDetailsFragment : Fragment() {

    private var knitting: Knitting? = null

    private lateinit var textViewStarted: TextView
    private lateinit var textViewFinished: TextView
    private lateinit var textViewDuration: TextView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val v = inflater.inflate(R.layout.fragment_edit_knitting_details, container, false)

        val editTextTitle = v.findViewById<EditText>(R.id.knitting_title)
        editTextTitle.addTextChangedListener(createTextWatcher { c, knitting -> knitting.copy(title = c.toString()) })

        val editTextDescription = v.findViewById<EditText>(R.id.knitting_description)
        editTextDescription.addTextChangedListener(createTextWatcher{ c, knitting -> knitting.copy(description = c.toString()) })

        textViewStarted = v.findViewById(R.id.knitting_started)
        textViewStarted.setOnClickListener {
            activity?.let {
                val fm = it.supportFragmentManager
                val dialog = DatePickerFragment.newInstance(knitting!!.started)
                dialog.setTargetFragment(this, REQUEST_STARTED)
                dialog.show(fm, DIALOG_DATE)
            }
        }

        textViewFinished = v.findViewById(R.id.knitting_finished)
        textViewFinished.setOnClickListener {
            activity?.let {
                val fm = it.supportFragmentManager
                val dialog = DatePickerFragment.newInstance(if (knitting!!.finished != null) knitting!!.finished else Date())
                dialog.setTargetFragment(this, REQUEST_FINISHED)
                dialog.show(fm, DIALOG_DATE)
            }
        }

        val editTextNeedleDiameter = v.findViewById<EditText>(R.id.knitting_needle_diameter)
        editTextNeedleDiameter.addTextChangedListener(createTextWatcher { c, knitting -> knitting.copy(needleDiameter = c.toString()) })

        val editTextSize = v.findViewById<EditText>(R.id.knitting_size)
        editTextSize.addTextChangedListener(createTextWatcher { c, knitting -> knitting.copy(size = c.toString()) })

        textViewDuration = v.findViewById(R.id.knitting_duration)
        textViewDuration.setOnClickListener {
            val mTimePicker = DurationPickerDialog(this.context!!, { durationPicker, duration ->
                textViewDuration.text = TimeUtils.formatDuration(duration)
                val knitting0 = knitting!!
                val knitting1 = knitting0.copy(duration = duration)
                knitting = knitting1
                datasource.updateKnitting(knitting1)
            }, knitting!!.duration)
            mTimePicker.show()
        }

        val buttonCategory = v.findViewById<Button>(R.id.knitting_category)
        buttonCategory.setOnClickListener {
            val i = Intent(context, SelectCategoryActivity::class.java)
            startActivityForResult(i, REQUEST_SELECT_CATEGORY)
//            val categories = datasource.allCategories
//            val builder = AlertDialog.Builder(this.context!!)
//            builder.setTitle("Select category")
//            builder.setItems(categories.map { it.name }.toTypedArray()) { dialog, which ->
//                val c = categories[which]
//                buttonCategory.text = c.name
//                knitting?.let {
//                    val k = it.copy(category = c)
//                    knitting = k
//                    datasource.updateKnitting(k)
//                }
//
//            }
//            builder.show()
        }

        // update knitting if user changes the rating
        val ratingBar = v.findViewById<RatingBar>(R.id.ratingBar)
        ratingBar.onRatingBarChangeListener = RatingBar.OnRatingBarChangeListener { ratingBar, rating, fromUser ->
            knitting?.let {
                val k = it.copy(rating = rating.toDouble())
                datasource.updateKnitting(k)
                knitting = k
            }
        }

        return v
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

            knitting = knitting?.copy(finished = date)
            textViewFinished.text = DateFormat.getDateInstance().format(date)
            KnittingsDataSource.getInstance(activity!!).updateKnitting(knitting!!)
        }
    }

    fun init(knitting: Knitting) {
        val v = view
        if (v != null) {
            val editTextTitle = v.findViewById<EditText>(R.id.knitting_title)
            editTextTitle.setText(knitting.title)

            val editTextDescription = v.findViewById<EditText>(R.id.knitting_description)
            editTextDescription.setText(knitting.description)

            textViewStarted.text = DateFormat.getDateInstance().format(knitting.started)

            textViewFinished.text = if (knitting.finished != null) DateFormat.getDateInstance().format(knitting.finished) else ""

            val editTextNeedleDiameter = v.findViewById<EditText>(R.id.knitting_needle_diameter)
            editTextNeedleDiameter.setText(knitting.needleDiameter)

            val editTextSize = v.findViewById<EditText>(R.id.knitting_size)
            editTextSize.setText(knitting.size)

            textViewDuration.text = TimeUtils.formatDuration(knitting.duration)

            if (knitting.category != null) {
                val buttonCategory = v.findViewById<Button>(R.id.knitting_category)
                buttonCategory.text = knitting.category.name
            }



            val ratingBar = v.findViewById<RatingBar>(R.id.ratingBar)
            ratingBar.rating = knitting.rating.toFloat()

            this.knitting = knitting
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
                    } catch(ex: Exception) {
                    }

                }
            }
        }
    }

    fun getKnittingID(): Long? = knitting?.id

    companion object {
        private const val DIALOG_DATE = "date"
        private const val REQUEST_STARTED = 0
        private const val REQUEST_FINISHED = 1
        private const val REQUEST_SELECT_CATEGORY = 2
    }
}
