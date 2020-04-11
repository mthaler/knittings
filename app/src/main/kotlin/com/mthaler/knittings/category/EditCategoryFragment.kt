package com.mthaler.knittings.category

import android.os.Bundle
import android.view.*
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.mthaler.knittings.DeleteDialog
import com.mthaler.knittings.R
import com.mthaler.knittings.Extras.EXTRA_CATEGORY_ID
import com.mthaler.knittings.SaveChangesDialog
import com.mthaler.knittings.TextWatcher
import com.mthaler.knittings.model.Category
import petrov.kristiyan.colorpicker.ColorPicker

class EditCategoryFragment : Fragment() {

    private var categoryID: Long = -1
    private lateinit var viewModel: EditCategoryViewModel
    private lateinit var editTextTitle: EditText
    private lateinit var buttonColor: Button
    private var color = 0
    private var moddified = false

    fun getCategoryID(): Long = categoryID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            categoryID = it.getLong(EXTRA_CATEGORY_ID)
        }

        // Retain this fragment across configuration changes.
        retainInstance = true
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        setHasOptionsMenu(true)

        // Inflate the layout for this fragment
        val v = inflater.inflate(R.layout.fragment_edit_category, container, false)

        // set edit text title text to category name
        editTextTitle = v.findViewById(R.id.category_name)
        editTextTitle.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(c: CharSequence, start: Int, before: Int, count: Int) {
                moddified = true
            }
        })

        // set background color of the button to category color if it is defined
        buttonColor = v.findViewById<Button>(R.id.button_select_color)
        buttonColor.setOnClickListener { view ->
            val colorPicker = ColorPicker(activity)
            colorPicker.setOnFastChooseColorListener(object : ColorPicker.OnFastChooseColorListener {
                override fun setOnFastChooseColorListener(position: Int, c: Int) {
                    color = c
                    buttonColor.setBackgroundColor(c)
                    moddified = true
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

        return v
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory.getInstance(activity!!.application)).get(EditCategoryViewModel::class.java)
        viewModel.init(categoryID)
        viewModel.category.observe(viewLifecycleOwner, Observer { category ->
            editTextTitle.setText(category.name)
            if (category.color != null) {
                color = category.color
                buttonColor.setBackgroundColor(category.color)
            }
            moddified = false
        })
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.edit_category, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_item_save_category -> {
                if (moddified) {
                    viewModel.saveCategory(Category(categoryID, editTextTitle.text.toString(), color))
                }
                fragmentManager?.popBackStack()
                true
            }
            R.id.menu_item_delete_category -> {
                context?.let {
                    DeleteDialog.create(it, editTextTitle.text.toString(), {
                        viewModel.deleteCategory()
                        fragmentManager?.popBackStack()
                    }).show()
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    fun onBackPressed() {
        context?.let {
            if (moddified) {
                SaveChangesDialog.create(it, {
                    viewModel.saveCategory(Category(categoryID, editTextTitle.text.toString(), color))
                    fragmentManager?.popBackStack()
                }, {
                    fragmentManager?.popBackStack()
                }).show()
            } else {
                fragmentManager?.popBackStack()
            }
        }
    }

    companion object {

        @JvmStatic
        fun newInstance(categoryID: Long) =
                EditCategoryFragment().apply {
                    arguments = Bundle().apply {
                        putLong(EXTRA_CATEGORY_ID, categoryID)
                    }
                }
    }
}
