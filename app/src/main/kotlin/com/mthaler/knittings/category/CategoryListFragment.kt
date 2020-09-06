package com.mthaler.knittings.category

import android.content.Context
import android.os.Bundle
import android.view.*
import com.google.android.material.floatingactionbutton.FloatingActionButton
import androidx.fragment.app.Fragment
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ActionMode
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.mthaler.dbapp.category.CategoryAdapter
import com.mthaler.dbapp.database.CategoryRepository
import com.mthaler.knittings.DeleteDialog
import com.mthaler.knittings.Extras
import com.mthaler.knittings.R
import kotlinx.android.synthetic.main.fragment_category_list.*

class CategoryListFragment : Fragment() {

    private var knittingID: Long = -1
    private var listener: OnFragmentInteractionListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            knittingID = it.getLong(Extras.EXTRA_KNITTING_ID)
        }

        retainInstance = true
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.fragment_category_list, container, false)

        val fab = v.findViewById<FloatingActionButton>(R.id.fab_create_category)
        fab.setOnClickListener { listener?.createCategory() }

        return v
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val rv = requireView().findViewById<RecyclerView>(R.id.category_recycler_view)
        rv.layoutManager = LinearLayoutManager(context)
        val adapter = CategoryAdapter({ category -> listener?.categoryClicked(category.id) }, { category ->
            (activity as AppCompatActivity).startSupportActionMode(object : ActionMode.Callback {

                override fun onActionItemClicked(mode: ActionMode?, menu: MenuItem?): Boolean {
                    when (menu?.itemId) {
                        R.id.action_delete -> {
                            this@CategoryListFragment.activity?.let {
                                DeleteDialog.create(it, category.name) {
                                    CategoryRepository.deleteCategory(category)
                                }.show()
                            }
                            mode?.finish()
                            return true
                        }
                        R.id.action_copy -> {
                            val newName = "${category.name} - ${getString(R.string.copy)}"
                            val categoryCopy = category.copy(name = newName)
                            CategoryRepository.addCategory(categoryCopy)
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
        rv.adapter = adapter

        val viewModel = ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().application)).get(CategoryListViewModel::class.java)
        viewModel.categories.observe(viewLifecycleOwner, Observer { categories ->
            // show image if category list is empty
            if (categories.isEmpty()) {
                category_empty_recycler_view.visibility = View.VISIBLE
                category_nonempty_recycler_view.visibility = View.GONE
                category_recycler_view.visibility = View.GONE
            } else {
                category_empty_recycler_view.visibility = View.GONE
                category_nonempty_recycler_view.visibility = View.VISIBLE
                category_recycler_view.visibility = View.VISIBLE
            }
            adapter.setCategories(categories)
        })
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

        fun createCategory()

        fun categoryClicked(categoryID: Long)
    }

    companion object {

        @JvmStatic
        fun newInstance(knittingID: Long) =
            CategoryListFragment().apply {
                arguments = Bundle().apply {
                    putLong(Extras.EXTRA_KNITTING_ID, knittingID)
                }
            }
    }
}
