package com.example.core.chroma

import com.example.config.Config
import com.example.core.Feature
import com.example.core.features.ColoredFeature
import com.example.misc.scheduler.NewScheduler
import com.example.utils.ColorUtils
import com.example.utils.Utils
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.ScaledResolution
import java.awt.Color

object ManualChromaManager {
    var coloringTextChroma = false

    var featureScale = 0f
    private val defaultColorHSB = floatArrayOf(0f, 0.75f, 0.9f)

    /**
     * Before rending a string that supports chroma, call this method so it marks the text
     * to have the color fade applied to it.<br></br><br></br>
     *
     * After calling this & doing the drawString, make sure to call [ManualChromaManager.doneRenderingText].
     *
     * @param feature The feature to check if fade chroma is enabled.
     */
    fun renderingText(feature: Feature) {
        if (Config.Chroma.mode === Config.ChromaMode.FADE && feature is ColoredFeature && feature.isChroma) {
            coloringTextChroma = true
            featureScale = feature.guiScale
        }
    }

    // TODO Don't force alpha in the future...
    fun getChromaColor(x: Float, y: Float, alpha: Int): Int {
        return getChromaColor(x, y, defaultColorHSB, alpha)
    }

    fun getChromaColor(x: Float, y: Float, currentHSB: FloatArray, alpha: Int): Int {
        var x = x
        var y = y
        if (Config.Chroma.mode === Config.ChromaMode.ALL_SAME_COLOR) {
            x = 0f
            y = 0f
        }
        if (coloringTextChroma) {
            x *= featureScale
            y *= featureScale
        }
        val scale = ScaledResolution(Minecraft.getMinecraft()).scaleFactor
        x *= scale.toFloat()
        y *= scale.toFloat()
        val chromaSize: Float = Config.Chroma.size * (Minecraft.getMinecraft().displayWidth / 100f)
        val chromaSpeed: Float = Config.Chroma.speed / 360f
        val ticks: Float = NewScheduler.INSTANCE.totalTicks as Float + Utils.getPartialTicks()
        val timeOffset = ticks * chromaSpeed
        val newHue = ((x + y) / chromaSize - timeOffset) % 1

        //if (currentHSB[2] < 0.3) { // Keep shadows as shadows
        //    return ColorUtils.setColorAlpha(Color.HSBtoRGB(newHue, currentHSB[1], currentHSB[2]), alpha);
        //} else {
        val saturation: Float = Config.Chroma.saturation
        val brightness: Float = Config.Chroma.brightness * currentHSB[2]
        return ColorUtils.setColorAlpha(Color.HSBtoRGB(newHue, saturation, brightness), alpha)
        //}
    }

    fun getChromaColor(x: Float, y: Float, z: Float, alpha: Int): Int {
        var x = x
        var y = y
        var z = z
        if (Config.Chroma.mode === Config.ChromaMode.ALL_SAME_COLOR) {
            x = 0f
            y = 0f
            z = 0f
        }
        val chromaSize: Float = Config.Chroma.size * (Minecraft.getMinecraft().displayWidth / 100f)
        val chromaSpeed: Float = Config.Chroma.speed / 360f
        val ticks: Float = NewScheduler.INSTANCE.totalTicks as Float + Utils.getPartialTicks()
        val timeOffset = ticks * chromaSpeed
        val newHue = ((x - y + z) / (chromaSize / 20f) - timeOffset) % 1
        val saturation: Float = Config.Chroma.saturation
        val brightness: Float = Config.Chroma.brightness
        return ColorUtils.setColorAlpha(Color.HSBtoRGB(newHue, saturation, brightness), alpha)
    }

    /**
     * Disables any chroma stuff.
     */
    fun doneRenderingText() {
        coloringTextChroma = false
        featureScale = 1f
    }
}