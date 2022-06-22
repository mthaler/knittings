package com.mthaler.knittings.utils

import android.content.Context


object ResourcesProvider {

    private lateinit var context: Context

    fun init(context: Context) {
        this.context = context
    }

    fun getString(redId: Int): String = context.getString(redId)
}