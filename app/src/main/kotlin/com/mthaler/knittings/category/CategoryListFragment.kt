package com.mthaler.knittings.category

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ActionMode
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.mthaler.knittings.DatabaseApplication
import com.mthaler.knittings.DeleteDialog
import com.mthaler.knittings.R
import com.mthaler.knittings.database.CategoryDataSource
import com.mthaler.knittings.database.KnittingsDataSource
import com.mthaler.knittings.databinding.FragmentCategoryListBinding
import com.mthaler.knittings.model.Category

class CategoryListFragment : Fragment() {

    private var _binding: FragmentCategoryListBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentCategoryListBinding.inflate(inflater, container, false)
        val view = binding.root

        binding.fabCreateCategory.setOnClickListener { createCategory() }

        val appSettings = (requireContext().applicationContext as DatabaseApplication).getApplicationSettings()
        val emptyListBackground = appSettings.emptyCategoryListBackground()
        if (emptyListBackground != -1) {
            binding.categoryEmptyRecyclerView.setImageResource(emptyListBackground)
        }

        val listBackground = appSettings.categoryListBackground()
        if (listBackground != -1) {
            binding.categoryNonemptyRecyclerView.setImageResource(listBackground)
        }

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val ds = KnittingsDataSource as CategoryDataSource

        binding.categoryRecyclerView.layoutManager = LinearLayoutManager(context)
        val adapter = CategoryAdapter({ category -> categoryClicked(category.id) }, { category ->
            (activity as AppCompatActivity).startSupportActionMode(object : ActionMode.Callback {

                override fun onActionItemClicked(mode: ActionMode?, menu: MenuItem?): Boolean {
                    when (menu?.itemId) {
                        R.id.action_delete -> {
                            this@CategoryListFragment.activity?.let {
                                DeleteDialog.create(it, category.name) {
                                    ds.deleteCategory(category)
                                }.show()
                            }
                            mode?.finish()
                            return true
                        }
                        R.id.action_copy -> {
                            val newName = "${category.name} - ${getString(R.string.copy)}"
                            val categoryCopy = category.copy(name = newName)
                            ds.addCategory(categoryCopy)
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
                    inflater?.inflate(R.menu.category_list_action, menu)
                    return true
                }

                override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean = true

                override fun onDestroyActionMode(mode: ActionMode?) {
                }
            })
        })
        binding.categoryRecyclerView.adapter = adapter

        val viewModel = ViewModelProvider(requireActivity()).get(CategoryListViewModel::class.java)
        viewModel.categories.observe(viewLifecycleOwner, { categories ->
            // show image if category list is empty
            if (categories.isEmpty()) {
                binding.categoryEmptyRecyclerView.visibility = View.VISIBLE
                binding.categoryNonemptyRecyclerView.visibility = View.GONE
                binding.categoryRecyclerView.visibility = View.GONE
            } else {
                binding.categoryEmptyRecyclerView.visibility = View.GONE
                binding.categoryNonemptyRecyclerView.visibility = View.VISIBLE
                binding.categoryRecyclerView.visibility = View.VISIBLE
            }
            adapter.setCategories(categories)
        })
    }

    private fun createCategory() {
        val f = EditCategoryFragment.newInstance(Category.EMPTY.id)
        val ft = requireActivity().supportFragmentManager.beginTransaction()
        ft.replace(R.id.category_list_container, f)
        ft.addToBackStack(null)
        ft.commit()
    }

    private fun categoryClicked(categoryID: Long) {
        val f = EditCategoryFragment.newInstance(categoryID, -1)
        val ft = requireActivity().supportFragmentManager.beginTransaction()
        ft.replace(R.id.category_list_container, f)
        ft.addToBackStack(null)
        ft.commit()
    }

    companion object {

        @JvmStatic
        fun newInstance() = CategoryListFragment()
    }
}
