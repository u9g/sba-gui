package com.example.core.features

import com.example.core.Feature
import com.example.gui.buttons.ButtonLocation
import net.minecraft.client.Minecraft

class BarFeature(message: String,
                 private val defaultBarSize: Pair<Float, Float>,
                 var barSize: Pair<Float, Float> = defaultBarSize,
    override var isChroma: Boolean,
    override var staticColor: Int
) : Feature(message, true), ColoredFeature {
    override fun draw(scale: Float, mc: Minecraft, buttonLocation: ButtonLocation?) {
        TODO("Not yet implemented")
    }

    fun getSizesX(): Float {
        return this.barSize.first.coerceIn(.25f, 1f)
    }

    fun getSizesY(): Float {
        return this.barSize.second.coerceIn(.25f, 1f)
    }

    override val colorsRestricted: Boolean
        get() = true

    override fun resetPosition() {
        barSize = defaultBarSize
        super.resetPosition()
    }
}
