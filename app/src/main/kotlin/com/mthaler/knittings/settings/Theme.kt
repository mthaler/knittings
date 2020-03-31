package com.mthaler.knittings.settings

import com.mthaler.knittings.R

data class Theme(val name: String, val color: Int) {

    companion object {

        val default = Theme("default", R.color.colorPrimary)

        val themes = arrayListOf(default, Theme("mint", R.color.mintColorPrimary))

        fun getTheme(name: String): Theme {
            val result = themes.find { it.name == name }
            return if (result != null) result else default
        }
    }
}