package com.mthaler.knittings.dropbox

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.preference.PreferenceManager
import com.dropbox.core.DbxRequestConfig
import com.dropbox.core.android.Auth
import com.dropbox.core.oauth.DbxCredential
import com.dropbox.core.v2.DbxClientV2
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Base class for Dropbox fragments
 *
 * The base class handles loging into Dropbox
 */
abstract class AbstractDropboxFragment : Fragment() {

    abstract protected val APP_KEY: String
    abstract protected fun exception(ex: String)

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
        val clientIdentifier = "KNITTINGS"
        val requestConfig = DbxRequestConfig(clientIdentifier)

        // The scope's your app will need from Dropbox
        // Read more about Scopes here: https://developers.dropbox.com/oauth-guide#dropbox-api-permissions
        val scopes = listOf("account_info.read", "files.content.read", "files.content.write")
        Auth.startOAuth2PKCE(requireContext(), APP_KEY, requestConfig, scopes)
    }

    private fun revokeDropboxAuthorization() {
        val sharedPreferences = requireActivity().getSharedPreferences("dropbox-sample", Context.MODE_PRIVATE)
        sharedPreferences.edit().remove("credential").apply()
        clearData()
    }

    protected fun clearData() {
        val builder = AlertDialog.Builder(requireContext())
        with(builder) {
            setTitle("Dropbox")
            setMessage("Do you want to log out of Dropbox?")
            setPositiveButton("OK") { dialog, which ->
                lifecycleScope.launch {
                    withContext(Dispatchers.IO) {
                        try {
                            //Check if we have an existing token stored, this will be used by DbxClient to make requests
                            val localCredential: DbxCredential? = getLocalCredential()
                            val credential: DbxCredential? = if (localCredential == null) {
                                val credential = Auth.getDbxCredential() //fetch the result from the AuthActivity
                                credential?.let {
                                    //the user successfully connected their Dropbox account!
                                    storeCredentialLocally(it)
                                }
                                credential
                            } else localCredential

                            // remove auth token from Dropbox server
                            credential?.let {
                                val requestConfig = DbxRequestConfig(CLIENT_IDENTIFIER)
                                val dropboxClient = DbxClientV2(requestConfig, credential)
                                val dropboxApi = DropboxApi(dropboxClient)
                                clearDropboxClient(dropboxApi)
                            }
                        } catch (ex: Exception) {
                        }
                    }
                    Snackbar.make(requireActivity().window.decorView.rootView, "Logged out of Dropbox", Snackbar.LENGTH_LONG).show()
                }
            }
            show()
        }
    }

    //deserialize the credential from SharedPreferences if it exists
    protected fun getLocalCredential(): DbxCredential? {
        val sharedPreferences = requireActivity().getSharedPreferences(KNITTINGS, Activity.MODE_PRIVATE)
        val serializedCredential = sharedPreferences.getString("credential", null) ?: return null
        return DbxCredential.Reader.readFully(serializedCredential)
    }

    //serialize the credential and store in SharedPreferences
    protected fun storeCredentialLocally(dbxCredential: DbxCredential) {
        val sharedPreferences = requireActivity().getSharedPreferences(KNITTINGS, Activity.MODE_PRIVATE)
        sharedPreferences.edit().putString("credential", dbxCredential.toString()).apply()
    }

    private fun removeAccessToken() {
        val prefs = PreferenceManager.getDefaultSharedPreferences(requireContext())
        prefs.removeAccessToken()
    }

    private suspend fun clearDropboxClient(dropboxApi: DropboxApi) {
        dropboxApi.revokeDropboxAuthorization()
    }

    companion object {
        val CLIENT_IDENTIFIER = "KNITTINGS"
        val KNITTINGS = "com.mthaler.knittings"
        val TAG = "AbstractDropboxFragment"
    }
}