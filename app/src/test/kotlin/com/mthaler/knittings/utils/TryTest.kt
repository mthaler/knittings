package com.mthaler.knittings.utils

import org.junit.Test
import org.junit.Assert.*
import java.lang.ArithmeticException

class TryTest {

    @Test
    fun testSuccess() {
        val result = Try { 42 }
        assertTrue(result.isSucesss())
        assertEquals(42, result.get())
    }

    @Test
    fun testFailure() {
        val result = Try { 42 / 0 }
        assertTrue(result.isFailure())
        val failure = result as Try.Failure
        assertTrue(failure.exception is ArithmeticException)
    }
}