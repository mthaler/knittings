package com.mthaler.knittings.dropbox

import android.support.v7.app.AppCompatActivity
import com.dropbox.core.android.Auth

/**
 * Base class for Activities that require auth tokens
 * Will redirect to auth flow if needed
 */
abstract class AbstractDropboxActivity : AppCompatActivity() {

    override fun onResume() {
        super.onResume()

        val prefs = getSharedPreferences(SharedPreferencesName, MODE_PRIVATE)
        var accessToken = prefs.getString("access-token", null)
        if (accessToken == null) {
            accessToken = Auth.getOAuth2Token()
            if (accessToken != null) {
                prefs.edit().putString("access-token", accessToken).apply()
                initAndLoadData(accessToken)
            }
        } else {
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
        loadData()
    }

    protected abstract fun loadData()

    protected fun hasToken(): Boolean {
        val prefs = getSharedPreferences(SharedPreferencesName, MODE_PRIVATE)
        val accessToken = prefs.getString("access-token", null)
        return accessToken != null
    }

    companion object {
        private val SharedPreferencesName = "com.mthaler.knittings"
    }
}