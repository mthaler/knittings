package com.mthaler.knittings.needle

import android.content.Context
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.mthaler.knittings.R
import com.mthaler.knittings.database.datasource
import kotlinx.android.synthetic.main.fragment_needle_list.*

class NeedleListFragment : Fragment() {

    private var listener: OnFragmentInteractionListener? = null

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

        setRetainInstance(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val v = inflater.inflate(R.layout.fragment_needle_list, container, false)

        // add new needle if the user clicks the floating action button and start the
        // EditNeedleActivity to edit the new category
        val fab = v.findViewById<FloatingActionButton>(R.id.fab_create_needle)
        fab.setOnClickListener { v -> listener?.createNeedle() }

        val rv = v.findViewById<RecyclerView>(R.id.needles_recycler_view)
        rv.layoutManager = LinearLayoutManager(context)

        return v
    }

    override fun onResume() {
        super.onResume()
        updateNeedleList()
    }

    /**
    * Updates the list of needles
    */
    private fun updateNeedleList() {
        val rv = view!!.findViewById<RecyclerView>(R.id.needles_recycler_view)
        val needles = datasource.allNeedles
        // show image if category list is empty
        if (needles.isEmpty()) {
            needles_empty_recycler_view.visibility = View.VISIBLE
            needles_recycler_view.visibility = View.GONE
        } else {
            needles_empty_recycler_view.visibility = View.GONE
            needles_recycler_view.visibility = View.VISIBLE
        }
        // start EditCategoryActivity if the users clicks on a category
        val adapter = NeedleAdapter(needles, { needle ->
            listener?.needleClicked(needle.id)
        })
        rv.adapter = adapter
    }

    /**
     * Called when a fragment is first attached to its context. onCreate(Bundle) will be called after this.
     *
     * @param context Context
     */
    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnFragmentInteractionListener) {
            listener = context
        } else {
            throw RuntimeException(context.toString() + " must implement OnFragmentInteractionListener")
        }
    }

    /**
     * Called when the fragment is no longer attached to its activity. This is called after onDestroy().
     */
    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     */
    interface OnFragmentInteractionListener {

        /**
         * Called if the user clicks the floating action button to create a category
         */
        fun createNeedle()

        /**
         * Called if the user clicks a needle in the list
         *
         * @param needleID needle ID
         */
        fun needleClicked(needleID: Long)
    }
}
