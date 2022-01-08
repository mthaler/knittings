package com.mthaler.knittings.dropbox

import android.content.SharedPreferences

val ACCESS_TOKEN = "access-token"

var SharedPreferences.accessToken
    get() = getString(ACCESS_TOKEN, null)
    set(value) = edit().putString(ACCESS_TOKEN, value).apply()

fun SharedPreferences.removeAccessToken() {
    edit().remove(ACCESS_TOKEN).commit()
}

val USER_ID = "user-id"

var SharedPreferences.userID
    get() = getString(USER_ID, null)
    set(value) = edit().putString(USER_ID, value).apply()