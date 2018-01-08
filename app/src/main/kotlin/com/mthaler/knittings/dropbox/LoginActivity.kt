package com.mthaler.knittings.dropbox

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import com.dropbox.core.android.Auth
import com.mthaler.knittings.MainActivity
import com.mthaler.knittings.R

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val SignInButton = findViewById<Button>(R.id.sign_in_button)
        SignInButton.setOnClickListener(object : View.OnClickListener {
            override fun onClick(view: View) {
                val ctx = applicationContext
                val s = getString(R.string.APP_KEY)
                Auth.startOAuth2Authentication(applicationContext, getString(R.string.APP_KEY))
            }
        })
    }

    override fun onResume() {
        super.onResume()
        getAccessToken()
    }

    fun getAccessToken() {
        val accessToken = Auth.getOAuth2Token() //generate Access Token
        if (accessToken != null) {
            //Store accessToken in SharedPreferences
            val prefs = getSharedPreferences("com.mthaler.knittings", MODE_PRIVATE)
            prefs.edit().putString("access-token", accessToken).apply()

            //Proceed to MainActivity
            val intent = Intent(this@LoginActivity, MainActivity::class.java)
            startActivity(intent)
        }
    }
}
