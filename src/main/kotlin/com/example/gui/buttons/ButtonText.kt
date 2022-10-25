package com.example.gui.buttons

import com.example.core.Feature
import com.example.utils.DrawUtils
import net.minecraft.client.renderer.GlStateManager

/**
 * Create a button that displays text.
 */
internal open class ButtonText(buttonId: Int, x: Int, y: Int, buttonText: String?, feature: Feature) : ButtonFeature(buttonId, x, y, buttonText, feature) {
    fun drawButtonBoxAndText(boxColor: Int, scale: Float, fontColor: Int) {
        drawRect(xPosition, yPosition, xPosition + width, yPosition + height, boxColor)
        GlStateManager.pushMatrix()
        GlStateManager.scale(scale, scale, 1f)
        DrawUtils.drawCenteredText(
            displayString,
            (xPosition + width / 2) / scale, (yPosition + height - 8 * scale / 2) / scale, fontColor
        )
        GlStateManager.disableBlend()
        GlStateManager.popMatrix()
    }
}