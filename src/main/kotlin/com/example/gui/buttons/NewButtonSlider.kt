package com.example.gui.buttons

import com.example.utils.ColorCode
import com.example.utils.MathUtils
import com.example.utils.Utils
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiButton
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.util.MathHelper
import org.lwjgl.input.Mouse
import java.awt.Color

class NewButtonSlider(
    x: Double,
    y: Double,
    width: Int,
    height: Int,
    value: Float,
    min: Float,
    max: Float,
    step: Float,
    sliderCallback: (Float) -> Unit
) :
    GuiButton(0, x.toInt(), y.toInt(), "") {
    private val min: Float
    private val max: Float
    private val step: Float
    private val sliderCallback: (Float) -> Unit
    private var prefix = ""
    private var dragging = false
    private var normalizedValue: Float

    init {
        this.width = width
        this.height = height
        this.sliderCallback = sliderCallback
        this.min = min
        this.max = max
        this.step = step
        normalizedValue = MathUtils.normalizeSliderValue(value, min, max, step)
        displayString = Utils.roundForString(value, 2)
    }

    override fun drawButton(mc: Minecraft, mouseX: Int, mouseY: Int) {
        mc.textureManager.bindTexture(buttonTextures)
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f)
        hovered =
            mouseX >= xPosition && mouseY >= yPosition && mouseX < xPosition + width && mouseY < yPosition + height
        GlStateManager.enableBlend()
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0)
        GlStateManager.blendFunc(770, 771)
        var boxAlpha = 100
        if (hovered) {
            boxAlpha = 170
        }
        drawRect(xPosition, yPosition, xPosition + width, yPosition + height, Color(150, 236, 255, boxAlpha).rgb)
        mouseDragged(mc, mouseX, mouseY)
        var j = 14737632
        if (packedFGColour != 0) {
            j = packedFGColour
        } else if (!enabled) {
            j = 10526880
        } else if (hovered) {
            j = 16777120
        }
        drawCenteredString(
            mc.fontRendererObj,
            displayString, xPosition + width / 2, yPosition + (height - 8) / 2, j
        )
    }

    override fun getHoverState(mouseOver: Boolean): Int {
        return 0
    }

    override fun mouseDragged(mc: Minecraft, mouseX: Int, mouseY: Int) {
        if (visible) {
            val sr = ScaledResolution(mc)
            val minecraftScale = sr.scaleFactor.toFloat()
            val floatMouseX = Mouse.getX() / minecraftScale
            if (dragging) {
                normalizedValue = (floatMouseX - (xPosition + 4)) / (width - 8).toFloat()
                normalizedValue = MathHelper.clamp_float(normalizedValue, 0.0f, 1.0f)
                onUpdate()
            }
            mc.textureManager.bindTexture(buttonTextures)
            GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f)
            drawRect(
                xPosition + (normalizedValue * (width - 8).toFloat()).toInt() + 1,
                yPosition,
                xPosition + (normalizedValue * (width - 8).toFloat()).toInt() + 7,
                yPosition + height,
                ColorCode.GRAY.color
            )
        }
    }

    override fun mousePressed(mc: Minecraft, mouseX: Int, mouseY: Int): Boolean {
        return if (super.mousePressed(mc, mouseX, mouseY)) {
            normalizedValue = (mouseX - (xPosition + 4)).toFloat() / (width - 8).toFloat()
            normalizedValue = MathHelper.clamp_float(normalizedValue, 0.0f, 1.0f)
            onUpdate()
            dragging = true
            true
        } else {
            false
        }
    }

    override fun mouseReleased(mouseX: Int, mouseY: Int) {
        dragging = false
    }

    fun setPrefix(text: String): NewButtonSlider {
        prefix = text
        updateDisplayString()
        return this
    }

    private fun onUpdate() {
        sliderCallback(denormalize())
        updateDisplayString()
    }

    private fun updateDisplayString() {
        displayString = prefix + Utils.roundForString(denormalize(), 2)
    }

    fun denormalize(): Float {
        return MathUtils.denormalizeSliderValue(normalizedValue, min, max, step)
    }
}