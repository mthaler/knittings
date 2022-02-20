package com.mthaler.knittings.dropbox

import android.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import androidx.preference.PreferenceManager
import com.dropbox.core.android.AuthActivity
import com.google.android.material.snackbar.Snackbar
import com.mthaler.knittings.BaseActivity
import com.mthaler.knittings.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

abstract class AbstractDropboxActivity : BaseActivity(), AbstractDropboxFragment.OnFragmentInteractionListener {

    override fun error(ex: Exception) {
        val builder = androidx.appcompat.app.AlertDialog.Builder(this)
        with(builder) {
            setTitle(resources.getString(R.string.dropbox_error))
            setMessage("${ex.message}")
            setNegativeButton(resources.getString(R.string.dialog_button_ok)) { dialog, which -> }
            show()
        }
        removeAccessToken()
        clearDropboxClient()
        clearAuthActivity()
        updateFragment()
    }

    private fun removeAccessToken() {
        val prefs = PreferenceManager.getDefaultSharedPreferences(this@AbstractDropboxActivity)
        prefs.removeAccessToken()
    }

    private fun clearDropboxClient() {
        //DropboxClientFactory.clearClient()
    }

    private fun clearAuthActivity() {
        AuthActivity.result = null
    }

    protected abstract fun updateFragment()
}