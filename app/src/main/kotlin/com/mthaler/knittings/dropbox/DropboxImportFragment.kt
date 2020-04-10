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
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.support.v4.alert
import org.jetbrains.anko.uiThread
import androidx.appcompat.app.AlertDialog
import com.mthaler.knittings.database.datasource
import com.mthaler.knittings.model.Database

class DropboxImportFragment : AbstractDropboxFragment(), AnkoLogger {

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
            val ctx = context
            if (ctx != null) {
                val isWiFi = NetworkUtils.isWifiConnected(ctx)
                if (!isWiFi) {
                    alert {
                        title = resources.getString(R.string.dropbox_import)
                        message = resources.getString(R.string.dropbox_export_no_wifi_question)
                        positiveButton(resources.getString(R.string.dropbox_export_dialog_export_button)) {
                            importTask = ListFolderTask(DropboxClientFactory.getClient(), ::onListFolder, ::onListFolderError).execute("")
                        }
                        negativeButton(resources.getString(R.string.dialog_button_cancel)) {}
                    }.show()
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
        } else {
            import_button.visibility = View.VISIBLE
            import_text.visibility = View.GONE
            progressBar.visibility = View.GONE
            progressBar.progress = 0
        }
        this.importing = importing
    }

    private fun onListFolder(result: ListFolderResult?) {
        val ctx = context
        if (ctx != null && result != null) {
            val files = result.entries.map { it.name }.sortedDescending().toTypedArray()
            val dialogBuilder = AlertDialog.Builder(ctx)
            dialogBuilder.setTitle("Backups")
            dialogBuilder.setItems(files) { dialog, item ->
                val folderName = files[item]
                backupDirectory = folderName
                alert {
                    title = resources.getString(R.string.dropbox_import)
                    message = "Do you really want to import from Dropbox? This will delete all existing data!"
                    positiveButton("Import") {
                        DownloadDatabaseTask(ctx.applicationContext, DropboxClientFactory.getClient(), ::onDownloadDatabase, ::onDownloadDatabaseError).execute(folderName)
                        setMode(true)
                    }
                    negativeButton(resources.getString(R.string.dialog_button_cancel)) {}
                }.show()
            }
            dialogBuilder.setNegativeButton("Cancel") { dialog, which -> }
            // Create alert dialog object via builder
            val alertDialogObject = dialogBuilder.create()
            // Show the dialog
            alertDialogObject.show()
        } else {
            alert {
                title = "List folders"
                message = "Error when listing folders: null"
                positiveButton("OK") {}
            }.show()
        }
    }

    /**
     * The onListFolderError method is called if an exception happens when the ListFolderTask is executed,
     *
     * @param ex exception that happened when executing ListFolderTask
     */
    private fun onListFolderError(ex: Exception) {
        alert {
            title = "List folders"
            message = "Error when listing folders: " + ex.message
            positiveButton("OK") {}
        }.show()
    }

    private fun onDownloadDatabase(database: Database?) {
        val ctx = context
        if (ctx != null && database != null) {
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
            DownloadPhotosTask(DropboxClientFactory.getClient(), ctx, backupDirectory, database, progressBar::setProgress, ::onDownloadPhotosComplete).execute()
        } else {
            alert {
                title = "Download database"
                message = "Could not download database: null"
                positiveButton("OK") {}
            }.show()
        }
    }

    /**
     * The onDownloadDatabaseError method is called if an exception happens when the DownloadDatabaseTask is executed,
     *
     * @param ex exception that happened when executing DownloadDatabaseTask
     */
    private fun onDownloadDatabaseError(ex: Exception) {
        alert {
            title = "Download database"
            message = "Could not download database: " + ex.message
            positiveButton("OK") {}
        }.show()
    }

    private fun onDownloadPhotosComplete() {
        setMode(false)
        alert {
            title = resources.getString(R.string.dropbox_import)
            message = "Dropbox import completed"
            positiveButton("OK") {}
        }.show()
    }

    companion object {
        private const val AppKey = "6ybf7tgqdbhf641"
    }
}
