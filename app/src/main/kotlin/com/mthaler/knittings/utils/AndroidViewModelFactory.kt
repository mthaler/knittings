package com.mthaler.knittings.utils

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import java.lang.IllegalArgumentException

class AndroidViewModelFactory(private val application: Application): ViewModelProvider.Factory {

    @SuppressWarnings("ClassNewInstance")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        try {
            return modelClass.getConstructor(Application::class.java).newInstance(application)
        } catch (e: InstantiationException) {
            throw InstantiationException("Cannot create an instance of " + modelClass)
        } catch (e: IllegalAccessException) {
            throw IllegalAccessException("Cannot create an instance of " + modelClass)
        }
    }
}