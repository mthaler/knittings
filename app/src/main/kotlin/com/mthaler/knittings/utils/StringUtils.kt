package com.mthaler.knittings.utils

fun String.containsIgnoreCase(what: String): Boolean {
    val length = what.length
    if (length == 0)
        return true // Empty string is contained

    val firstLo = Character.toLowerCase(what[0])
    val firstUp = Character.toUpperCase(what[0])

    for (i in this.length - length downTo 0) {
        // Quick check before calling the more expensive regionMatches() method:
        val ch = this[i]
        if (ch != firstLo && ch != firstUp)
            continue

        if (this.regionMatches(i, what, 0, length, ignoreCase = true))
            return true
    }

    return false
}

fun String.removeLeadingChars(c: Char): String {
    if (this.startsWith(c)) {
        val result = this.substring(1)
        val result2 = result.removeLeadingChars(c)
        if (result == result2) {
            return result
        } else {
            return result2.removeLeadingChars(c)
        }
    } else {
        return this
    }
}