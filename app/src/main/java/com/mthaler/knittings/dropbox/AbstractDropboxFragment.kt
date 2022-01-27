package com.mthaler.knittings.dropbox

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import com.dropbox.core.android.Auth
import com.mthaler.knittings.dropbox.DropboxClientFactory
import com.mthaler.knittings.dropbox.accessToken
import com.mthaler.knittings.dropbox.removeAccessToken
import com.mthaler.knittings.dropbox.userID

/**
 * Base class for Dropbox fragments
 *
 * The base class handles loging into Dropbox
 */
abstract class AbstractDropboxFragment : Fragment() {

    protected var listener: OnFragmentInteractionListener? = null

    override fun onResume() {
        super.onResume()

        // get the access token from shared pref_sharing
        val prefs = PreferenceManager.getDefaultSharedPreferences(requireContext())
        var accessToken = prefs.accessToken
        if (accessToken == null) {
            // if the user just logged in using the web browser, we get a non null access
            accessToken = Auth.getOAuth2Token()
            if (accessToken != null) {
                // save the access token
                prefs.accessToken = accessToken
                initAndLoadData(accessToken)
            }
        } else {
            // we have an access token
            initAndLoadData(accessToken)
        }

        val uid = Auth.getUid()
        val storedUid = prefs.userID
        if (uid != null && uid != storedUid) {
            prefs.userID = uid
        }
    }

    // called from onResume if we have an access token
    private fun initAndLoadData(accessToken: String) {
        DropboxClientFactory.init(accessToken)
        loadData(::onLoadDataError)
    }

    // called after the DropboxClientFactory is initialized
    protected abstract fun loadData(onError: (Exception) -> Unit)

    private fun onLoadDataError(ex: Exception) {
        val prefs = PreferenceManager.getDefaultSharedPreferences(requireContext())
        prefs.removeAccessToken()
        // clear client so that it is not reused next time we connect to Dropbox
        DropboxClientFactory.clearClient()
        val accessToken = Auth.getOAuth2Token()
        if (accessToken != null) {
            prefs.accessToken = accessToken
            initAndLoadData(accessToken)
        }
    }

    protected fun hasToken(): Boolean {
        val prefs = PreferenceManager.getDefaultSharedPreferences(requireContext())
        return prefs.accessToken != null
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

        fun error(ex: Exception)
    }
}