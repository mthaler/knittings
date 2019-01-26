package com.mthaler.knittings.category

import android.content.Intent
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.mthaler.knittings.Extras
import com.mthaler.knittings.Extras.EXTRA_KNITTING_ID
import com.mthaler.knittings.R
import com.mthaler.knittings.database.datasource

class SelectCategoryFragment : Fragment() {

    private var knittingID: Long = -1

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
        }
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
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val v = inflater.inflate(R.layout.fragment_select_category, container, false)

        // add new category if the user clicks the floating action button and start the
        // EditCategoryActivity to edit the new category
        val fab = v.findViewById<FloatingActionButton>(R.id.fab_create_category)
        fab.setOnClickListener { view ->
            val category = datasource.createCategory("", null)
            val i = Intent(context, EditCategoryActivity::class.java)
            i.putExtra(Extras.EXTRA_CATEGORY_ID, category.id)
            startActivityForResult(i, REQUEST_EDIT_CATEGORY)
        }

        val rv = v.findViewById<RecyclerView>(R.id.category_recycler_view)
        rv.layoutManager = LinearLayoutManager(context)

        return v
    }


    companion object {

        /**
         * Use this factory method to create a new instance of this fragment using the provided parameters.
         *
         * @param categoryID id of the category that should be edited
         * @return A new instance of fragment EditCategoryFragment.
         */
        @JvmStatic
        fun newInstance(knittingID: Long) =
            SelectCategoryFragment().apply {
                arguments = Bundle().apply {
                    putLong(EXTRA_KNITTING_ID, knittingID)
                }
            }

        private const val REQUEST_EDIT_CATEGORY = 0
    }
}
