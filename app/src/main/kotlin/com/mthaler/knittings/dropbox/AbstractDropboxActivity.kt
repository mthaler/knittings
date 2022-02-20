package com.mthaler.knittings.dropbox

import com.dropbox.core.android.AuthActivity
import com.mthaler.knittings.BaseActivity

abstract class AbstractDropboxActivity : BaseActivity() {

    private fun clearDropboxClient() {
        //DropboxClientFactory.clearClient()
    }

    private fun clearAuthActivity() {
        AuthActivity.result = null
    }

    protected abstract fun updateFragment()
}