package com.example.gui.buttons

import com.example.core.Feature
import com.example.utils.DrawUtils
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.util.ResourceLocation

class ButtonColorWheel(x: Float, y: Float, feature: Feature) :
    ButtonFeature(0, 0, 0, "", feature) {
    var x: Float
    var y: Float

    init {
        width = size
        height = size
        this.x = x
        this.y = y
    }

    override fun drawButton(mc: Minecraft, mouseX: Int, mouseY: Int) {
        val scale: Float = feature.guiScale
        hovered = mouseX >= x * scale && mouseY >= y * scale && mouseX < x * scale + width * scale && mouseY < y * scale + height * scale
        GlStateManager.color(1f, 1f, 1f, if (hovered) 1f else 0.5f)
        GlStateManager.pushMatrix()
        GlStateManager.scale(scale, scale, 1f)
        GlStateManager.enableBlend()
        mc.textureManager.bindTexture(COLOR_WHEEL)
        DrawUtils.drawModalRectWithCustomSizedTexture(x, y, 0f, 0f, 10f, 10f, 10f, 10f, true)
        GlStateManager.popMatrix()
    }

    override fun mousePressed(mc: Minecraft?, mouseX: Int, mouseY: Int): Boolean {
        val scale: Float = feature.guiScale
        return mouseX >= x * scale && mouseY >= y * scale && mouseX < x * scale + width * scale && mouseY < y * scale + height * scale
    }

    companion object {
        private val COLOR_WHEEL = ResourceLocation("examplemod", "gui/colorwheel.png")
        const val size = 10
    }
}