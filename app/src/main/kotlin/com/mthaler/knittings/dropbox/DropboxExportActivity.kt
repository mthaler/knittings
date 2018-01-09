package com.mthaler.knittings.dropbox

import android.os.Bundle
import android.support.v7.widget.Toolbar
import android.view.View
import android.widget.Button
import com.mthaler.knittings.R
import com.dropbox.core.android.Auth
import android.widget.TextView
import kotlinx.android.synthetic.main.activity_dropbox_export.*
import com.mthaler.knittings.database.datasource
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread

class DropboxExportActivity : AbstractDropboxActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dropbox_export)

        val toolbar = findViewById<Toolbar>(R.id.app_bar)
        setSupportActionBar(toolbar)

        val loginButton = findViewById<Button>(R.id.login_button)

        loginButton.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View) {
                Auth.startOAuth2Authentication(this@DropboxExportActivity, getString(R.string.APP_KEY))
            }
        })

        export_button.setOnClickListener(object: View.OnClickListener {
            override fun onClick(v: View) {
                val photos = datasource.allPhotos
                if (!photos.isEmpty()) {
                    val photo = photos[0]
                    val file = photo.filename
                    UploadTask(DropboxClientFactory.getClient(), file, applicationContext).execute()
                }
            }
        })
    }

    override fun onResume() {
        super.onResume()

        if (hasToken()) {
            findViewById<View>(R.id.login_button).visibility = View.GONE
            findViewById<View>(R.id.email_text).visibility = View.VISIBLE
            findViewById<View>(R.id.name_text).visibility = View.VISIBLE
            findViewById<View>(R.id.type_text).visibility = View.VISIBLE
            findViewById<View>(R.id.export_button).isEnabled = true
        } else {
            findViewById<View>(R.id.login_button).visibility = View.VISIBLE
            findViewById<View>(R.id.email_text).visibility = View.GONE
            findViewById<View>(R.id.name_text).visibility = View.GONE
            findViewById<View>(R.id.type_text).visibility = View.GONE
            findViewById<View>(R.id.export_button).isEnabled = false
        }
    }

    override fun loadData() {
        doAsync {
            val account = DropboxClientFactory.getClient().users().getCurrentAccount();
            uiThread {
                (findViewById<View>(R.id.email_text) as TextView).text = account.email
                (findViewById<View>(R.id.name_text) as TextView).text = account.name.displayName
                (findViewById<View>(R.id.type_text) as TextView).text = account.accountType.name
            }
        }
    }
}
