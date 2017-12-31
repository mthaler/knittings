package com.mthaler.knittings

import android.text.Editable

/**
 * TextWatcher interface with default implementations for beforeTextChanged and afterTextChanged
 */
interface TextWatcher : android.text.TextWatcher {

    override fun beforeTextChanged(c: CharSequence, start: Int, count: Int, after: Int) = Unit

    override fun afterTextChanged(c: Editable) = Unit
}