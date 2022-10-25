package com.example.core.features

import com.example.config.Config
import com.example.core.Feature
import com.example.fonts.FontRendererHook
import com.example.gui.buttons.ButtonLocation
import com.example.utils.DrawUtils
import com.example.utils.Utils
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.RenderHelper
import net.minecraft.item.ItemStack
import kotlin.math.roundToInt

abstract class TextFeature(message: String,
                           override var staticColor: Int,
                           defaultAnchorPoint: Config.AnchorPoint = Config.AnchorPoint.BOTTOM_MIDDLE,
                           defaultCoordinates: Pair<Float, Float> = Pair(0f, 0f)) : Feature(message, true, defaultAnchorPoint, defaultCoordinates), ColoredFeature {
    override val colorsRestricted: Boolean
        get() = false

    override var isChroma = false

    override fun draw(scale: Float, mc: Minecraft, buttonLocation: ButtonLocation?) {
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f) // can be called once before all features are drawn
        val text = textToDraw()

        val x = getActualX()
        val y = getActualY()

        var height = 7

        var width = mc.fontRendererObj.getStringWidth(text)

        // TODO: Constant width overrides for some features.
        if (this is TextWithIconFeature) {
            width += 18
            height += 9
        }
        // TODO: MultiIcon with text like Feature.ENDSTONE_PROTECTOR_DISPLAY

        transformXY(x, width, scale)
        transformXY(y, height, scale)

        if (buttonLocation != null) {
            buttonLocation.checkHoveredAndDrawBox(x, x + width, y, y + height, scale)
            GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f)
        }

        Utils.enableStandardGLOptions()

        // TODO: Custom Icon textures like Feature.DARK_AUCTION_TIMER

        if (this is TextWithIconFeature) {
            renderItem(getItem(), x, y)
        }

        FontRendererHook.setupFeatureFont(this)
        DrawUtils.drawText(text, x, y, this.getColor())
        FontRendererHook.endFeatureFont()

        Utils.restoreGLOptions()
    }

    private fun renderItem(item: ItemStack, x: Float, y: Float) {
        GlStateManager.enableRescaleNormal()
        RenderHelper.enableGUIStandardItemLighting()
        GlStateManager.enableDepth()
        GlStateManager.pushMatrix()
        GlStateManager.translate(x, y, 0f)
        Minecraft.getMinecraft().renderItem.renderItemIntoGUI(item, 0, 0)
        GlStateManager.popMatrix()
        RenderHelper.disableStandardItemLighting()
        GlStateManager.disableRescaleNormal()
    }

    private fun transformXY(xy: Float, widthHeight: Int, scale: Float): Float {
        var xy = xy
        val minecraftScale = ScaledResolution(Minecraft.getMinecraft()).scaleFactor.toFloat()
        xy -= widthHeight / 2f * scale
        xy = (xy * minecraftScale).roundToInt() / minecraftScale
        return xy / scale
    }

    abstract fun textToDraw(): String
}