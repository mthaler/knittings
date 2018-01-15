package com.mthaler.knittings.dropbox

import android.os.Bundle
import android.view.View
import com.mthaler.knittings.R
import com.dropbox.core.android.Auth
import kotlinx.android.synthetic.main.activity_dropbox_export.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread

class DropboxExportActivity : AbstractDropboxActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dropbox_export)

        setSupportActionBar(toolbar)

        login_button.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View) {
                Auth.startOAuth2Authentication(this@DropboxExportActivity, getString(R.string.APP_KEY))
            }
        })

        export_button.setOnClickListener(object: View.OnClickListener {
            override fun onClick(v: View) {
                UploadTask(DropboxClientFactory.getClient(), applicationContext, progressBar).execute()
            }
        })
    }

    override fun onResume() {
        super.onResume()

        if (hasToken()) {
            login_button.visibility = View.GONE
            email_text.visibility = View.VISIBLE
            name_text.visibility = View.VISIBLE
            type_text.visibility = View.VISIBLE
            export_button.isEnabled = true
        } else {
            login_button.visibility = View.VISIBLE
            email_text.visibility = View.GONE
            name_text.visibility = View.GONE
            type_text.visibility = View.GONE
            export_button.isEnabled = false
        }
    }

    override fun loadData() {
        doAsync {
            val account = DropboxClientFactory.getClient().users().getCurrentAccount();
            uiThread {
                email_text.text = account.email
                name_text.text = account.name.displayName
                type_text.text = account.accountType.name
            }
        }
    }
}
