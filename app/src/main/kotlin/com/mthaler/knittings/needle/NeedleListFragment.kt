package com.mthaler.knittings.needle

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ActionMode
import androidx.recyclerview.widget.LinearLayoutManager
import android.view.*
import androidx.lifecycle.ViewModelProvider
import com.mthaler.dbapp.DeleteDialog
import com.mthaler.knittings.R
import com.mthaler.knittings.database.KnittingsDataSource
import com.mthaler.knittings.databinding.FragmentNeedleListBinding
import com.mthaler.knittings.model.NeedleType

class NeedleListFragment : Fragment() {

    private var _binding: FragmentNeedleListBinding? = null
    private val binding get() = _binding!!
    private var listener: OnFragmentInteractionListener? = null
    private lateinit var viewModel: NeedleListViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNeedleListBinding.inflate(inflater, container, false)
        val view = binding.root
        setHasOptionsMenu(true)
        binding.fabCreateNeedle.setOnClickListener { v -> listener?.createNeedle() }
        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        binding.needlesRecyclerView.layoutManager = LinearLayoutManager(context)

        val adapter = NeedleAdapter({ needle -> listener?.needleClicked(needle.id) }, { needle ->
            (activity as AppCompatActivity).startSupportActionMode(object : ActionMode.Callback {

                override fun onActionItemClicked(mode: ActionMode?, menu: MenuItem?): Boolean {
                    when (menu?.itemId) {
                        R.id.action_delete -> {
                            this@NeedleListFragment.activity?.let {
                                DeleteDialog.create(it, needle.name) {
                                    KnittingsDataSource.deleteNeedle(needle)
                                }.show()
                            }
                            mode?.finish()
                            return true
                        }
                        R.id.action_copy -> {
                            val newName = "${needle.name} - ${getString(R.string.copy)}"
                            val needleCopy = needle.copy(name = newName)
                            KnittingsDataSource.addNeedle(needleCopy)
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
        binding.needlesRecyclerView.adapter = adapter

        viewModel = ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().application)).get(NeedleListViewModel::class.java)
        viewModel.needles.observe(viewLifecycleOwner, { needles ->
            // show image if category list is empty
            if (needles.isEmpty()) {
                binding.needlesEmptyRecyclerView.visibility = View.VISIBLE
                binding.needlesRecyclerView.visibility = View.GONE
            } else {
                binding.needlesEmptyRecyclerView.visibility = View.GONE
                binding.needlesRecyclerView.visibility = View.VISIBLE
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
                val checkedItem = when (val f = viewModel.filter) {
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
