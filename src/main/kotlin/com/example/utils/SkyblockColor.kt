package com.example.utils

import com.example.config.Config
import com.example.core.chroma.ManualChromaManager
import com.example.shader.ShaderManager
import net.minecraft.client.renderer.Tessellator
import java.awt.Color
import java.util.*

class SkyblockColor {
    var colorAnimation = ColorAnimation.NONE
    private val colors = LinkedList<Int>()

    @JvmOverloads
    constructor(color: Int = DEFAULT_COLOR) {
        colors.add(color)
    }

    constructor(color: Int, alpha: Float) {
        colors.add(ColorUtils.setColorAlpha(color, alpha))
    }

    constructor(r: Int, g: Int, b: Int, a: Int) {
        colors.add(ColorUtils.getColor(r, g, b, a))
    }

    constructor(r: Int, g: Int, b: Int, a: Float) {
        colors.add(ColorUtils.getColor(r, g, b, ColorUtils.getAlphaIntFromFloat(a)))
    }

    fun getColorAtPosition(x: Float, y: Float): Int {
        return if (colorAnimation == ColorAnimation.CHROMA) {
            ManualChromaManager.getChromaColor(x, y, ColorUtils.getAlpha(color))
        } else colors[0]
    }

    fun getTintAtPosition(x: Float, y: Float): Int {
        return if (colorAnimation == ColorAnimation.CHROMA) {
            ManualChromaManager.getChromaColor(
                x, y, Color.RGBtoHSB(
                    ColorUtils.getRed(color), ColorUtils.getGreen(
                        color
                    ), ColorUtils.getBlue(color), null
                ), ColorUtils.getAlpha(color)
            )
        } else colors[0]
    }

    fun getColorAtPosition(x: Double, y: Double, z: Double): Int {
        return getColorAtPosition(x.toFloat(), y.toFloat(), z.toFloat())
    }

    fun getColorAtPosition(x: Float, y: Float, z: Float): Int {
        return if (colorAnimation == ColorAnimation.CHROMA) {
            ManualChromaManager.getChromaColor(x, y, z, ColorUtils.getAlpha(color))
        } else colors[0]
    }

    fun setColor(color: Int): SkyblockColor {
        return setColor(0, color)
    }

    fun setColor(index: Int, color: Int): SkyblockColor {
        if (index >= colors.size) {
            colors.add(color)
        } else {
            colors[index] = color
        }
        return this
    }

    val isMulticolor: Boolean
        get() = colorAnimation != ColorAnimation.NONE
    val isPositionalMulticolor: Boolean
        get() = colorAnimation != ColorAnimation.NONE && Config.Chroma.mode !== Config.ChromaMode.ALL_SAME_COLOR
    val color: Int
        get() = getColorSafe(0)

    private fun getColorSafe(index: Int): Int {
        while (index >= colors.size) {
            colors.add(DEFAULT_COLOR)
        }
        return colors[index]
    }

    fun drawMulticolorManually(): Boolean {
        return colorAnimation == ColorAnimation.CHROMA && !shouldUseChromaShaders()
    }

    fun drawMulticolorUsingShader(): Boolean {
        return colorAnimation == ColorAnimation.CHROMA && shouldUseChromaShaders()
    }

    enum class ColorAnimation {
        NONE, CHROMA
    }

    companion object {
        private val tessellator = Tessellator.getInstance()
        private val worldRenderer = tessellator.worldRenderer
        private const val DEFAULT_COLOR = -0x1
        fun shouldUseChromaShaders(): Boolean {
            return Config.Chroma.mode == Config.ChromaMode.ALL_SAME_COLOR &&
                    ShaderManager.INSTANCE.areShadersSupported() &&
                    Config.Chroma.useNewMode
        }
    }
}