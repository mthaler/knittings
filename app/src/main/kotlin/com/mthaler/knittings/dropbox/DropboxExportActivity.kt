package com.mthaler.knittings.dropbox

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.dropbox.core.v2.users.FullAccount
import com.mthaler.knittings.R

class DropboxExportActivity : AppCompatActivity() {

    private var ACCESS_TOKEN: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dropbox_export)

        if (!tokenExists()) {
            val intent = Intent(this@DropboxExportActivity, LoginActivity::class.java)
            startActivity(intent)
        }

        ACCESS_TOKEN = retrieveAccessToken();
        getUserAccount();
    }

    private fun tokenExists(): Boolean {
        val prefs = getSharedPreferences("com.mthaler.knittings", MODE_PRIVATE)
        val accessToken = prefs.getString("access-token", null)
        return accessToken != null
    }

    private fun retrieveAccessToken(): String? {
        //check if ACCESS_TOKEN is previously stored on previous app launches
        val prefs = getSharedPreferences("com.mthaler.knittings", MODE_PRIVATE)
        val accessToken = prefs.getString("access-token", null)
        if (accessToken == null) {
            Log.d("AccessToken Status", "No token found")
            return null
        } else {
            //accessToken already exists
            Log.d("AccessToken Status", "Token exists")
            return accessToken
        }
    }

    protected fun getUserAccount() {
        if (ACCESS_TOKEN == null) return
        UserAccountTask(DropboxClient.getClient(ACCESS_TOKEN), object : UserAccountTask.TaskDelegate {
            override fun onAccountReceived(account: FullAccount) {
                Log.d("User data", account.email)
                Log.d("User data", account.name.displayName)
                Log.d("User data", account.accountType.name)
                updateUI(account)
            }

            override fun onError(error: Exception) {
                Log.d("User data", "Error receiving account details.")
            }
        }).execute()
    }

    private fun updateUI(account: FullAccount) {
        Toast.makeText(this, "Sucessfully logged in: " + account.email, Toast.LENGTH_SHORT).show();
    }
}
