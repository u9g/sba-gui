package com.example.fonts

import com.example.config.Config
import com.example.core.Feature
import com.example.core.features.ColoredFeature
import com.example.utils.SkyblockColor
import com.example.utils.draw.DrawStateFontRenderer
import net.minecraft.client.Minecraft

object FontRendererHook {
    private val CHROMA_COLOR = SkyblockColor(-0x1).apply { colorAnimation = SkyblockColor.ColorAnimation.CHROMA }
    private val DRAW_CHROMA: DrawStateFontRenderer = DrawStateFontRenderer(CHROMA_COLOR)
    private val CHROMA_COLOR_SHADOW: SkyblockColor = SkyblockColor(-0xaaaaab).apply { colorAnimation = SkyblockColor.ColorAnimation.CHROMA }
    private val DRAW_CHROMA_SHADOW: DrawStateFontRenderer = DrawStateFontRenderer(CHROMA_COLOR_SHADOW)
    private val stringsWithChroma = MaxSizeHashMap<String, Boolean>(1000)
    private var currentDrawState: DrawStateFontRenderer? = null
    private var modInitialized = false
    fun changeTextColor() {
        if (shouldRenderChroma() && currentDrawState != null && currentDrawState!!.shouldManuallyRecolorFont()) {
            val fontRenderer = Minecraft.getMinecraft().fontRendererObj
            currentDrawState!!.bindAnimatedColor(fontRenderer.posX, fontRenderer.posY)
        }
    }

    fun setupFeatureFont(feature: Feature) {
        if (Config.Chroma.mode === Config.ChromaMode.FADE && feature is ColoredFeature && feature.isChroma) {
            DRAW_CHROMA.setupMulticolorFeature(feature.guiScale)
            DRAW_CHROMA_SHADOW.setupMulticolorFeature(feature.guiScale)
        }
    }

    fun endFeatureFont() {
        DRAW_CHROMA.endMulticolorFeature()
        DRAW_CHROMA_SHADOW.endMulticolorFeature()
    }

    /**
     * Called in patcher code to stop patcher optimization and do vanilla render
     * @param s string to render
     * @return true to override
     */
    fun shouldOverridePatcher(s: String): Boolean {
        return if (shouldRenderChroma()) {
            //return chromaStrings.get(s) == null || chromaStrings.get(s);
            if (stringsWithChroma[s] != null) {
                return stringsWithChroma[s]!!
            }
            // Check if there is a "§z" colorcode in the string and cache it
            var hasChroma = false
            var i = 0
            while (i < s.length) {
                if (s[i] == '§') {
                    i++
                    if (i < s.length && (s[i] == 'z' || s[i] == 'Z')) {
                        hasChroma = true
                        break
                    }
                }
                i++
            }
            stringsWithChroma[s] = hasChroma
            hasChroma
        } else {
            false
        }
    }

    /**
     * Called to save the current shader state
     */
    fun beginRenderString(shadow: Boolean) {
        if (shouldRenderChroma()) {
            val alpha = Minecraft.getMinecraft().fontRendererObj.alpha
            if (shadow) {
                currentDrawState = DRAW_CHROMA_SHADOW
                CHROMA_COLOR_SHADOW.setColor((255 * alpha).toInt() shl 24 or 0x555555)
            } else {
                currentDrawState = DRAW_CHROMA
                CHROMA_COLOR.setColor((255 * alpha).toInt() shl 24 or 0xFFFFFF)
            }
            currentDrawState!!.loadFeatureColorEnv()
        }
    }

    /**
     * Called to restore the saved chroma state
     */
    fun restoreChromaState() {
        if (shouldRenderChroma()) {
            currentDrawState!!.restoreColorEnv()
        }
    }

    /**
     * Called to turn chroma on
     */
    fun toggleChromaOn() {
        if (shouldRenderChroma()) {
            currentDrawState!!.newColorEnv().bindActualColor()
        }
    }

    /**
     * Called to turn chroma off after the full string has been rendered (before returning)
     */
    fun endRenderString() {
        if (shouldRenderChroma()) {
            currentDrawState!!.endColorEnv()
        }
    }

    /**
     * Called by [SkyblockAddons.postInit]
     */
    fun onModInitialized() {
        modInitialized = true
    }

    private fun shouldRenderChroma(): Boolean {
        return modInitialized/* && SkyblockAddons.getInstance().getUtils().isOnSkyblock()*/
    }

    /**
     * HashMap with upper limit on storage size. Used to enforce the font renderer cache not getting too large over time
     */
    class MaxSizeHashMap<K, V>(private val maxSize: Int) : LinkedHashMap<K, V>() {
        override fun removeEldestEntry(eldest: Map.Entry<K, V>): Boolean {
            return size > maxSize
        }
    }
}