package com.mthaler.knittings.dropbox

import android.app.IntentService
import android.content.Intent
import com.mthaler.knittings.model.toDatabase
import com.mthaler.knittings.utils.FileUtils
import org.json.JSONObject
import java.io.FileOutputStream

class DownloadPhotosService : IntentService("DownloadPhotosService") {

    override fun onHandleIntent(intent: Intent?) {
//        if (intent != null) {
//            val dataString: String = intent.dataString
//            val json = JSONObject(dataString)
//            val database = json.toDatabase()
//            val count = database.photos.size
//            for ((index, photo) in database.photos.withIndex()) {
//                // Download the file.
//                val filename = "/" + directory + "/" + photo.id + "." + FileUtils.getExtension(photo.filename.name)
//                FileOutputStream(photo.filename).use {
//                    dbxClient.files().download(filename).download(it)
//                }
//            }
//        }
        // Gets data from the incoming Intent


    }
}