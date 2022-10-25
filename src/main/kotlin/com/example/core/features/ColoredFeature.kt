package com.example.core.features

import com.example.core.chroma.ManualChromaManager
import com.example.utils.ColorCode

/**
 * Allows all colors including hex colors
 */
interface ColoredFeature {
    /**
     * This represents whether the color selection is restricted to the minecraft color codes only
     * such as &f, &a, and &b (white, green, and blue respectively).<br></br>
     *
     * Colors that cannot be used include other hex colors such as #FF00FF.
     */
    val colorsRestricted: Boolean
    var isChroma: Boolean
    var staticColor: Int

    fun getColor(): Int {
        if (isChroma) return ManualChromaManager.getChromaColor(0f, 0f, 255)

        return this.staticColor
    }
}