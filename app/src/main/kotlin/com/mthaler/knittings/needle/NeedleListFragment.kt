package com.mthaler.knittings.needle

import android.content.Context
import android.os.Bundle
import com.google.android.material.floatingactionbutton.FloatingActionButton
import androidx.fragment.app.Fragment
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ActionMode
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.*
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.mthaler.knittings.DeleteDialog
import com.mthaler.knittings.R
import com.mthaler.knittings.database.datasource
import com.mthaler.knittings.model.Needle
import com.mthaler.knittings.model.NeedleType
import kotlinx.android.synthetic.main.fragment_needle_list.*

class NeedleListFragment : Fragment() {

    private var listener: OnFragmentInteractionListener? = null
    private var filter: Filter = NoFilter
    private lateinit var viewModel: NeedleListViewModel

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

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory.getInstance(activity!!.application)).get(NeedleListViewModel::class.java)
        viewModel.needles.observe(viewLifecycleOwner, Observer { needles ->
            updateNeedleList(needles)
        })
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.needle_list, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_item_filter -> {
                context?.let {
                    val needles = datasource.allNeedles
                    val types = NeedleType.formattedValues(it)
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
                        updateNeedleList(viewModel.needles.value ?: emptyList<Needle>())
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
    }

    private fun updateNeedleList(needles: List<Needle>) {
        view?.let {
            val rv = it.findViewById<RecyclerView>(R.id.needles_recycler_view)
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

                            override fun onActionItemClicked(mode: ActionMode?, menu: MenuItem?): Boolean {
                                when(menu?.itemId) {
                                    R.id.action_delete -> {
                                        this@NeedleListFragment.activity?.let {
                                            DeleteDialog.create(it, needle.name, {
                                                datasource.deleteNeedle(needle)
                                            }).show()
                                        }
                                        mode?.finish()
                                        return true
                                    }
                                    R.id.action_copy -> {
                                        val newName = "${needle.name} - ${getString(R.string.copy)}"
                                        val needleCopy = needle.copy(name = newName)
                                        datasource.addNeedle(needleCopy)
                                        mode?.finish()
                                        return true
                                    }
                                    else -> {
                                        return false
                                    }
                                }
                            }

                            override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
                                val inflater = mode?.menuInflater
                                inflater?.inflate(R.menu.needle_list_action, menu)
                                return true
                            }

                            override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean = true

                            override fun onDestroyActionMode(mode: ActionMode?) {}
                        })
                    })
            rv.adapter = adapter
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnFragmentInteractionListener) {
            listener = context
        } else {
            throw RuntimeException("$context must implement OnFragmentInteractionListener")
        }
    }

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
