package com.mthaler.knittings.dropbox

import android.os.AsyncTask
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.dropbox.core.android.Auth
import com.dropbox.core.v2.files.ListFolderResult
import com.mthaler.knittings.R
import com.mthaler.knittings.utils.NetworkUtils
import kotlinx.android.synthetic.main.fragment_dropbox_import.*
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import com.mthaler.knittings.database.datasource
import com.mthaler.knittings.model.Database
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DropboxImportFragment : AbstractDropboxFragment() {

    private var importTask: AsyncTask<String, Void, ListFolderResult?>? = null
    private var importing = false
    private var backupDirectory = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Retain this fragment across configuration changes.
        retainInstance = true
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_dropbox_import, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        login_button.setOnClickListener { Auth.startOAuth2Authentication(context, AppKey) }

        import_button.setOnClickListener {
            context?.let {
                val isWiFi = NetworkUtils.isWifiConnected(it)
                if (!isWiFi) {
                    val builder = AlertDialog.Builder(it)
                    with(builder) {
                        setTitle(resources.getString(R.string.dropbox_import))
                        setMessage(resources.getString(R.string.dropbox_export_no_wifi_question))
                        setPositiveButton(resources.getString(R.string.dropbox_export_dialog_export_button), { dialog, which ->
                            importTask = ListFolderTask(DropboxClientFactory.getClient(), ::onListFolder, ::onListFolderError).execute("")
                        })
                        setNegativeButton(resources.getString(R.string.dialog_button_cancel), { dialog, which -> })
                        show()
                    }
                } else {
                    importTask = ListFolderTask(DropboxClientFactory.getClient(), ::onListFolder, ::onListFolderError).execute("")
                }
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

    override fun loadData(onError: (Exception) -> Unit) {
        viewLifecycleOwner.lifecycleScope.launch {
            val account = withContext(Dispatchers.IO) {
                val client = DropboxClientFactory.getClient()
                val account = client.users().currentAccount
                //val spaceUsage = client.users().spaceUsage
                account
            }
            email_text.text = account.email
            name_text.text = account.name.displayName
            type_text.text = account.accountType.name
        }
    }

    private fun setMode(importing: Boolean) {
        if (importing) {
            import_button.visibility = View.GONE
            import_text.visibility = View.VISIBLE
            progressBar.visibility = View.VISIBLE
        } else {
            import_button.visibility = View.VISIBLE
            import_text.visibility = View.GONE
            progressBar.visibility = View.GONE
            progressBar.progress = 0
        }
        this.importing = importing
    }

    private fun onListFolder(result: ListFolderResult?) {
        context?.let {
            if (result != null) {
                val files = result.entries.map { it.name }.sortedDescending().toTypedArray()
                val dialogBuilder = AlertDialog.Builder(it)
                dialogBuilder.setTitle("Backups")
                dialogBuilder.setItems(files) { dialog, item ->
                    val folderName = files[item]
                    backupDirectory = folderName
                    val builder = AlertDialog.Builder(it)
                    with(builder) {
                        setTitle(resources.getString(R.string.dropbox_import))
                        setMessage("Do you really want to import from Dropbox? This will delete all existing data!")
                        setPositiveButton("Import", { dialog, which ->
                            DownloadDatabaseTask(it.applicationContext, DropboxClientFactory.getClient(), ::onDownloadDatabase, ::onDownloadDatabaseError).execute(folderName)
                            setMode(true)
                        })
                        setNegativeButton(resources.getString(R.string.dialog_button_cancel), { dialog, which -> })
                        show()
                    }
                }
                dialogBuilder.setNegativeButton("Cancel") { dialog, which -> }
                // Create alert dialog object via builder
                val alertDialogObject = dialogBuilder.create()
                // Show the dialog
                alertDialogObject.show()
            } else {
                val builder = AlertDialog.Builder(it)
                with(builder) {
                    setTitle("List folders")
                    setMessage("Error when listing folders: null")
                    setPositiveButton("OK", { dialog, which -> })
                    show()
                }
            }
        }
    }

    /**
     * The onListFolderError method is called if an exception happens when the ListFolderTask is executed,
     *
     * @param ex exception that happened when executing ListFolderTask
     */
    private fun onListFolderError(ex: Exception) {
        context?.let {
            val builder = AlertDialog.Builder(it)
            with(builder) {
                setTitle("List folders")
                setMessage("Error when listing folders: " + ex.message)
                setPositiveButton("OK", { dialog, which -> })
                show()
            }
        }
    }

    private fun onDownloadDatabase(database: Database?) {
        context?.let {
            if (database != null) {
                // remove all existing entries from the database
                datasource.deleteAllKnittings()
                datasource.deleteAllPhotos()
                datasource.deleteAllCategories()
                datasource.deleteAllNeedles()
                // add downloaded database
                for (knitting in database.knittings) {
                    datasource.addKnitting(knitting, manualID = true)
                }
                for (photo in database.photos) {
                    datasource.addPhoto(photo, manualID = true)
                }
                for (category in database.categories) {
                    datasource.addCategory(category, manualID = true)
                }
                for (needle in database.needles) {
                    datasource.addNeedle(needle, manualID = true)
                }
                DownloadPhotosTask(DropboxClientFactory.getClient(), it, backupDirectory, database, progressBar::setProgress, ::onDownloadPhotosComplete).execute()
            } else {
                val builder = AlertDialog.Builder(it)
                with(builder) {
                    setTitle("Download database")
                    setMessage("Could not download database: null")
                    setPositiveButton("OK", { dialog, which -> })
                    show()
                }
            }
        }
    }

    /**
     * The onDownloadDatabaseError method is called if an exception happens when the DownloadDatabaseTask is executed,
     *
     * @param ex exception that happened when executing DownloadDatabaseTask
     */
    private fun onDownloadDatabaseError(ex: Exception) {
        val builder = AlertDialog.Builder(requireContext())
        with(builder) {
            setTitle("Download database")
            setMessage("Could not download database: " + ex.message)
            setPositiveButton("OK", { dialog, which -> })
            show()
        }
    }

    private fun onDownloadPhotosComplete() {
        setMode(false)
        val builder = AlertDialog.Builder(requireContext())
        with(builder) {
            setTitle(resources.getString(R.string.dropbox_import))
            setMessage("Dropbox import completed")
            setPositiveButton("OK", { dialog, which -> })
            show()
        }
    }

    companion object {
        private const val AppKey = "6ybf7tgqdbhf641"
    }
}
