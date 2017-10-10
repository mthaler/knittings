package com.mthaler.knittings

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.RatingBar
import android.widget.TextView
import com.mthaler.knittings.database.KnittingsDataSource
import com.mthaler.knittings.model.Knitting
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.debug
import java.text.DateFormat
import java.util.Date

/**
 * KnittingFragment shows a single knitting
 *
 * It is used for adding new knittings or displaying / editing existing knittings
 */
class KnittingFragment : Fragment(), AnkoLogger {

    private var knitting: Knitting? = null

    private lateinit var textViewStarted: TextView
    private lateinit var textViewFinished: TextView

    override fun onCreateView(inflater: LayoutInflater, parent: ViewGroup?, savedInstanceState: Bundle?): View? {
        // create view
        val v = inflater.inflate(R.layout.fragment_knitting, parent, false)

        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(KNITTING_ID)) {
                knitting = KnittingsDataSource.getInstance(activity).getKnitting(savedInstanceState.getLong(KNITTING_ID))
                debug("Set knitting: " + knitting)
            }
        } else {
            val knittingID = arguments.getLong(KNITTING_ID)
            knitting = KnittingsDataSource.getInstance(activity).getKnitting(knittingID)
            debug("Set knitting: " + knitting)
        }

        // initialize title text field
        val editTextTitle = v.findViewById<EditText>(R.id.knitting_title)
        editTextTitle.setText(knitting!!.title)
        editTextTitle.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(c: CharSequence, start: Int, before: Int, count: Int) {
                knitting = knitting?.copy(title = c.toString())
                KnittingsDataSource.getInstance(activity).updateKnitting(knitting!!)
            }

            override fun beforeTextChanged(c: CharSequence, start: Int, count: Int, after: Int) {
                // this space intentionally left blank
            }

            override fun afterTextChanged(c: Editable) {
                // this one too
            }
        })

        // initialize description text field
        val editTextDescription = v.findViewById<EditText>(R.id.knitting_description)
        editTextDescription.setText(knitting!!.description)
        editTextDescription.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(c: CharSequence, start: Int, before: Int, count: Int) {
                knitting = knitting?.copy(description = c.toString())
                KnittingsDataSource.getInstance(activity).updateKnitting(knitting!!)
            }

            override fun beforeTextChanged(c: CharSequence, start: Int, count: Int, after: Int) {
                // this space intentionally left blank
            }

            override fun afterTextChanged(c: Editable) {
                // this one too
            }
        })

        textViewStarted = v.findViewById(R.id.knitting_started)
        textViewStarted.text = DateFormat.getDateInstance().format(knitting!!.started)
        textViewStarted.setOnClickListener {
            val fm = activity.supportFragmentManager
            val dialog = DatePickerFragment.newInstance(knitting!!.started)
            dialog.setTargetFragment(this@KnittingFragment, REQUEST_STARTED)
            dialog.show(fm, DIALOG_DATE)
        }

        // initialize finish date button
        textViewFinished = v.findViewById(R.id.knitting_finished)
        textViewFinished.text = if (knitting!!.finished != null) DateFormat.getDateInstance().format(knitting!!.finished) else ""
        textViewFinished.setOnClickListener {
            val fm = activity.supportFragmentManager
            val dialog = DatePickerFragment.newInstance(if (knitting!!.finished != null) knitting!!.finished else Date())
            dialog.setTargetFragment(this@KnittingFragment, REQUEST_FINISHED)
            dialog.show(fm, DIALOG_DATE)
        }

        val editTextNeedleDiameter = v.findViewById<EditText>(R.id.knitting_needle_diameter)
        editTextNeedleDiameter.setText(java.lang.Double.toString(knitting!!.needleDiameter))
        editTextNeedleDiameter.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(c: CharSequence, start: Int, before: Int, count: Int) {
                try {
                    knitting = knitting?.copy(needleDiameter = java.lang.Double.parseDouble(c.toString()))
                } catch (ex: Exception) {
                    knitting = knitting?.copy(needleDiameter = 0.0)
                }
                KnittingsDataSource.getInstance(activity).updateKnitting(knitting!!)
            }

            override fun beforeTextChanged(c: CharSequence, start: Int, count: Int, after: Int) {
                // this space intentionally left blank
            }

            override fun afterTextChanged(c: Editable) {
                try {
                    knitting = knitting?.copy(needleDiameter = java.lang.Double.parseDouble(c.toString()))
                } catch (ex: Exception) {
                    knitting = knitting?.copy(needleDiameter = 0.0)
                }
                KnittingsDataSource.getInstance(activity).updateKnitting(knitting!!)
            }
        })

        val editTextSize = v.findViewById<EditText>(R.id.knitting_size)
        editTextSize.setText(java.lang.Double.toString(knitting!!.size))
        editTextSize.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(c: CharSequence, start: Int, before: Int, count: Int) {
                try {
                    knitting = knitting?.copy(size = java.lang.Double.parseDouble(c.toString()))
                } catch (ex: Exception) {
                    knitting = knitting?.copy(size = 0.0)
                }
                KnittingsDataSource.getInstance(activity).updateKnitting(knitting!!)
            }

            override fun beforeTextChanged(c: CharSequence, start: Int, count: Int, after: Int) {
                // this space intentionally left blank
            }

            override fun afterTextChanged(c: Editable) {
                try {
                    knitting = knitting?.copy(size = java.lang.Double.parseDouble(c.toString()))
                } catch (ex: Exception) {
                    knitting = knitting?.copy(size = 0.0)
                }
                KnittingsDataSource.getInstance(activity).updateKnitting(knitting!!)
            }
        })

        val ratingBar = v.findViewById<RatingBar>(R.id.ratingBar)
        ratingBar.rating = knitting!!.rating.toFloat()
        ratingBar.onRatingBarChangeListener = RatingBar.OnRatingBarChangeListener { ratingBar, rating, fromUser ->
            knitting = knitting?.copy(rating = rating.toDouble())
            KnittingsDataSource.getInstance(activity).updateKnitting(knitting!!)
        }

        return v
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode != Activity.RESULT_OK) {
            return
        }
        if (requestCode == REQUEST_STARTED) {
            val date = data!!.getSerializableExtra(DatePickerFragment.EXTRA_DATE) as Date
            knitting = knitting?.copy(started = date)
            textViewStarted.text = DateFormat.getDateInstance().format(date)
            KnittingsDataSource.getInstance(activity).updateKnitting(knitting!!)
        } else if (requestCode == REQUEST_FINISHED) {
            val date = data!!.getSerializableExtra(DatePickerFragment.EXTRA_DATE) as Date
            knitting = knitting?.copy(finished = date)
            textViewFinished.text = DateFormat.getDateInstance().format(date)
            KnittingsDataSource.getInstance(activity).updateKnitting(knitting!!)
        }
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        if (knitting != null) {
            outState!!.putLong(KNITTING_ID, knitting!!.id)
        }
        super.onSaveInstanceState(outState)
    }

    /**
     * Deletes the displayed knitting
     *
     * The method will delete the knitting from the database and also remove the photo if it exists
     */
    fun deleteKnitting() {
        // delete all photos from the database
        KnittingsDataSource.getInstance(activity).deleteAllPhotos(knitting!!)
        // delete database entry
        KnittingsDataSource.getInstance(activity).deleteKnitting(knitting!!)
        knitting = null
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        if (knitting != null) {
            if (isVisibleToUser) {
                // the fragment became visible because the user selected it in the view pager
                // get current knitting from database
                knitting = KnittingsDataSource.getInstance(activity).getKnitting(knitting!!.id)
            } else {
                // the fragment became invisible because the user selected another tab in the view pager
                // save current knitting to database
                KnittingsDataSource.getInstance(activity).updateKnitting(knitting!!)
            }
        }
    }

    companion object: AnkoLogger {

        private val KNITTING_ID = "knitting_id"

        private val DIALOG_DATE = "date"
        private val REQUEST_STARTED = 0
        private val REQUEST_FINISHED = 1

        fun newInstance(knitting: Knitting): KnittingFragment {
            val fragment = KnittingFragment()
            val args = Bundle()
            args.putLong(KNITTING_ID, knitting.id)
            fragment.arguments = args
            debug("Created new KnittingFragment with knitting id: " + knitting.id)
            return fragment
        }
    }
}
