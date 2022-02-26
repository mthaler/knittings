package com.mthaler.knittings.dropbox

import android.content.Context
import android.os.Environment
import androidx.appcompat.app.AlertDialog
import com.dropbox.core.DbxException
import com.dropbox.core.v2.DbxClientV2
import com.dropbox.core.v2.files.FileMetadata
import com.dropbox.core.v2.files.ListFolderResult
import com.dropbox.core.v2.files.Metadata
import com.dropbox.core.v2.users.FullAccount
import com.mthaler.knittings.DatabaseApplication
import com.mthaler.knittings.R
import com.mthaler.knittings.model.Project
import com.mthaler.knittings.service.JobStatus
import com.mthaler.knittings.utils.FileUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.ByteArrayOutputStream

class DropboxApi(private val dropboxClient: DbxClientV2) {

    suspend fun listFolders(): ListFolderResult =
        withContext(Dispatchers.IO) {
            dropboxClient.files().listFolder("")
        }

    suspend fun getAccountInfo(): DropboxAccountInfoResponse = withContext(Dispatchers.IO) {
        try {
            val accountInfo = dropboxClient.users().currentAccount
            DropboxAccountInfoResponse.Success(accountInfo)
        } catch (exception: DbxException) {
            DropboxAccountInfoResponse.Failure(exception)
        }
    }

    suspend fun revokeDropboxAuthorization() = withContext(Dispatchers.IO) {
        dropboxClient.auth().tokenRevoke()
    }
    private suspend fun getFilesForFolder(folderPath: String): GetFilesApiResponse =
        withContext(Dispatchers.IO) {
            try {
                val files = dropboxClient.files().listFolder(folderPath)
                GetFilesApiResponse.Success(files)
            } catch (exception: DbxException) {
                GetFilesApiResponse.Failure(exception)
            }
        }

    private suspend fun getFilesForFolderContinue(cursor: String) = withContext(Dispatchers.IO) {
        try {
            val files = dropboxClient.files().listFolderContinue(cursor)
            GetFilesApiResponse.Success(files)
        } catch (exception: DbxException) {
            GetFilesApiResponse.Failure(exception)
        }
    }


    suspend fun readDatabase(directory: String, ctx: Context) {
        val (database, idsFromPhotoFiles) = withContext(Dispatchers.IO) {
            val os = ByteArrayOutputStream()
            dropboxClient.files().download("/$directory/db.json").download(os)
            val bytes = os.toByteArray()
            val jsonStr = String(bytes)
            val json = JSONObject(jsonStr)
            val externalFilesDir = ctx.getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!
            val database = (ctx.applicationContext as DatabaseApplication<Project>).createExportDatabaseFromJSON(json, externalFilesDir
            )
            database.checkValidity()
            val entries = dropboxClient.files().listFolder("/$directory").entries
            val ids = entries.filter { it.name != "db.json" }
                .map { FileUtils.getFilenameWithoutExtension(it.name).toLong() }.toHashSet()
            Pair(database, ids)
        }
        val ids = database.photos.map { it.id }.toHashSet()
        val missingPhotos = ids - idsFromPhotoFiles
        if (missingPhotos.isNotEmpty()) {
            withContext(Dispatchers.Main) {
                val builder = AlertDialog.Builder(ctx)
                with(builder) {
                    setTitle(R.string.dropbox_import_dialog_title)
                    setMessage(
                        ctx.resources.getString(
                            R.string.dropbox_import_dialog_incomplete_msg,
                            missingPhotos.size as Any
                        )
                    )
                    setPositiveButton(R.string.dropbox_import_dialog_button_import) { dialog, which ->
                        val filteredDatabase = database.removeMissingPhotos(missingPhotos)
                        DropboxImportService.startService(ctx, directory, filteredDatabase)
                        DropboxImportServiceManager.getInstance().updateJobStatus(JobStatus.Progress(0))
                    }
                    setNegativeButton(ctx.resources.getString(R.string.dialog_button_cancel)) { dialog, which -> }
                    show()
                }
            }
        } else {
            withContext(Dispatchers.Main) {
                val builder = AlertDialog.Builder(ctx)
                with(builder) {
                    setTitle(ctx.resources.getString(R.string.dropbox_import_dialog_title))
                    setMessage(ctx.resources.getString(R.string.dropbox_import_dialog_msg))
                    setPositiveButton(ctx.resources.getString(R.string.dropbox_import_dialog_button_import)) { dialog, which ->
                        DropboxImportService.startService(ctx, directory, database)
                        DropboxImportServiceManager.getInstance().updateJobStatus(JobStatus.Progress(0))
                    }
                    setNegativeButton(ctx.resources.getString(R.string.dialog_button_cancel)) { dialog, which -> }
                    show()
                }
            }
        }
    }
}

sealed class DropboxUploadApiResponse {
    data class Success(val fileMetadata: FileMetadata) : DropboxUploadApiResponse()
    data class Failure(val exception: DbxException) : DropboxUploadApiResponse()
}

sealed class DropboxAccountInfoResponse {
    data class Success(val accountInfo: FullAccount) : DropboxAccountInfoResponse()
    data class Failure(val exception: DbxException) : DropboxAccountInfoResponse()
}

sealed class GetFilesResponse {
    data class Success(val result: List<Metadata>) : GetFilesResponse()
    data class Failure(val exception: DbxException) : GetFilesResponse()
}


sealed class GetFilesApiResponse {
    data class Success(val result: ListFolderResult) : GetFilesApiResponse()
    data class Failure(val exception: DbxException) : GetFilesApiResponse()
}