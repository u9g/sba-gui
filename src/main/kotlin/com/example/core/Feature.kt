package com.example.core

import com.example.config.Config
import com.example.gui.buttons.ButtonLocation
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.ScaledResolution

abstract class Feature(val message: String,
                       val isGuiFeature: Boolean,
                       private val defaultAnchorPoint: Config.AnchorPoint = Config.AnchorPoint.BOTTOM_MIDDLE,
                       private val defaultCoordinates: Pair<Float, Float> = Pair(0f, 0f)) {
    var anchorPoint = defaultAnchorPoint
    var coordinates = defaultCoordinates
    var guiScale = Config.guiScale
    val disabled = false

    abstract fun draw(scale: Float, mc: Minecraft, buttonLocation: ButtonLocation?)

    fun getActualX(): Float {
        val maxX = ScaledResolution(Minecraft.getMinecraft()).scaledWidth
        return this.anchorPoint.getX(maxX) + this.coordinates.first
    }

    fun getActualY(): Float {
        val maxY = ScaledResolution(Minecraft.getMinecraft()).scaledHeight
        return this.anchorPoint.getY(maxY) + this.coordinates.second
    }

    open fun resetPosition() {
        anchorPoint = defaultAnchorPoint
        coordinates = defaultCoordinates
        guiScale = Config.guiScale
    }
}