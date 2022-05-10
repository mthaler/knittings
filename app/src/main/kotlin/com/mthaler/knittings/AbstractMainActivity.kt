package com.mthaler.knittings

import android.text.TextUtils
import androidx.activity.viewModels
import androidx.appcompat.widget.SearchView
import com.mthaler.knittings.filter.CombinedFilter
import com.mthaler.knittings.filter.ContainsFilter
import com.mthaler.knittings.model.Knitting

abstract class AbstractMainActivity : BaseActivity(), SearchView.OnQueryTextListener, SearchView.OnCloseListener {

    protected lateinit var viewModel: MainViewModel


    /**
     * Called when the query text is changed by the use
     *
     * @param newText the new content of the query text field
     * @return false if the SearchView should perform the default action of showing any suggestions
     *         if available, true if the action was handled by the listener
     */
    override fun onQueryTextChange(newText: String?): Boolean {
        if (newText == null || TextUtils.isEmpty(newText)) {
            viewModel.filter = CombinedFilter.empty<Knitting>()
        } else {
            viewModel.filter = CombinedFilter(listOf(ContainsFilter(newText)))
        }
        return true
    }

    /**
     * Called when the user submits the query. This could be due to a key press on the keyboard or due to pressing a submit button.
     * The listener can override the standard behavior by returning true to indicate that it has handled the submit request.
     * Otherwise return false to let the SearchView handle the submission by launching any associated intent.
     *
     * @param newText new content of the query text field
     * @return true if the query has been handled by the listener, false to let the SearchView perform the default action.
     */
    override fun onQueryTextSubmit(newText: String?): Boolean = false

    /**
     * The user is attempting to close the SearchView.
     *
     * @return true if the listener wants to override the default behavior of clearing the text field and dismissing it, false otherwise.
     */
    override fun onClose(): Boolean {
        viewModel.filter = CombinedFilter.empty<Knitting>()
        return true
    }
}