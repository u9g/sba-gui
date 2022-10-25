package com.example.utils.draw

import com.example.core.chroma.MulticolorShaderManager
import com.example.utils.ColorUtils.getAlpha
import com.example.utils.ColorUtils.getBlue
import com.example.utils.ColorUtils.getGreen
import com.example.utils.ColorUtils.getRed
import com.example.utils.SkyblockColor
import net.minecraft.client.renderer.GlStateManager

open class DrawStateFontRenderer(theColor: SkyblockColor?) : DrawState2D(theColor!!, true, false) {
    private var multicolorFeatureOverride = false
    private var isActive = false
    private var featureScale = 1f
    fun setupMulticolorFeature(theFeatureScale: Float) {
        if (color.drawMulticolorManually()) {
            featureScale = theFeatureScale
        }
        multicolorFeatureOverride = true
    }

    fun endMulticolorFeature() {
        if (color.drawMulticolorManually()) {
            featureScale = 1f
        }
        multicolorFeatureOverride = false
    }

    fun loadFeatureColorEnv() {
        if (multicolorFeatureOverride) {
            newColorEnv()
        }
    }

    fun restoreColorEnv() {
        if (color.drawMulticolorUsingShader()) {
            if (multicolorFeatureOverride) {
                // TODO: change order of restore to bind white here after font renderer binds the other color
            } else {
                MulticolorShaderManager.INSTANCE.end()
            }
        }
        isActive = false
    }

    override fun newColorEnv(): DrawStateFontRenderer {
        super.newColorEnv()
        isActive = true
        return this
    }

    override fun endColorEnv(): DrawStateFontRenderer {
        super.endColorEnv()
        isActive = false
        return this
    }

    override fun bindAnimatedColor(x: Float, y: Float): DrawStateFontRenderer {
        // Handle feature scale here
        val colorInt = color.getTintAtPosition(x * featureScale, y * featureScale)
        GlStateManager.color(
            getRed(colorInt) / 255f,
            getGreen(colorInt) / 255f,
            getBlue(colorInt) / 255f,
            getAlpha(colorInt) / 255f
        )
        return this
    }

    fun shouldManuallyRecolorFont(): Boolean {
        return (multicolorFeatureOverride || isActive) && color.drawMulticolorManually()
    }
}