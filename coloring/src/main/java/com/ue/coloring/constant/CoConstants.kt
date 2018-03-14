package com.ue.coloring.constant

import java.io.File

/**
 * Created by hawk on 2017/12/26.
 */

object CoConstants {
    private const val ASSETS = "file:///android_asset/"
    const val THEME_DEFAULT_IMAGE = "${ASSETS}secret_garden.jpg"
    const val THEME_DEFAULT = "SecretGarden"
    val THEME_DEFAULT_PREFIX = "$ASSETS$THEME_DEFAULT${File.separator}"

    const val TAG_COLORING_THEMES = "coloringThemes"
}
