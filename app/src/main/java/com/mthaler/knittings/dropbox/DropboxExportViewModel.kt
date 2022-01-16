package com.mthaler.knittings.dropbox

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.mthaler.knittings.DatabaseApplication
import com.mthaler.knittings.database.DataSourceViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.Exception

class DropboxExportViewModel(application: Application) : DataSourceViewModel(application)  {

    private val ds = (application as DatabaseApplication<*>).getPhotoDataSource()

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