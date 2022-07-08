package com.mthaler.knittings.needle

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ActionMode
import androidx.recyclerview.widget.LinearLayoutManager
import com.mthaler.knittings.DeleteDialog
import com.mthaler.knittings.filter.CombinedFilter
import com.mthaler.knittings.R
import com.mthaler.knittings.database.KnittingsDataSource
import com.mthaler.knittings.databinding.FragmentNeedleListBinding
import com.mthaler.knittings.model.NeedleType
import com.mthaler.knittings.needle.filter.InUseFilter
import com.mthaler.knittings.needle.filter.SingleTypeFilter
import androidx.lifecycle.ViewModelProvider
import android.view.*
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.mthaler.knittings.model.Needle

class NeedleListFragment : Fragment() {

    private var _binding: FragmentNeedleListBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: NeedleListViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNeedleListBinding.inflate(inflater, container, false)
        val view = binding.root
        setHasOptionsMenu(true)
        binding.fabCreateNeedle.setOnClickListener {
            val navController = findNavController()
            createNeedle(navController)
        }
        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.needlesRecyclerView.layoutManager = LinearLayoutManager(context)

        val adapter = NeedleAdapter({ needle -> needleClicked(needle.id) }, { needle ->
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

        val activeFilters = binding.knittingActiveFilters

        viewModel = ViewModelProvider(requireActivity()).get(NeedleListViewModel::class.java)
        viewModel.needles.observe(viewLifecycleOwner, { needles ->
            // show image if category list is empty
            if (needles.isEmpty()) {
                binding.needlesEmptyRecyclerView.visibility = View.VISIBLE
                binding.needlesRecyclerView.visibility = View.GONE
            } else {
                binding.needlesEmptyRecyclerView.visibility = View.GONE
                binding.needlesRecyclerView.visibility = View.VISIBLE
            }
            if (viewModel.filter.filters.filter { it is SingleTypeFilter || it is InUseFilter }.isEmpty()) {
                activeFilters.text = ""
                activeFilters.visibility = View.GONE
            } else {
                val sb = StringBuilder()
                sb.append(resources.getString(R.string.active_filters))
                sb.append(": ")
                val hasTypeFilter = viewModel.filter.filters.filter { it is SingleTypeFilter }.isNotEmpty()
                val hasInUseFilter = viewModel.filter.filters.filter { it is InUseFilter }.isNotEmpty()
                if (hasTypeFilter) {
                    sb.append(resources.getString(R.string.needle_filter_type))
                }
                if (hasTypeFilter && hasInUseFilter) {
                    sb.append(", ")
                }
                if (hasInUseFilter) {
                    sb.append(resources.getString(R.string.needle_filter_in_use))
                }
                activeFilters.text = sb.toString()
                activeFilters.visibility = View.VISIBLE
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
            R.id.menu_item_clear_filters -> {
                viewModel.filter = CombinedFilter.empty()
                true
            }
            R.id.menu_item_type_filter -> {
                val types = NeedleType.formattedValues(requireContext())
                val listItems = (listOf(getString(R.string.filter_show_all)) + types).toTypedArray()
                val builder = AlertDialog.Builder(requireContext())
                val f = viewModel.filter
                val checkedItem: Int = let {
                    val result = f.filters.find { it is SingleTypeFilter }
                    if (result != null && result is SingleTypeFilter) {
                        val index = NeedleType.values().indexOf(result.type)
                        index + 1
                    } else {
                        0
                    }
                }
                builder.setSingleChoiceItems(listItems, checkedItem) { dialog, which -> when (which) {
                    0 -> viewModel.filter = CombinedFilter(f.filters.filterNot { it is SingleTypeFilter })
                    else -> {
                        val type = NeedleType.values()[which - 1]
                        val newFilter = SingleTypeFilter(type)
                        viewModel.filter = CombinedFilter(f.filters.filterNot { it is SingleTypeFilter } + newFilter)
                    }
                }
                    dialog.dismiss()
                }
                builder.setNegativeButton(R.string.dialog_button_cancel) { dialog, _ -> dialog.dismiss() }
                val dialog = builder.create()
                dialog.show()
                true
            }
            R.id.menu_item_in_use_filter -> {
                val listItems = (listOf(getString(R.string.filter_show_all)) + getString(R.string.needle_filter_in_use) + getString(R.string.needle_filter_not_in_use)).toTypedArray()
                val builder = AlertDialog.Builder(requireContext())
                val f = viewModel.filter
                val checkedItem: Int = let {
                    val result = f.filters.find { it is InUseFilter }
                    if (result != null && result is InUseFilter) {
                        if (result.inUse) {
                            1
                        } else {
                            2
                        }
                    } else {
                        0
                    }
                }
                builder.setSingleChoiceItems(listItems, checkedItem) { dialog, which -> when (which) {
                    0 -> viewModel.filter = CombinedFilter(f.filters.filterNot { it is InUseFilter })
                    1 -> viewModel.filter = CombinedFilter(f.filters.filterNot { it is InUseFilter} + InUseFilter(true))
                    else -> viewModel.filter = CombinedFilter(f.filters.filterNot { it is InUseFilter} + InUseFilter(false))
                }
                    dialog.dismiss()
                }
                builder.setNegativeButton(R.string.dialog_button_cancel) { dialog, _ -> dialog.dismiss() }
                val dialog = builder.create()
                dialog.show()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun createNeedle(navController: NavController) {
        val f = EditNeedleFragment.newInstance(Needle.EMPTY.id)
        val ft = requireActivity().supportFragmentManager.beginTransaction()
        ft.replace(R.id.needle_list_container, f)
        ft.addToBackStack(null)
        ft.commit()
    }

    private fun needleClicked(needleID: Long) {
        val f = EditNeedleFragment.newInstance(needleID)
        val ft = requireActivity().supportFragmentManager.beginTransaction()
        ft.replace(R.id.needle_list_container, f)
        ft.addToBackStack(null)
        ft.commit()
    }
}