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
import com.mthaler.knittings.DeleteDialog
import com.mthaler.knittings.Extras
import com.mthaler.knittings.R
import com.mthaler.knittings.database.datasource
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
        // Inflate the layout for this fragment
        val v = inflater.inflate(R.layout.fragment_category_list, container, false)

        // add new category if the user clicks the floating action button and start the
        // EditCategoryActivity to edit the new category
        val fab = v.findViewById<FloatingActionButton>(R.id.fab_create_category)
        fab.setOnClickListener { listener?.createCategory() }

        return v
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val rv = requireView().findViewById<RecyclerView>(R.id.category_recycler_view)
        rv.layoutManager = LinearLayoutManager(context)
        val adapter = CategoryAdapter({ category ->
            listener?.categoryClicked(category.id)
        }, { category ->
            (activity as AppCompatActivity).startSupportActionMode(object : ActionMode.Callback {

                /**
                 * Called to report a user click on an action button.
                 *
                 * @param mode The current ActionMode
                 * @param menu The item that was clicked
                 * @return true if this callback handled the event, false if the standard MenuItem invocation should continue.
                 */
                override fun onActionItemClicked(mode: ActionMode?, menu: MenuItem?): Boolean {
                    when (menu?.itemId) {
                        R.id.action_delete -> {
                            this@CategoryListFragment.activity?.let {
                                DeleteDialog.create(it, category.name, {
                                    datasource.deleteCategory(category)
                                }).show()
                            }
                            mode?.finish()
                            return true
                        }
                        R.id.action_copy -> {
                            val newName = "${category.name} - ${getString(R.string.copy)}"
                            val categoryCopy = category.copy(name = newName)
                            datasource.addCategory(categoryCopy)
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
                 * @param menu Menu used to populate action buttons
                 * @return true if the action mode should be created, false if entering this mode should be aborted.
                 */
                override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
                    val inflater = mode?.menuInflater
                    inflater?.inflate(R.menu.category_list_action, menu)
                    return true
                }

                /**
                 * Called to refresh an action mode's action menu whenever it is invalidated.
                 *
                 * @param mode The current ActionMode
                 * @param menu Menu used to populate action buttons
                 */
                override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean = true

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
        fun createCategory()

        /**
         * Called if the user clicks a category in the list
         *
         * @param categoryID category ID
         */
        fun categoryClicked(categoryID: Long)
    }

    companion object {

        /**
         * Use this factory method to create a new instance of this fragment using the provided parameters.
         *
         * @param knittingID id of the knitting for which a category should be selected
         * @return A new instance of fragment EditCategoryFragment.
         */
        @JvmStatic
        fun newInstance(knittingID: Long) =
            CategoryListFragment().apply {
                arguments = Bundle().apply {
                    putLong(Extras.EXTRA_KNITTING_ID, knittingID)
                }
            }
    }
}
