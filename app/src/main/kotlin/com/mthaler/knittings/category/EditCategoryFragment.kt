package com.mthaler.knittings.category

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import com.mthaler.knittings.*
import com.mthaler.knittings.color.ColorPicker
import com.mthaler.knittings.database.Extras.EXTRA_CATEGORY_ID
import com.mthaler.knittings.database.CategoryDataSource
import com.mthaler.knittings.database.KnittingsDataSource
import com.mthaler.knittings.databinding.FragmentEditCategoryBinding
import com.mthaler.knittings.model.Category

class EditCategoryFragment : Fragment() {

    private var categoryID: Long = Category.EMPTY.id
    private var _binding: FragmentEditCategoryBinding? = null
    private val binding get() = _binding!!
    private var color: Int? = null
    private lateinit var ds: CategoryDataSource

    fun getCategoryID(): Long = categoryID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ds = KnittingsDataSource
        arguments?.let {
            categoryID = it.getLong(EXTRA_CATEGORY_ID)
        }
        savedInstanceState?.let {
            if (it.containsKey(EXTRA_CATEGORY_ID)) {
                categoryID = it.getLong(EXTRA_CATEGORY_ID)
            }
            if (it.containsKey(EXTRA_COLOR)) {
                color = it.getInt(EXTRA_COLOR)
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {

        setHasOptionsMenu(true)

        val category = if (categoryID != Category.EMPTY.id) ds.getCategory(categoryID) else Category()

        _binding = FragmentEditCategoryBinding.inflate(inflater, container, false)
        val view = binding.root

        if (savedInstanceState == null) {
            binding.categoryName.setText(category?.name ?: "")
            val c = category?.color
            if (c != null) {
                binding.buttonSelectColor.setBackgroundColor(c)
                color = c
            }
        } else {
            color?.let { binding.buttonSelectColor.setBackgroundColor(it) }
        }

        binding.buttonSelectColor.setOnClickListener { showColorPicker() }

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onSaveInstanceState(savedInstanceState: Bundle) {
        savedInstanceState.putLong(EXTRA_CATEGORY_ID, categoryID)
        color?.let { savedInstanceState.putInt(EXTRA_COLOR, it) }
        super.onSaveInstanceState(savedInstanceState)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.edit_category, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_item_save_category -> {
                val oldCategory = if (categoryID != Category.EMPTY.id) ds.getCategory(categoryID) else Category.EMPTY
                val newCategory = Category(categoryID, binding.categoryName.text.toString(), color)
                if (newCategory != oldCategory) {
                    saveCategory(newCategory)
                }
                categorySaved(categoryID)
                true
            }
            R.id.menu_item_delete_category -> {
                if (categoryID == Category.EMPTY.id) {
                    DiscardChangesDialog.create(requireContext()) {
                        parentFragmentManager.popBackStack()
                    }.show()
                } else {
                    DeleteDialog.create(requireContext(), binding.categoryName.text.toString()) {
                        deleteCategory()
                        parentFragmentManager.popBackStack()
                    }.show()
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    fun onBackPressed(action: (Long) -> Unit) {
        val oldCategory = if (categoryID != Category.EMPTY.id) ds.getCategory(categoryID) else Category.EMPTY
        val newCategory = Category(categoryID, binding.categoryName.text.toString(), color)
        if (newCategory != oldCategory) {
            SaveChangesDialog.create(requireContext(), {
                saveCategory(newCategory)
                action(categoryID)
            }, {
                action(categoryID)
            }).show()
        } else {
            action(categoryID)
        }
    }

    private fun showColorPicker() {
        val colorPicker = ColorPicker(activity)
        colorPicker.setOnFastChooseColorListener(object : ColorPicker.OnFastChooseColorListener {
            override fun setOnFastChooseColorListener(position: Int, c: Int) {
                color = c
                binding.buttonSelectColor.setBackgroundColor(c)
            }

            override fun onCancel() {
            }
        })
        colorPicker.setTitle(resources.getString(R.string.category_color_dialog_title))
        colorPicker.setColors(R.array.category_colors)
        colorPicker.setRoundColorButton(true)
        colorPicker.setColorButtonMargin(6, 6, 6, 6)
        colorPicker.setColumns(4)
        colorPicker.show()
    }

    private fun deleteCategory() {
        val category = ds.getCategory(categoryID)
        if (category != null) {
            ds.deleteCategory(category)
        }
    }

    private fun saveCategory(category: Category) {
        if (category.id == Category.EMPTY.id) {
            val result = ds.addCategory(category)
            categoryID = result.id
        } else {
            ds.updateCategory(category)
        }
    }

    private fun categorySaved(categoryID: Long) {
        requireActivity().supportFragmentManager.popBackStack()
    }

//    private fun categorySaved(categoryID: Long) {
//        if (categoryID == Category.EMPTY.id) {
//            requireActivity().finish()
//        } else {
//            val i = Intent()
//            i.putExtra(Extras.EXTRA_OWNER_ID, ownerID)
//            i.putExtra(Extras.EXTRA_CATEGORY_ID, categoryID)
//            setResult(Activity.RESULT_OK, i)
//            finish()
//        }
//    }

    companion object {

        private const val EXTRA_COLOR = "com.mthaler.knittings.category.COLOR"

        @JvmStatic
        fun newInstance(categoryID: Long) =
            EditCategoryFragment().apply {
                arguments = Bundle().apply {
                    putLong(EXTRA_CATEGORY_ID, categoryID)
                }
            }
    }
}