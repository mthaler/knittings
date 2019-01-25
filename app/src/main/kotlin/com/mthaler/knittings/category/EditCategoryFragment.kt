package com.mthaler.knittings.category

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.mthaler.knittings.R
import com.mthaler.knittings.model.Category
import com.mthaler.knittings.Extras.EXTRA_CATEGORY_ID
import com.mthaler.knittings.database.datasource

class EditCategoryFragment : Fragment() {

    private var category: Category? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            val categoryID = it.getLong(EXTRA_CATEGORY_ID)
            category = datasource.getCategory(categoryID)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_edit_category, container, false)
    }

    companion object {

        /**
         * Use this factory method to create a new instance of this fragment using the provided parameters.
         *
         * @param categoryID id of the category that should be edited
         * @return A new instance of fragment EditCategoryFragment.
         */
        @JvmStatic
        fun newInstance(categoryID: Long) =
                EditCategoryFragment().apply {
                    arguments = Bundle().apply {
                        putLong(EXTRA_CATEGORY_ID, categoryID)
                    }
                }
    }
}
