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
import com.mthaler.knittings.model.NeedleType
import kotlinx.android.synthetic.main.fragment_needle_list.*

class NeedleListFragment : Fragment() {

    private var listener: OnFragmentInteractionListener? = null
    private lateinit var viewModel: NeedleListViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v = inflater.inflate(R.layout.fragment_needle_list, container, false)

        setHasOptionsMenu(true)

        val fab = v.findViewById<FloatingActionButton>(R.id.fab_create_needle)
        fab.setOnClickListener { v -> listener?.createNeedle() }

        return v
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val rv = requireView().findViewById<RecyclerView>(R.id.needles_recycler_view)
        rv.layoutManager = LinearLayoutManager(context)

        val adapter = NeedleAdapter({ needle -> listener?.needleClicked(needle.id) }, { needle ->
            (activity as AppCompatActivity).startSupportActionMode(object : ActionMode.Callback {

                override fun onActionItemClicked(mode: ActionMode?, menu: MenuItem?): Boolean {
                    when (menu?.itemId) {
                        R.id.action_delete -> {
                            this@NeedleListFragment.activity?.let {
                                DeleteDialog.create(it, needle.name) {
                                    datasource.deleteNeedle(needle)
                                }.show()
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

        viewModel = ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().application)).get(NeedleListViewModel::class.java)
        viewModel.needles.observe(viewLifecycleOwner, Observer { needles ->
            // show image if category list is empty
            if (needles.isEmpty()) {
                needles_empty_recycler_view.visibility = View.VISIBLE
                needles_recycler_view.visibility = View.GONE
            } else {
                needles_empty_recycler_view.visibility = View.GONE
                needles_recycler_view.visibility = View.VISIBLE
            }
            val items = NeedleAdapter.groupItems(requireContext(), needles)
            adapter.setItems(items)
        })
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.needle_list, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_item_filter -> {
                val types = NeedleType.formattedValues(requireContext())
                val listItems = (listOf(getString(R.string.filter_show_all)) + types).toTypedArray()
                val builder = AlertDialog.Builder(requireContext())
                val f = viewModel.filter
                val checkedItem = when (f) {
                    is NoFilter -> 0
                    is SingleTypeFilter -> {
                        val index = NeedleType.values().indexOf(f.type)
                        index + 1
                    }
                    else -> throw Exception("Unknown filter: $f")
                }
                builder.setSingleChoiceItems(listItems, checkedItem) { dialog, which -> when (which) {
                    0 -> viewModel.filter = NoFilter
                    else -> {
                        val type = NeedleType.values()[which - 1]
                        viewModel.filter = SingleTypeFilter(type)
                    }
                }
                    dialog.dismiss()
                }
                builder.setNegativeButton(R.string.dialog_button_cancel) { dialog, which -> dialog.dismiss() }
                val dialog = builder.create()
                dialog.show()
                true
            }
            else -> super.onOptionsItemSelected(item)
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

    interface OnFragmentInteractionListener {

        fun createNeedle()

        fun needleClicked(needleID: Long)
    }
}
