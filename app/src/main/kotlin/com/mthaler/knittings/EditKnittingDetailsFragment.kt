package com.mthaler.knittings

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.mthaler.knittings.database.datasource
import com.mthaler.knittings.model.Knitting
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.debug


class EditKnittingDetailsFragment : Fragment(), AnkoLogger {

    private var knitting: Knitting? = null

    private lateinit var textViewStarted: TextView
    private lateinit var textViewFinished: TextView

    override fun onCreateView(inflater: LayoutInflater, parent: ViewGroup?, savedInstanceState: Bundle?): View? {
        // create view
        val v = inflater.inflate(R.layout.fragment_edit_knitting_details, parent, false)

        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(EditKnittingDetailsFragment.KNITTING_ID)) {
                knitting = datasource.getKnitting(savedInstanceState.getLong(EditKnittingDetailsFragment.KNITTING_ID))
                debug("Set knitting: " + knitting)
            }
        } else {
            val knittingID = arguments!!.getLong(EditKnittingDetailsFragment.KNITTING_ID)
            knitting = datasource.getKnitting(knittingID)
            debug("Set knitting: " + knitting)
        }

        return v
    }

    companion object: AnkoLogger {

        private val KNITTING_ID = "knitting_id"

        private val DIALOG_DATE = "date"
        private val REQUEST_STARTED = 0
        private val REQUEST_FINISHED = 1
    }
}
