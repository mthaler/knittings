package com.mthaler.knittings.needle

import android.content.Context
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v4.app.Fragment
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.view.ActionMode
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.*
import com.mthaler.knittings.R
import com.mthaler.knittings.database.datasource
import com.mthaler.knittings.model.NeedleType
import kotlinx.android.synthetic.main.fragment_needle_list.*

class NeedleListFragment : Fragment() {

    private var listener: OnFragmentInteractionListener? = null
    private var filter: Filter = NoFilter

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

        retainInstance = true
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val v = inflater.inflate(R.layout.fragment_needle_list, container, false)

        setHasOptionsMenu(true)

        // add new needle if the user clicks the floating action button and start the
        // EditNeedleActivity to edit the new category
        val fab = v.findViewById<FloatingActionButton>(R.id.fab_create_needle)
        fab.setOnClickListener { v -> listener?.createNeedle() }

        val rv = v.findViewById<RecyclerView>(R.id.needles_recycler_view)
        rv.layoutManager = LinearLayoutManager(context)

        return v
    }

    /**
     * Initialize the contents of the Fragment host's standard options menu. You should place your menu items in to menu.
     * For this method to be called, you must have first called setHasOptionsMenu(boolean).
     * See Activity.onCreateOptionsMenu for more information.
     *
     * @param menu The options menu in which you place your items.
     * @param inflater MenuInflater
     */
    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        super.onCreateOptionsMenu(menu, inflater)
        if (menu != null && inflater != null) {
            inflater.inflate(R.menu.needle_list, menu)
        }
    }

    /**
     * This hook is called whenever an item in your options menu is selected.
     *
     * @param item the menu item that was selected.
     * @return return false to allow normal menu processing to proceed, true to consume it here.
     */
    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        val c = context
        if (item != null && c != null) {
            return when (item.itemId) {
                R.id.menu_item_filter -> {
                    context?.let {
                        val needles = datasource.allNeedles
                        val types = NeedleType.formattedValues(c)
                        val listItems = (listOf(getString(R.string.filter_show_all)) + types).toTypedArray()
                        val builder = AlertDialog.Builder(it)
                        val f = filter
                        val checkedItem = when (f) {
                            is NoFilter -> 0
                            is SingleTypeFilter -> {
                                val index = NeedleType.values().indexOf(f.type)
                                index + 1
                            }
                            else -> throw Exception("Unknown filter: $f")
                        }
                        builder.setSingleChoiceItems(listItems, checkedItem) { dialog, which -> when(which) {
                            0 -> filter = NoFilter
                            else -> {
                                val type = NeedleType.values()[which - 1]
                                filter = SingleTypeFilter(type)
                            }
                        }
                            updateNeedleList()
                            dialog.dismiss()
                        }
                        builder.setNegativeButton(R.string.dialog_button_cancel) { dialog, which -> dialog.dismiss() }
                        val dialog = builder.create()
                        dialog.show()
                    }
                    true
                }
                else -> super.onOptionsItemSelected(item)
            }
        } else {
            return super.onOptionsItemSelected(item)
        }
    }

    override fun onResume() {
        super.onResume()
        updateNeedleList()
    }

    /**
    * Updates the list of needles
    */
    private fun updateNeedleList() {
        view?.let {
            val rv = it.findViewById<RecyclerView>(R.id.needles_recycler_view)
            val needles = datasource.allNeedles
            val filtered = filter.filter(needles)
            // show image if category list is empty
            if (needles.isEmpty()) {
                needles_empty_recycler_view.visibility = View.VISIBLE
                needles_recycler_view.visibility = View.GONE
            } else {
                needles_empty_recycler_view.visibility = View.GONE
                needles_recycler_view.visibility = View.VISIBLE
            }
            // start EditCategoryActivity if the users clicks on a category
            val adapter = NeedleAdapter(NeedleAdapter.groupItems(it.context, filtered), { needle -> listener?.needleClicked(needle.id) },
                    { needle ->
                        (activity as AppCompatActivity).startSupportActionMode(object : ActionMode.Callback {

                            /**
                             * Called to report a user click on an action button.
                             *
                             * @param mode The current ActionMode
                             * @param menu The item that was clicked
                             * @return true if this callback handled the event, false if the standard MenuItem invocation should continue.
                             */
                            override fun onActionItemClicked(mode: ActionMode?, menu: MenuItem?): Boolean {
                                when(menu?.itemId) {
                                    R.id.action_delete -> {
                                        this@NeedleListFragment.activity?.let {
                                            DeleteNeedleDialog.create(it, needle, {
                                                datasource.deleteNeedle(needle)
                                                updateNeedleList()
                                            }).show()
                                        }
                                        mode?.finish()
                                        return true
                                    }
                                    R.id.action_copy -> {
                                        mode?.finish()
                                        return true
                                    }
                                    else -> {
                                        return false
                                    }
                                }
                            }

                            /**
                             * Called when action mode is first created. The menu supplied will be used to generate action buttons for the action mode.
                             *
                             * @param mode The current ActionMode
                             * @param menu  Menu used to populate action buttons
                             * @return true if the action mode should be created, false if entering this mode should be aborted.
                             */
                            override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
                                val inflater = mode?.getMenuInflater()
                                inflater?.inflate(R.menu.needle_list_action, menu)
                                return true
                            }

                            /**
                             * Called to refresh an action mode's action menu whenever it is invalidated.
                             *
                             * @param mode The current ActionMode
                             * @param menu  Menu used to populate action buttons
                             */
                            override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
                                return true
                            }

                            /**
                             * Called when an action mode is about to be exited and destroyed.
                             *
                             * @param mode The current ActionMode
                             */
                            override fun onDestroyActionMode(mode: ActionMode?) {
                            }
                        })
                    })
            rv.adapter = adapter
        }
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
            throw RuntimeException("$context must implement OnFragmentInteractionListener")
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
