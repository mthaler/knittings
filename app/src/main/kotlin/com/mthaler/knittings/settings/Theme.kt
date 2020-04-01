package com.mthaler.knittings.settings

import android.content.Context
import androidx.core.content.ContextCompat
import com.mthaler.knittings.R

data class Theme(val name: String, val colorId: Int, val themeId: Int) {

    companion object {

        val default = Theme("default", R.color.colorPrimary, R.style.AppTheme_NoActionBar)
        val mint = Theme("mint", R.color.mintColorPrimary, R.style.Theme_App_Mint)
        val rose = Theme("rose", R.color.roseColorPrimary, R.style.Theme_App_Rose)
        val sky = Theme("sky", R.color.skyColorPrimary, R.style.Theme_App_Sky)

        val themes = listOf(default, mint, rose, sky)

        fun getTheme(name: String): Theme {
            val result = themes.find { it.name == name }
            return if (result != null) result else default
        }

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