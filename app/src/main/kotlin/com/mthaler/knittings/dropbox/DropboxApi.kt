package com.mthaler.knittings.dropbox

import com.dropbox.core.DbxException
import com.dropbox.core.v2.DbxClientV2
import com.dropbox.core.v2.files.FileMetadata
import com.dropbox.core.v2.files.ListFolderResult
import com.dropbox.core.v2.files.Metadata
import com.dropbox.core.v2.users.FullAccount
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DropboxApi(private val dropboxClient: DbxClientV2) {

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