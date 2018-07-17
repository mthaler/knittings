package com.mthaler.knittings.dropbox

import android.os.AsyncTask
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.dropbox.core.android.Auth
import com.dropbox.core.v2.files.ListFolderResult
import com.mthaler.knittings.R
import kotlinx.android.synthetic.main.fragment_dropbox_import.*
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread

class DropboxImportFragment : AbstractDropboxFragment(), AnkoLogger {

    private var importTask: AsyncTask<String, Void, ListFolderResult>? = null
    private var importing = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Retain this fragment across configuration changes.
        retainInstance = true
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_dropbox_import, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        login_button.setOnClickListener { Auth.startOAuth2Authentication(context, DropboxImportFragment.AppKey) }

        import_button.setOnClickListener {
            val ctx = context
            if (ctx != null) {
                importTask = ListFolderTask(DropboxClientFactory.getClient(), ::onListFolder, ::onListFolderError).execute("")
            }
        }
    }

    override fun onResume() {
        super.onResume()

        if (hasToken()) {
            login_button.visibility = View.GONE
            email_text.visibility = View.VISIBLE
            name_text.visibility = View.VISIBLE
            type_text.visibility = View.VISIBLE
            import_button.isEnabled = true
            setMode(importing)
        } else {
            login_button.visibility = View.VISIBLE
            email_text.visibility = View.GONE
            name_text.visibility = View.GONE
            type_text.visibility = View.GONE
            import_button.isEnabled = false
        }
    }

    override fun loadData() {
        doAsync {
            val client = DropboxClientFactory.getClient()
            val account = client.users().currentAccount
            val spaceUsage = client.users().spaceUsage
            uiThread {
                email_text.text = account.email
                name_text.text = account.name.displayName
                type_text.text = account.accountType.name
            }
        }
    }

    private fun setMode(importing: Boolean) {
        if (importing) {
            import_button.visibility = View.GONE
            import_text.visibility = View.VISIBLE
            progressBar.visibility = View.VISIBLE
            cancel_button.visibility = View.VISIBLE
        } else {
            import_button.visibility = View.VISIBLE
            import_text.visibility = View.GONE
            progressBar.visibility = View.GONE
            progressBar.progress = 0
            cancel_button.visibility = View.GONE
        }
        this.importing = importing
    }

    private fun onListFolder(result: ListFolderResult) {
        val files = result.entries.map { it.name }.toTypedArray()
        val f = DropboxListFolderFragment.newInstance(files)
        val fragmentTransaction = activity!!.supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.frament_container, f)
        fragmentTransaction.addToBackStack(null)
        fragmentTransaction.commit()

    }

    private fun onListFolderError(exception: Exception) {

    }

    companion object {
        private val AppKey = "6ybf7tgqdbhf641"
    }
}
