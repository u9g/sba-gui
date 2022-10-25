package com.example.utils.draw

import com.example.utils.ColorUtils
import com.example.utils.SkyblockColor
import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.renderer.vertex.VertexFormat

open class DrawState2D : DrawState {
    constructor(
        theColor: SkyblockColor,
        theDrawType: Int,
        theFormat: VertexFormat?,
        isTextured: Boolean,
        shouldIgnoreTexture: Boolean
    ) : super(theColor, theDrawType, theFormat, isTextured, shouldIgnoreTexture) {
    }

    constructor(theColor: SkyblockColor, isTextured: Boolean, shouldIgnoreTexture: Boolean) : super(
        theColor,
        isTextured,
        shouldIgnoreTexture
    ) {
    }

    open fun newColorEnv(): DrawState2D {
        super.newColor(false)
        return this
    }

    open fun endColorEnv(): DrawState2D {
        super.endColor()
        return this
    }

    fun setColor(color: SkyblockColor): DrawState2D {
        super.reColor(color)
        return this
    }

    fun bindActualColor(): DrawState2D {
        super.bindColor(color.color)
        return this
    }

    open fun bindAnimatedColor(x: Float, y: Float): DrawState2D {
        super.bindColor(color.getColorAtPosition(x, y))
        return this
    }

    fun addColoredVertex(x: Float, y: Float): DrawState2D {
        // Add a new position in the world with the correct color
        if (canAddVertices) {
            if (color.drawMulticolorManually()) {
                val colorInt = color.getColorAtPosition(x, y)
                worldRenderer.pos(x.toDouble(), y.toDouble(), 0.0).color(
                    ColorUtils.getRed(colorInt),
                    ColorUtils.getGreen(colorInt),
                    ColorUtils.getBlue(colorInt),
                    ColorUtils.getAlpha(colorInt)
                ).endVertex()
            } else {
                worldRenderer.pos(x.toDouble(), y.toDouble(), 0.0).endVertex()
            }
        } else {
            bindAnimatedColor(x, y)
        }
        return this
    }

    companion object {
        private val worldRenderer = Tessellator.getInstance().worldRenderer
    }
}