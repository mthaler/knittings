package com.mthaler.knittings.dropbox

import android.app.Activity
import android.content.Context
import androidx.fragment.app.Fragment
import com.dropbox.core.DbxRequestConfig
import com.dropbox.core.android.Auth
import com.dropbox.core.oauth.DbxCredential

/**
 * Base class for Dropbox fragments
 *
 * The base class handles loging into Dropbox
 */
abstract class AbstractDropboxFragment : Fragment() {

    protected var listener: OnFragmentInteractionListener? = null

    abstract protected val APP_KEY: String
    abstract protected fun exception(ex: String)

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
        //Auth.startOAuth2Authentication(requireContext(), getString(R.string.app_name))
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

    companion object {
        val CLIENT_IDENTIFIER = "KNITTINGS"
        val KNITTINGS = "com.mthaler.knittings"
        val TAG = "AbstractDropboxFragment"
    }
}