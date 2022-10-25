package com.example.gui.buttons

import com.example.config.Config
import com.example.core.Feature
import com.example.utils.ColorCode
import com.example.utils.DrawUtils
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.renderer.GlStateManager
import org.lwjgl.input.Mouse

class ButtonResize(var x: Float, var y: Float, feature: Feature, val corner: Corner)
    : ButtonFeature(0, 0, 0, "", feature) {
    private var cornerOffsetX = 0f
    private var cornerOffsetY = 0f
    override fun drawButton(mc: Minecraft?, mouseX: Int, mouseY: Int) {
        val scale: Float = feature.guiScale
        GlStateManager.pushMatrix()
        GlStateManager.scale(scale, scale, 1f)
        hovered = mouseX >= (x - SIZE) * scale && mouseY >= (y - SIZE) * scale && mouseX < (x + SIZE) * scale && mouseY < (y + SIZE) * scale
        val color: Int = if (hovered) ColorCode.WHITE.color else ColorCode.WHITE.getColor(70)
        DrawUtils.drawRectAbsolute((x - SIZE).toDouble(), (y - SIZE).toDouble(), (x + SIZE).toDouble(), (y + SIZE).toDouble(), color)
        GlStateManager.popMatrix()
    }

    override fun mousePressed(mc: Minecraft, mouseX: Int, mouseY: Int): Boolean {
        val sr = ScaledResolution(mc)
        val minecraftScale = sr.scaleFactor.toFloat()
        val floatMouseX = Mouse.getX() / minecraftScale
        val floatMouseY = (mc.displayHeight - Mouse.getY()) / minecraftScale
        cornerOffsetX = floatMouseX
        cornerOffsetY = floatMouseY
        return hovered
    }

    enum class Corner {
        TOP_LEFT, TOP_RIGHT, BOTTOM_RIGHT, BOTTOM_LEFT
    }

    companion object {
        private const val SIZE = 2
    }
}