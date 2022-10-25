package com.example.gui.buttons

import com.example.config.Config
import com.example.core.Feature
import com.example.utils.ColorCode
import com.example.utils.DrawUtils
import net.minecraft.client.Minecraft
import net.minecraft.client.audio.SoundHandler
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.renderer.GlStateManager
import org.lwjgl.input.Mouse

/**
 * Create a button that allows you to change the location of a GUI element.
 */
class ButtonLocation(feature: Feature) : ButtonFeature(-1, 0, 0, null, feature) {
    var boxXOne = 0f
    var boxXTwo = 0f
    var boxYOne = 0f
    var boxYTwo = 0f
    var scale = 0f

    override fun drawButton(mc: Minecraft, mouseX: Int, mouseY: Int) {
        val scale: Float = feature.guiScale
        GlStateManager.pushMatrix()
        GlStateManager.scale(scale, scale, 1f)
//        if (feature == Feature.DEFENCE_ICON) { // this one is just a little different
//            main.getRenderListener().drawIcon(scale, mc, this)
//        } else {
            feature.draw(scale, mc, this)
//        }
        GlStateManager.popMatrix()
        if (hovered) {
            lastHoveredFeature = feature
        }
    }

    /**
     * This just updates the hovered status and draws the box around each feature. To avoid repetitive code.
     */
    fun checkHoveredAndDrawBox(boxXOne: Float, boxXTwo: Float, boxYOne: Float, boxYTwo: Float, scale: Float) {
        val sr = ScaledResolution(Minecraft.getMinecraft())
        val minecraftScale: Float = sr.scaleFactor.toFloat()
        val floatMouseX: Float = Mouse.getX() / minecraftScale
        val floatMouseY: Float = (Minecraft.getMinecraft().displayHeight - Mouse.getY()) / minecraftScale
        hovered =
            floatMouseX >= boxXOne * scale && floatMouseY >= boxYOne * scale && floatMouseX < boxXTwo * scale && floatMouseY < boxYTwo * scale
        var boxAlpha = 70
        if (hovered) {
            boxAlpha = 120
        }
        val boxColor: Int = ColorCode.GRAY.getColor(boxAlpha)
        DrawUtils.drawRectAbsolute(boxXOne.toDouble(), boxYOne.toDouble(), boxXTwo.toDouble(), boxYTwo.toDouble(), boxColor)
        this.boxXOne = boxXOne
        this.boxXTwo = boxXTwo
        this.boxYOne = boxYOne
        this.boxYTwo = boxYTwo
        this.scale = scale
    }

    fun checkHoveredAndDrawBox(
        boxXOne: Float,
        boxXTwo: Float,
        boxYOne: Float,
        boxYTwo: Float,
        scale: Float,
        scaleX: Float,
        scaleY: Float
    ) {
        val sr = ScaledResolution(Minecraft.getMinecraft())
        val minecraftScale: Float = sr.scaleFactor.toFloat()
        val floatMouseX: Float = Mouse.getX() / minecraftScale
        val floatMouseY: Float = (Minecraft.getMinecraft().displayHeight - Mouse.getY()) / minecraftScale
        hovered =
            floatMouseX >= boxXOne * scale * scaleX && floatMouseY >= boxYOne * scale * scaleY && floatMouseX < boxXTwo * scale * scaleX && floatMouseY < boxYTwo * scale * scaleY
        var boxAlpha = 70
        if (hovered) {
            boxAlpha = 120
        }
        val boxColor: Int = ColorCode.GRAY.getColor(boxAlpha)
        DrawUtils.drawRectAbsolute(boxXOne.toDouble(), boxYOne.toDouble(), boxXTwo.toDouble(), boxYTwo.toDouble(), boxColor)
        this.boxXOne = boxXOne
        this.boxXTwo = boxXTwo
        this.boxYOne = boxYOne
        this.boxYTwo = boxYTwo
        this.scale = scale
    }

    /**
     * Because the box changes with the scale, have to override this.
     */
    override fun mousePressed(mc: Minecraft?, mouseX: Int, mouseY: Int): Boolean {
        return enabled && visible && hovered
    }

    override fun playPressSound(soundHandlerIn: SoundHandler?) {}

    companion object {
        // So we know the latest hovered feature (used for arrow key movement).
        var lastHoveredFeature: Feature? = null
    }
}