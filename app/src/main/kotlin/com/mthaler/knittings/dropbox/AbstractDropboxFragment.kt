package com.mthaler.knittings.dropbox

import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import com.dropbox.core.android.Auth
import com.mthaler.dbapp.dropbox.DropboxClientFactory

/**
 * Base class for Dropbox fragments
 *
 * The base class handles loging into Dropbox
 */
abstract class AbstractDropboxFragment : Fragment() {

    override fun onResume() {
        super.onResume()

        // get the access token from shared pref_sharing
        val prefs = PreferenceManager.getDefaultSharedPreferences(requireContext())
        var accessToken = prefs.getString("access-token", null)
        if (accessToken == null) {
            // no access token, we need to login
            accessToken = Auth.getOAuth2Token()
            if (accessToken != null) {
                // save the access token
                prefs.edit().putString("access-token", accessToken).apply()
                initAndLoadData(accessToken)
            }
        } else {
            // we have an access token
            initAndLoadData(accessToken)
        }

        val uid = Auth.getUid()
        val storedUid = prefs.getString("user-id", null)
        if (uid != null && uid != storedUid) {
            prefs.edit().putString("user-id", uid).apply()
        }
    }

    private fun initAndLoadData(accessToken: String) {
        DropboxClientFactory.init(accessToken)
        loadData(::onLoadDataError)
    }

    protected abstract fun loadData(onError: (Exception) -> Unit)

    private fun onLoadDataError(ex: Exception) {
        val prefs = PreferenceManager.getDefaultSharedPreferences(requireContext())
        val editor = prefs.edit()
        editor.remove("access-token")
        editor.commit()
        // clear client so that it is not reused next time we connect to Dropbox
        DropboxClientFactory.clearClient()
        val accessToken = Auth.getOAuth2Token()
        if (accessToken != null) {
            // save the access token
            prefs.edit().putString("access-token", accessToken).apply()
            initAndLoadData(accessToken)
        }
    }

    protected fun hasToken(): Boolean {
        val prefs = PreferenceManager.getDefaultSharedPreferences(requireContext())
        val accessToken = prefs.getString("access-token", null)
        return accessToken != null
    }

    companion object {
        const val SharedPreferencesName = "com.mthaler.knittings"
    }
}