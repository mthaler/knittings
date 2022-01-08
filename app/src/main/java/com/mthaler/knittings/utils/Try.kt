package com.mthaler.knittings.utils

import java.lang.Exception

abstract sealed class Try<out E> {

    abstract fun isFailure(): Boolean

    fun isSucesss(): Boolean = !isFailure()

    abstract fun get(): E

    data class Success<E>(val value: E) : Try<E>() {

        override fun isFailure(): Boolean = false

        override fun get(): E = value
    }

    data class Failure(val exception: Exception) : Try<Nothing>() {

        override fun isFailure(): Boolean = true

        override fun get(): Nothing = throw exception
    }

    companion object {

        operator fun <E>invoke(f: () -> E): Try<E> {
            try {
                val result = f()
                return Success(result)
            } catch (ex: Exception) {
                return Failure(ex)
            }
        }
    }
}