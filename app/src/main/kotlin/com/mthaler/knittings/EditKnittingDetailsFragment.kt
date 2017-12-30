package com.mthaler.knittings

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import com.mthaler.knittings.database.datasource
import com.mthaler.knittings.model.Knitting
import java.text.DateFormat
import java.util.*

class EditKnittingDetailsFragment : Fragment() {

    private var knitting: Knitting? = null

    private lateinit var textViewStarted: TextView
    private lateinit var textViewFinished: TextView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val v = inflater.inflate(R.layout.fragment_edit_knitting_details, container, false)

        val editTextTitle = v.findViewById<EditText>(R.id.knitting_title)
        editTextTitle.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(c: CharSequence, start: Int, before: Int, count: Int) {
                val knitting0 = knitting
                if (knitting0 != null) {
                    val knitting1 = knitting0.copy(title = c.toString())
                    datasource.updateKnitting(knitting1)
                }
            }
        })

        val editTextDescription = v.findViewById<EditText>(R.id.knitting_description)
        editTextDescription.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(c: CharSequence, start: Int, before: Int, count: Int) {
                val knitting0 = knitting
                if (knitting0 != null) {
                    val knitting1 = knitting0.copy(description = c.toString())
                    datasource.updateKnitting(knitting1)
                }
            }
        })

        textViewStarted = v.findViewById(R.id.knitting_started)
        textViewStarted.setOnClickListener {
            val fm = activity!!.supportFragmentManager
            val dialog = DatePickerFragment.newInstance(knitting!!.started)
            dialog.setTargetFragment(this, EditKnittingDetailsFragment.REQUEST_STARTED)
            dialog.show(fm, EditKnittingDetailsFragment.DIALOG_DATE)
        }

        textViewFinished = v.findViewById(R.id.knitting_finished)
        textViewFinished.setOnClickListener {
            val fm = activity!!.supportFragmentManager
            val dialog = DatePickerFragment.newInstance(if (knitting!!.finished != null) knitting!!.finished else Date())
            dialog.setTargetFragment(this, REQUEST_FINISHED)
            dialog.show(fm, DIALOG_DATE)
        }


        return v
    }

    fun init(knitting: Knitting) {
        val v = view
        if (v != null) {
            val editTextTitle = v.findViewById<EditText>(R.id.knitting_title)
            editTextTitle.setText(knitting.title)

            val editTextDescription = v.findViewById<EditText>(R.id.knitting_description)
            editTextDescription.setText(knitting.description)

            textViewStarted.text = DateFormat.getDateInstance().format(knitting.started)

            textViewFinished.text = if (knitting!!.finished != null) DateFormat.getDateInstance().format(knitting!!.finished) else ""
        }
    }

    companion object {
        private val KNITTING_ID = "knitting_id"

        private val DIALOG_DATE = "date"
        private val REQUEST_STARTED = 0
        private val REQUEST_FINISHED = 1
    }
}
