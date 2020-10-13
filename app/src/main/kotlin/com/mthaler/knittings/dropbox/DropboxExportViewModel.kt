package com.mthaler.knittings.dropbox

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.mthaler.dbapp.DataSourceViewModel
import com.mthaler.dbapp.dropbox.Statistics
import com.mthaler.knittings.database.KnittingsDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.Exception

class DropboxExportViewModel(application: Application) : DataSourceViewModel(application)  {

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
                val photos = KnittingsDataSource.allPhotos
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