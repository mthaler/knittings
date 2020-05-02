package com.mthaler.knittings.dropbox

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.dropbox.core.v2.files.WriteMode
import com.mthaler.knittings.database.datasource
import com.mthaler.knittings.model.Database
import com.mthaler.knittings.model.toJSON
import com.mthaler.knittings.utils.FileUtils.createDateTimeDirectoryName
import com.mthaler.knittings.utils.FileUtils.getExtension
import java.io.ByteArrayInputStream
import java.io.FileInputStream
import java.util.*

class DropboxExportService : Service() {

    override fun onBind(intent: Intent): IBinder {
        TODO("Return the communication channel to the service.")
    }

    private fun upload() {
        val dbxClient = DropboxClientFactory.getClient()

        val knittings = datasource.allKnittings
        val photos = datasource.allPhotos
        val categories = datasource.allCategories
        val needles = datasource.allNeedles
        val dbJSON = Database(knittings, photos, categories, needles).toJSON()
        val s = dbJSON.toString(2)
        val dbInputStream = ByteArrayInputStream(s.toByteArray())

        // create directory containing current date & time
        val dir = createDateTimeDirectoryName(Date())
        dbxClient.files().createFolderV2("/$dir")

        // upload database to dropbox
        dbxClient.files().uploadBuilder("/$dir/db.json") // Path in the user's Dropbox to save the file.
                .withMode(WriteMode.OVERWRITE) // always overwrite existing file
                .uploadAndFinish(dbInputStream)

        // upload photos to dropbox
        val count = photos.size
        for ((index, photo) in photos.withIndex()) {
            //if (isCancelled) break
            val inputStream = FileInputStream(photo.filename)
            dbxClient.files().uploadBuilder("/" + dir + "/" + photo.id + "." + getExtension(photo.filename.name)) // Path in the user's Dropbox to save the file.
                    .withMode(WriteMode.OVERWRITE) // always overwrite existing file
                    .uploadAndFinish(inputStream)
            //publishProgress((index / count.toFloat() * 100).toInt())
        }
    }
}
