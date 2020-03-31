package com.mthaler.knittings.dropbox

import androidx.fragment.app.Fragment
import androidx.appcompat.app.AppCompatActivity
import com.dropbox.core.android.Auth
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.wtf

/**
 * Base class for Dropbox fragments
 *
 * The base class handles loging into Dropbox
 */
abstract class AbstractDropboxFragment : Fragment(), AnkoLogger {

    override fun onResume() {
        super.onResume()

        val ctx = context
        if (ctx != null) {
            // get the access token from shared pref_sharing
            val prefs = ctx.getSharedPreferences(SharedPreferencesName, AppCompatActivity.MODE_PRIVATE)
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
        } else {
            wtf("context null")
        }
    }

    private fun initAndLoadData(accessToken: String) {
        DropboxClientFactory.init(accessToken)
        loadData(::onLoadDataError)
    }

    protected abstract fun loadData(onError: (Exception) -> Unit)

    protected fun onLoadDataError(ex: Exception) {
        // delete auth token from shared pref_sharing
        context?.let {
            val prefs = it.getSharedPreferences(SharedPreferencesName, AppCompatActivity.MODE_PRIVATE)
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
    }

    protected fun hasToken(): Boolean {
        val ctx = context
        if (ctx != null) {
            val prefs = ctx.getSharedPreferences(SharedPreferencesName, AppCompatActivity.MODE_PRIVATE)
            val accessToken = prefs.getString("access-token", null)
            return accessToken != null
        } else {
            wtf("context null")
            return false
        }
    }

    companion object {
        const val SharedPreferencesName = "com.mthaler.knittings"
    }
}