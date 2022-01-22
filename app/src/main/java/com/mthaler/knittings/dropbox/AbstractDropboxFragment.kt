package com.mthaler.knittings.dropbox

import android.app.Activity
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.preference.PreferenceManager
import com.dropbox.core.DbxRequestConfig
import com.dropbox.core.android.Auth
import com.dropbox.core.oauth.DbxCredential
import com.dropbox.core.v2.DbxClientV2
import com.mthaler.knittings.BuildConfig
import kotlinx.coroutines.launch

/**
 * Base class for Dropbox fragments
 *
 * The base class handles loging into Dropbox
 */
abstract class AbstractDropboxFragment : Fragment() {

    protected var listener: OnFragmentInteractionListener? = null

    private val APP_KEY = BuildConfig.DROPBOX_KEY

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

    /*abstract protected fun clearData()*/

    /**
     * Starts the Dropbox OAuth process by launching the Dropbox official app or web
     * browser if dropbox official app is not available. In browser flow, normally user needs to
     * sign in.
     *
     * Because mobile apps need to keep Dropbox secrets in their binaries we need to use PKCE.
     * Read more about this here: https://dropbox.tech/developers/pkce--what-and-why-
     **/
    protected fun startDropboxAuthorization() {
        // The client identifier is usually of the form "SoftwareName/SoftwareVersion".
        val clientIdentifier = "DropboxSampleAndroid/1.0.0"
        val requestConfig = DbxRequestConfig(clientIdentifier)

        // The scope's your app will need from Dropbox
        // Read more about Scopes here: https://developers.dropbox.com/oauth-guide#dropbox-api-permissions
        val scopes = listOf("account_info.read", "files.content.write")
        Auth.startOAuth2PKCE(requireContext(), APP_KEY, requestConfig, scopes)
    }

    protected fun revokeDropboxAuthorization() {
        val clientIdentifier = "DropboxSampleAndroid/1.0.0"
        val requestConfig = DbxRequestConfig(clientIdentifier)
        val credential = getLocalCredential()
        val dropboxClient = DbxClientV2(requestConfig, credential)
        val dropboxApi = DropboxApi(dropboxClient)
        lifecycleScope.launch {
            dropboxApi.revokeDropboxAuthorization()
        }
        val sharedPreferences = requireActivity().getSharedPreferences("dropbox-sample", Activity.MODE_PRIVATE)
        sharedPreferences.edit().remove("credential").apply()
        clearData()
    }

    private fun clearData() {
    }

    //deserialize the credential from SharedPreferences if it exists
    protected fun getLocalCredential(): DbxCredential? {
        val sharedPreferences = requireActivity().getSharedPreferences("dropbox-sample",
            AppCompatActivity.MODE_PRIVATE
        )
        val serializedCredential = sharedPreferences.getString("credential", null) ?: return null
        return DbxCredential.Reader.readFully(serializedCredential)
    }

    //serialize the credential and store in SharedPreferences
    protected fun storeCredentialLocally(dbxCredential: DbxCredential) {
        val sharedPreferences = requireActivity().getSharedPreferences("dropbox-sample", AppCompatActivity.MODE_PRIVATE
        )
        sharedPreferences.edit().putString("credential", dbxCredential.toString()).apply()
    }
}