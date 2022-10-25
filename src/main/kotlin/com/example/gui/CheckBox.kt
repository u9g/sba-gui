package com.example.gui

import com.example.utils.ColorCode
import com.example.utils.DrawUtils
import com.example.utils.Utils
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.GlStateManager
import kotlin.math.roundToInt

/**
 * CheckBox GUI element to use in other GUI elements.
 *
 * @author DidiSkywalker
 */
class CheckBox constructor(
    private val mc: Minecraft,
    private val x: Int,
    private val y: Int,
    size: Int,
    text: String?
) {
    fun interface OnToggleListener {
        fun onToggle(value: Boolean)
    }

    private val scale: Float
    private val text: String?
    private val textWidth: Int
    private val size: Int
    var value = false
    private var onToggleListener: OnToggleListener? = null

    /**
     * @param mc Minecraft instance
     * @param x x position
     * @param y y position
     * @param size Desired size (height) to scale to
     * @param text Displayed text
     * @param value Default value
     */
    constructor(mc: Minecraft, x: Int, y: Int, size: Int, text: String?, value: Boolean) : this(mc, x, y, size, text) {
        this.value = value
    }

    /**
     * @param mc Minecraft instance
     * @param x x position
     * @param y y position
     * @param size Desired size (height) to scale to
     * @param text Displayed text
     */
    init {
        scale = size.toFloat() / ICON_SIZE.toFloat()
        this.text = text
        textWidth = mc.fontRendererObj.getStringWidth(text)
        this.size = size
    }

    fun draw() {
        val scaledX = Math.round(x / scale)
        val scaledY = (y / scale).roundToInt()
        GlStateManager.pushMatrix()
        GlStateManager.scale(scale, scale, 1f)
        val color: Int = if (value) ColorCode.WHITE.color else ColorCode.GRAY.color
        DrawUtils.drawText(text, (scaledX + (size * 1.5f / scale).roundToInt()).toFloat(), (scaledY + size / 2).toFloat(), color)
        GlStateManager.disableDepth()
        GlStateManager.enableBlend()
        Minecraft.getMinecraft().textureManager.bindTexture(Utils.ICONS)
        GlStateManager.color(1f, 1f, 1f, 1f)
        if (value) {
            mc.ingameGUI.drawTexturedModalRect(scaledX, scaledY, 49, 34, 16, 16)
        } else {
            mc.ingameGUI.drawTexturedModalRect(scaledX, scaledY, 33, 34, 16, 16)
        }
        GlStateManager.enableDepth()
        GlStateManager.popMatrix()
    }

    fun onMouseClick(mouseX: Int, mouseY: Int, mouseButton: Int) {
        if (mouseButton == 0 && mouseX > x && mouseX < x + size + textWidth && mouseY > y && mouseY < y + size) {
            value = !value
            Utils.playSound("gui.button.press", 0.25, 1.0)
            if (onToggleListener != null) {
                onToggleListener!!.onToggle(value)
            }
            Utils.blockNextClick = true
        }
    }

    /**
     * Attaches a listener that gets notified whenever the CheckBox is toggled
     *
     * @param listener Listener to attach
     */
    fun setOnToggleListener(listener: OnToggleListener?) {
        onToggleListener = listener
    }

    companion object {
        /**
         * Size of the CheckBox icon
         */
        private const val ICON_SIZE = 16
    }
}