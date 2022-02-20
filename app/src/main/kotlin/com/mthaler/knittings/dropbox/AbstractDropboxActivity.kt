package com.mthaler.knittings.dropbox

import com.mthaler.knittings.BaseActivity

abstract class AbstractDropboxActivity : BaseActivity() {

    protected abstract fun updateFragment()
}