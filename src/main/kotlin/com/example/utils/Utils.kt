package com.example.utils

import com.example.config.Config
import com.example.core.Feature
import com.example.misc.scheduler.NewScheduler
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.util.MathHelper
import net.minecraft.util.ResourceLocation
import org.apache.commons.io.IOUtils
import org.lwjgl.opengl.GL11
import java.awt.geom.Point2D
import java.io.BufferedInputStream
import java.io.IOException
import javax.vecmath.Vector3d

object Utils {
    @Throws(IOException::class)
    fun toByteArray(inputStream: BufferedInputStream) = inputStream.use { IOUtils.toByteArray(it) }!!

    fun getPartialTicks() = Minecraft.getMinecraft().timer.renderPartialTicks

    fun getCurrentTick() = NewScheduler.INSTANCE.totalTicks

    private val interpolatedPlayerPosition = Vector3d()
    private var lastTick: Long = 0
    private var lastPartialTicks = 0f

    var blockNextClick: Boolean = false

    fun getPlayerViewPosition(): Vector3d {
        val currentTick: Long = getCurrentTick()
        val currentPartialTicks: Float = getPartialTicks()
        if (currentTick != lastTick || currentPartialTicks != lastPartialTicks) {
            val renderViewEntity = Minecraft.getMinecraft().renderViewEntity
            interpolatedPlayerPosition.x = MathUtils.interpolateX(renderViewEntity, currentPartialTicks)
            interpolatedPlayerPosition.y = MathUtils.interpolateY(renderViewEntity, currentPartialTicks)
            interpolatedPlayerPosition.z = MathUtils.interpolateZ(renderViewEntity, currentPartialTicks)
            lastTick = currentTick
            lastPartialTicks = currentPartialTicks
        }
        return interpolatedPlayerPosition
    }

    private var depthEnabled = false
    private var blendEnabled = false
    private var alphaEnabled = false
    private var blendFunctionSrcFactor = 0
    private var blendFunctionDstFactor = 0

    fun enableStandardGLOptions() {
        depthEnabled = GL11.glIsEnabled(GL11.GL_DEPTH_TEST)
        blendEnabled = GL11.glIsEnabled(GL11.GL_BLEND)
        alphaEnabled = GL11.glIsEnabled(GL11.GL_ALPHA_TEST)
        blendFunctionSrcFactor = GL11.glGetInteger(GL11.GL_BLEND_SRC)
        blendFunctionDstFactor = GL11.glGetInteger(GL11.GL_BLEND_DST)
        GlStateManager.disableDepth()
        GlStateManager.enableBlend()
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
        GlStateManager.enableAlpha()
        GlStateManager.color(1f, 1f, 1f, 1f)
    }

    fun restoreGLOptions() {
        if (depthEnabled) {
            GlStateManager.enableDepth()
        }
        if (!alphaEnabled) {
            GlStateManager.disableAlpha()
        }
        if (!blendEnabled) {
            GlStateManager.disableBlend()
        }
        GlStateManager.blendFunc(blendFunctionSrcFactor, blendFunctionDstFactor)
    }

    fun playSound(sound: String, volume: Double, pitch: Double) {
        Minecraft.getMinecraft().thePlayer.playSound(sound, volume.toFloat(), pitch.toFloat())
    }

    /**
     * Rounds a float value for when it is being displayed as a string.
     *
     *
     * For example, if the given value is 123.456789 and the decimal places is 2, this will round
     * to 1.23.
     *
     * @param value         The value to round
     * @param decimalPlaces The decimal places to round to
     * @return A string representation of the value rounded
     */
    fun roundForString(value: Float, decimalPlaces: Int): String? {
        return String.format("%." + decimalPlaces + "f", value)
    }

    fun setClosestAnchorPoint(feature: Feature) {
        val x1: Float = feature.getActualX()
        val y1: Float = feature.getActualY()
        val sr = ScaledResolution(Minecraft.getMinecraft())
        val maxX = sr.scaledWidth
        val maxY = sr.scaledHeight
        var shortestDistance = -1.0
        var closestAnchorPoint = Config.AnchorPoint.BOTTOM_MIDDLE // default
        for (point in Config.AnchorPoint.values()) {
            val distance = Point2D.distance(x1.toDouble(), y1.toDouble(), point.getX(maxX).toDouble(), point.getY(maxY).toDouble())
            if (shortestDistance == -1.0 || distance < shortestDistance) {
                closestAnchorPoint = point
                shortestDistance = distance
            }
        }
        if (feature.anchorPoint === closestAnchorPoint) {
            return
        }
        val targetX: Float = feature.getActualX()
        val targetY: Float = feature.getActualY()
        val x: Float = targetX - closestAnchorPoint.getX(maxX)
        val y: Float = targetY - closestAnchorPoint.getY(maxY)
        feature.anchorPoint = closestAnchorPoint
        feature.coordinates = Pair(x, y)
    }

    fun normalizeValueNoStep(value: Float): Float {
        return MathHelper.clamp_float(
            (snapNearDefaultValue(value) - Config.GUI_SCALE_MINIMUM) / (Config.GUI_SCALE_MAXIMUM - Config.GUI_SCALE_MINIMUM),
            0.0f,
            1.0f
        )
    }

    private fun snapNearDefaultValue(value: Float): Float {
        return if (value != 1f && value > 1 - 0.05 && value < 1 + 0.05) {
            1f
        } else value
    }

    val ICONS = ResourceLocation("examplemod", "craftingpatterns.png")
}