package com.mthaler.knittings

import android.text.Editable

/**
 * TextWatcher interface with default implementations for beforeTextChanged and afterTextChanged
 */
interface TextWatcher : android.text.TextWatcher {

    /**
     * This method is called to notify you that, within s, the count characters beginning at start have just
     * replaced old text that had length before. It is an error to attempt to make changes to s from this callback.
     */
    override fun onTextChanged(c: CharSequence, start: Int, before: Int, count: Int) = Unit

    /**
     * This method is called to notify you that, within s, the count characters beginning at start are about
     * to be replaced by new text with length after. It is an error to attempt to make changes to s from this callback.
     */
    override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) = Unit

    /**
     * This method is called to notify you that, somewhere within s, the text has been changed. It is legitimate
     * to make further changes to s from this callback, but be careful not to get yourself into an infinite loop,
     * because any changes you make will cause this method to be called again recursively.
     */
    override fun afterTextChanged(c: Editable) = Unit
}