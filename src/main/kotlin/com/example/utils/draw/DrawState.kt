package com.example.utils.draw

import com.example.core.chroma.MulticolorShaderManager
import com.example.utils.ColorUtils
import com.example.utils.DrawUtils
import com.example.utils.SkyblockColor
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.renderer.vertex.VertexFormat
import org.lwjgl.opengl.GL11

abstract class DrawState {
    protected var canAddVertices: Boolean
    protected var drawType = 0
    protected var format: VertexFormat? = null
    protected var textured: Boolean
    protected var ignoreTexture: Boolean
    protected var color: SkyblockColor

    constructor(
        theColor: SkyblockColor,
        theDrawType: Int,
        theFormat: VertexFormat?,
        isTextured: Boolean,
        shouldIgnoreTexture: Boolean
    ) {
        color = theColor
        drawType = theDrawType
        format = theFormat
        textured = isTextured
        ignoreTexture = shouldIgnoreTexture
        canAddVertices = true
    }

    constructor(theColor: SkyblockColor, isTextured: Boolean, shouldIgnoreTexture: Boolean) {
        color = theColor
        ignoreTexture = shouldIgnoreTexture
        textured = isTextured
        canAddVertices = false
    }

    fun beginWorld() {
        if (canAddVertices) {
            worldRenderer.begin(drawType, format)
        }
    }

    fun draw() {
        if (canAddVertices) {
            tessellator.draw()
        }
    }

    protected fun newColor(is3D: Boolean) {
        if (color.drawMulticolorUsingShader()) {
            MulticolorShaderManager.INSTANCE.begin(textured, ignoreTexture, is3D)
            GlStateManager.shadeModel(GL11.GL_SMOOTH)
        }
        if (textured && ignoreTexture) {
            DrawUtils.enableOutlineMode()
            // Textured shader needs white color to work properly
            if (color.drawMulticolorUsingShader()) {
                DrawUtils.outlineColor(-0x1)
            } else {
                DrawUtils.outlineColor(color.color)
            }
        }
    }

    protected fun bindColor(colorInt: Int) {
        if (textured && ignoreTexture) {
            if (color.isPositionalMulticolor && color.drawMulticolorManually()) {
                DrawUtils.outlineColor(colorInt)
            }
        } else {
            GlStateManager.color(
                ColorUtils.getRed(colorInt) / 255f,
                ColorUtils.getGreen(colorInt) / 255f,
                ColorUtils.getBlue(colorInt) / 255f,
                ColorUtils.getAlpha(colorInt) / 255f
            )
        }
    }

    protected fun endColor() {
        if (color.drawMulticolorUsingShader()) {
            MulticolorShaderManager.INSTANCE.end()
            GlStateManager.shadeModel(GL11.GL_FLAT)
        }
        if (textured && ignoreTexture) {
            DrawUtils.disableOutlineMode()
        }
    }

    fun reColor(newColor: SkyblockColor) {
        color = newColor
    }

    companion object {
        private val tessellator = Tessellator.getInstance()
        private val worldRenderer = tessellator.worldRenderer
    }
}
