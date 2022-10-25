package com.example.gui.buttons

import com.example.core.chroma.ManualChromaManager
import com.example.core.chroma.MulticolorShaderManager
import com.example.shader.ShaderManager
import com.example.shader.chroma.ChromaScreenShader
import com.example.utils.ColorCode
import com.example.utils.ColorUtils
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiButton
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import org.lwjgl.opengl.GL11

/**
 * This button is for when you are choosing one of the 16 color codes.
 */
class ButtonColorBox(x: Int, y: Int, color: ColorCode) : GuiButton(0, x, y, null) {
    private val color: ColorCode

    init {
        width = 40
        height = 20
        this.color = color
    }

    override fun drawButton(mc: Minecraft, mouseX: Int, mouseY: Int) {
        hovered = mouseX > xPosition && mouseX < xPosition + width && mouseY > yPosition && mouseY < yPosition + height
        if (color === ColorCode.CHROMA && !MulticolorShaderManager.INSTANCE.shouldUseChromaShaders()) {
            if (hovered) {
                drawChromaRect(xPosition, yPosition, xPosition + width, yPosition + height, 255)
            } else {
                drawChromaRect(xPosition, yPosition, xPosition + width, yPosition + height, 127)
            }
        } else {
            if (color === ColorCode.CHROMA && MulticolorShaderManager.INSTANCE.shouldUseChromaShaders()) {
                ShaderManager.INSTANCE.enableShader(ChromaScreenShader::class.java)
            }
            if (hovered) {
                drawRect(xPosition, yPosition, xPosition + width, yPosition + height, color.color)
            } else {
                drawRect(xPosition, yPosition, xPosition + width, yPosition + height, color.getColor(127))
            }
            if (color === ColorCode.CHROMA && MulticolorShaderManager.INSTANCE.shouldUseChromaShaders()) {
                ShaderManager.INSTANCE.disableShader()
            }
        }
    }

    fun getColor(): ColorCode {
        return color
    }

    companion object {
        const val WIDTH = 40
        const val HEIGHT = 20
        fun drawChromaRect(left: Int, top: Int, right: Int, bottom: Int, alpha: Int) {
            var left = left
            var top = top
            var right = right
            var bottom = bottom
            if (left < right) {
                val i = left
                left = right
                right = i
            }
            if (top < bottom) {
                val j = top
                top = bottom
                bottom = j
            }
            val tessellator = Tessellator.getInstance()
            val worldrenderer = tessellator.worldRenderer
            GlStateManager.enableBlend()
            GlStateManager.disableTexture2D()
            GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0)
            GlStateManager.shadeModel(GL11.GL_SMOOTH)
            //GlStateManager.disableAlpha();

            //GlStateManager.color(1, 1, 1, 1);
            worldrenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR)
            val colorLB: Int = ManualChromaManager.getChromaColor(left.toFloat(), bottom.toFloat(), 1)
            val colorRB: Int = ManualChromaManager.getChromaColor(right.toFloat(), bottom.toFloat(), 1)
            val colorLT: Int = ManualChromaManager.getChromaColor(left.toFloat(), top.toFloat(), 1)
            val colorRT: Int = ManualChromaManager.getChromaColor(right.toFloat(), top.toFloat(), 1)
            val colorMM: Int = ManualChromaManager.getChromaColor(Math.floorDiv(right + left, 2).toFloat(), Math.floorDiv(top + bottom, 2).toFloat(), 1)
            // First triangle
            worldrenderer.pos(right.toDouble(), bottom.toDouble(), 0.0)
                .color(ColorUtils.getRed(colorRB), ColorUtils.getGreen(colorRB), ColorUtils.getBlue(colorRB), alpha)
                .endVertex()
            worldrenderer.pos(Math.floorDiv(right + left, 2).toDouble(), Math.floorDiv(top + bottom, 2).toDouble(), 0.0)
                .color(ColorUtils.getRed(colorMM), ColorUtils.getGreen(colorMM), ColorUtils.getBlue(colorMM), alpha)
                .endVertex()
            worldrenderer.pos(left.toDouble(), top.toDouble(), 0.0)
                .color(ColorUtils.getRed(colorLT), ColorUtils.getGreen(colorLT), ColorUtils.getBlue(colorLT), alpha)
                .endVertex()
            worldrenderer.pos(left.toDouble(), bottom.toDouble(), 0.0)
                .color(ColorUtils.getRed(colorLB), ColorUtils.getGreen(colorLB), ColorUtils.getBlue(colorLB), alpha)
                .endVertex()
            // 2nd triangle
            worldrenderer.pos(right.toDouble(), bottom.toDouble(), 0.0)
                .color(ColorUtils.getRed(colorRB), ColorUtils.getGreen(colorRB), ColorUtils.getBlue(colorRB), alpha)
                .endVertex()
            worldrenderer.pos(right.toDouble(), top.toDouble(), 0.0)
                .color(ColorUtils.getRed(colorRT), ColorUtils.getGreen(colorRT), ColorUtils.getBlue(colorRT), alpha)
                .endVertex()
            worldrenderer.pos(left.toDouble(), top.toDouble(), 0.0)
                .color(ColorUtils.getRed(colorLT), ColorUtils.getGreen(colorLT), ColorUtils.getBlue(colorLT), alpha)
                .endVertex()
            worldrenderer.pos(Math.floorDiv(right + left, 2).toDouble(), Math.floorDiv(top + bottom, 2).toDouble(), 0.0)
                .color(ColorUtils.getRed(colorMM), ColorUtils.getGreen(colorMM), ColorUtils.getBlue(colorMM), alpha)
                .endVertex()
            tessellator.draw()
            GlStateManager.shadeModel(GL11.GL_FLAT)
            GlStateManager.enableTexture2D()
            GlStateManager.disableBlend() //GlStateManager.enableAlpha();
        }
    }
}