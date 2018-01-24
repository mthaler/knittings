package com.mthaler.knittings.dropbox

import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import com.dropbox.core.android.Auth
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.wtf

abstract class AbstractDropboxFragment : Fragment(), AnkoLogger {

    override fun onResume() {
        super.onResume()

        val ctx = context
        if (ctx != null) {
            val prefs = ctx.getSharedPreferences(SharedPreferencesName, AppCompatActivity.MODE_PRIVATE)
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
        } else {
            wtf("context null")
        }
    }

    private fun initAndLoadData(accessToken: String) {
        DropboxClientFactory.init(accessToken)
        loadData()
    }

    protected abstract fun loadData()

    protected fun hasToken(): Boolean {
        val ctx = context
        if (ctx != null) {
            val prefs = context!!.getSharedPreferences(SharedPreferencesName, AppCompatActivity.MODE_PRIVATE)
            val accessToken = prefs.getString("access-token", null)
            return accessToken != null
        } else {
            wtf("context null")
            return false
        }
    }

    companion object {
        private val SharedPreferencesName = "com.mthaler.knittings"
    }
}