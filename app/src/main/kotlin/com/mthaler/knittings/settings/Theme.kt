package com.mthaler.knittings.settings

import android.content.Context
import androidx.core.content.ContextCompat

data class Theme(val name: String, val colorId: Int, val themeId: Int) {

    companion object {

        private lateinit var themes: List<Theme>

        fun setThemes(themes: List<Theme>) {
            Companion.themes = themes
        }

        val default: Theme
            get() = themes.first()

        fun getTheme(name: String): Theme = themes.find { it.name == name } ?: default

        fun getColors(context: Context): ArrayList<String> {
            val result = ArrayList<String>()
            for (theme in themes) {
                val c = ContextCompat.getColor(context, theme.colorId)
                val s = java.lang.String.format("#%06X", 0xFFFFFF and c)
                result.add(s)
            }
            return result
        }

        fun getTheme(index: Int): Theme {
            if (index >= 0 && index < themes.size) {
                return themes[index]
            } else {
                return default
            }
        }
    }
}