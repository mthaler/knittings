package com.mthaler.knittings.compressphotos

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.mthaler.knittings.database.DataSourceViewModel
import com.mthaler.knittings.database.KnittingsDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.Exception

class CompressPhotosViewModel(application: Application) : DataSourceViewModel(application) {

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
                var photosToCompress = 0
                for (p in photos) {
                    try {
                        if (p.filename.exists()) {
                            val size = p.filename.length()
                            totalSize += size
                            if (size > 350 * 1024) {
                                photosToCompress += 1
                            }
                        }
                    } catch(ex: Exception) {
                        // ignore
                    }
                }
                Statistics(photos.size, totalSize, photosToCompress)
            }
            statistics.value = s
        }
    }
}