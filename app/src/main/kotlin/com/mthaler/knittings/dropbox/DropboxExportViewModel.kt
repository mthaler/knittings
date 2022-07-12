package com.mthaler.knittings.dropbox

import android.app.Application
import android.os.Environment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.mthaler.knittings.DatabaseApplication
import com.mthaler.knittings.database.DataSourceViewModel
import com.mthaler.knittings.database.KnittingsDataSource
import com.mthaler.knittings.database.PhotoDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.Exception

class DropboxExportViewModel : DataSourceViewModel()  {

    private val ds = KnittingsDataSource as PhotoDataSource

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
                val photos = ds.allPhotos
                var totalSize = 0L
                for (p in photos) {
                    try {
                        if (p.filename.exists()) {

                            val size = p.filename.length()
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