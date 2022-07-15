package com.mthaler.knittings.dropbox

import android.os.Environment
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.mthaler.knittings.HasContext
import com.mthaler.knittings.database.DataSourceViewModel
import com.mthaler.knittings.database.KnittingsDataSource
import com.mthaler.knittings.database.PhotoDataSource
import com.mthaler.knittings.utils.removeLeadingChars
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.lang.Exception

class DropboxExportViewModel : DataSourceViewModel()  {

    private val ds = KnittingsDataSource as PhotoDataSource
    private val context = (KnittingsDataSource as HasContext).context

    val statistics = MutableLiveData(Statistics.EMPTY)

    init {
        updatePhotoCount()
    }

    override fun databaseChanged() {
        updatePhotoCount()
    }

    private fun updatePhotoCount() {
        viewModelScope.launch {
            val s = withContext(Dispatchers.IO) {
                val storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
                Log.d(DropboxImportWorker.TAG, "storage dir: " + storageDir)
                val photos = ds.allPhotos
                var totalSize = 0L
                for (p in photos) {
                    try {
                        val localPath = File(storageDir, p.filename.name.removeLeadingChars('/'))
                        if (localPath.exists()) {
                            val size = localPath.length()
                            totalSize += size
                        }
                    } catch(ex: Exception) {
                        // ignore
                    }
                }
                Statistics(photos.size, totalSize)
            }
            statistics.value = s
        }
    }
}