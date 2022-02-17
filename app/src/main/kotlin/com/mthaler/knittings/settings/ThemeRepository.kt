package com.mthaler.knittings.settings

import com.mthaler.knittings.R

object ThemeRepository {

    val default = Theme("default", R.color.colorPrimary, R.style.AppTheme_NoActionBar)
    private val mint = Theme("mint", R.color.mintColorPrimary, R.style.Theme_App_Mint)
    private val rose = Theme("rose", R.color.roseColorPrimary, R.style.Theme_App_Rose)
    private val sky = Theme("sky", R.color.skyColorPrimary, R.style.Theme_App_Sky)

    val themes = listOf(default, mint, rose, sky)
}