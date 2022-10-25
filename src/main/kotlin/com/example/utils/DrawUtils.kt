package com.example.utils

import com.example.config.Config
import com.example.core.chroma.ManualChromaManager
import com.example.shader.ShaderManager
import com.example.shader.chroma.Chroma3DShader
import com.example.shader.chroma.ChromaScreenShader
import com.example.shader.chroma.ChromaScreenTexturedShader
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.util.StringUtils
import org.lwjgl.BufferUtils
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL13
import org.lwjgl.util.vector.Matrix4f
import java.awt.Color
import javax.vecmath.Vector3d

object DrawUtils {
    private const val HALF_PI = Math.PI / 2.0
    private const val PI = Math.PI
    private val tessellator = Tessellator.getInstance()
    private val worldRenderer = tessellator.worldRenderer
    private var previousTextureState = false
    private const val previousBlendState = false
    private var previousCullState = false
    fun drawScaledCustomSizeModalRect(
        x: Float,
        y: Float,
        u: Float,
        v: Float,
        uWidth: Float,
        vHeight: Float,
        width: Float,
        height: Float,
        tileWidth: Float,
        tileHeight: Float,
        linearTexture: Boolean
    ) {
        if (linearTexture) {
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR)
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR)
        }
        val f = 1.0f / tileWidth
        val f1 = 1.0f / tileHeight
        val tessellator = Tessellator.getInstance()
        val worldrenderer = tessellator.worldRenderer
        worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX)
        worldrenderer.pos(x.toDouble(), (y + height).toDouble(), 0.0)
            .tex((u * f).toDouble(), ((v + vHeight) * f1).toDouble()).endVertex()
        worldrenderer.pos((x + width).toDouble(), (y + height).toDouble(), 0.0)
            .tex(((u + uWidth) * f).toDouble(), ((v + vHeight) * f1).toDouble()).endVertex()
        worldrenderer.pos((x + width).toDouble(), y.toDouble(), 0.0)
            .tex(((u + uWidth) * f).toDouble(), (v * f1).toDouble()).endVertex()
        worldrenderer.pos(x.toDouble(), y.toDouble(), 0.0).tex((u * f).toDouble(), (v * f1).toDouble()).endVertex()
        tessellator.draw()
        if (linearTexture) {
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST)
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST)
        }
    }

    fun drawCylinder(x: Double, y: Double, z: Double, radius: Float, height: Float, color: SkyblockColor) {
        var x = x
        var y = y
        var z = z
        begin3D(GL11.GL_QUADS, color)
        val viewPosition: Vector3d = Utils.getPlayerViewPosition()

        // Calculate the heading of the player
        val startAngle = Math.atan2(viewPosition.z - z, viewPosition.x - x) + Math.PI
        x -= viewPosition.x
        y -= viewPosition.y
        z -= viewPosition.z

        // This draws all the segments back-to-front to avoid depth issues
        val segments = 64
        val angleStep = Math.PI * 2.0 / segments.toDouble()
        for (segment in 0 until segments / 2) {
            val previousAngleOffset = segment * angleStep
            val currentAngleOffset = (segment + 1) * angleStep

            // Draw the positive side of this offset
            var previousRotatedX = x + radius * Math.cos(startAngle + previousAngleOffset)
            var previousRotatedZ = z + radius * Math.sin(startAngle + previousAngleOffset)
            var rotatedX = x + radius * Math.cos(startAngle + currentAngleOffset)
            var rotatedZ = z + radius * Math.sin(startAngle + currentAngleOffset)
            add3DVertex(previousRotatedX, y + height, previousRotatedZ, color)
            add3DVertex(rotatedX, y + height, rotatedZ, color)
            add3DVertex(rotatedX, y, rotatedZ, color)
            add3DVertex(previousRotatedX, y, previousRotatedZ, color)

            // Draw the negative side of this offset
            previousRotatedX = x + radius * Math.cos(startAngle - previousAngleOffset)
            previousRotatedZ = z + radius * Math.sin(startAngle - previousAngleOffset)
            rotatedX = x + radius * Math.cos(startAngle - currentAngleOffset)
            rotatedZ = z + radius * Math.sin(startAngle - currentAngleOffset)
            add3DVertex(previousRotatedX, y + height, previousRotatedZ, color)
            add3DVertex(previousRotatedX, y, previousRotatedZ, color)
            add3DVertex(rotatedX, y, rotatedZ, color)
            add3DVertex(rotatedX, y + height, rotatedZ, color)
        }
        end(color)
    }

    fun begin2D(drawType: Int, color: SkyblockColor) {
        if (color.drawMulticolorManually()) {
            worldRenderer.begin(drawType, DefaultVertexFormats.POSITION_COLOR)
            GlStateManager.shadeModel(GL11.GL_SMOOTH)
        } else {
            worldRenderer.begin(drawType, DefaultVertexFormats.POSITION)
            if (color.drawMulticolorUsingShader()) {
                ColorUtils.bindWhite()
                if (GlStateManager.textureState[GlStateManager.activeTextureUnit].texture2DState.currentState) {
                    ShaderManager.INSTANCE.enableShader(ChromaScreenTexturedShader::class.java)
                } else {
                    ShaderManager.INSTANCE.enableShader(ChromaScreenShader::class.java)
                }
            } else {
                ColorUtils.bindColor(color.color)
            }
        }
    }

    fun begin3D(drawType: Int, color: SkyblockColor) {
        if (color.drawMulticolorManually()) {
            worldRenderer.begin(drawType, DefaultVertexFormats.POSITION_COLOR)
            GlStateManager.shadeModel(GL11.GL_SMOOTH)
        } else {
            worldRenderer.begin(drawType, DefaultVertexFormats.POSITION)
            if (color.drawMulticolorUsingShader()) {
                ShaderManager.INSTANCE.enableShader(Chroma3DShader::class.java)?.alpha = ColorUtils.getAlphaFloat(color.color)
            } else {
                ColorUtils.bindColor(color.color)
            }
        }
    }

    fun end(color: SkyblockColor) {
        if (color.drawMulticolorManually()) {
            tessellator.draw()
            GlStateManager.shadeModel(GL11.GL_FLAT)
        } else {
            tessellator.draw()
            if (color.drawMulticolorUsingShader()) {
                ShaderManager.INSTANCE.disableShader()
            }
        }
    }

    /**
     * Draws a textured rectangle at z = 0. Args: x, y, u, v, width, height, textureWidth, textureHeight
     */
    @JvmOverloads
    fun drawModalRectWithCustomSizedTexture(
        x: Float,
        y: Float,
        u: Float,
        v: Float,
        width: Float,
        height: Float,
        textureWidth: Float,
        textureHeight: Float,
        linearTexture: Boolean = false
    ) {
        if (linearTexture) {
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR)
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR)
        }
        val f = 1.0f / textureWidth
        val f1 = 1.0f / textureHeight
        val tessellator = Tessellator.getInstance()
        val worldrenderer = tessellator.worldRenderer
        worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX)
        worldrenderer.pos(x.toDouble(), (y + height).toDouble(), 0.0)
            .tex((u * f).toDouble(), ((v + height) * f1).toDouble()).endVertex()
        worldrenderer.pos((x + width).toDouble(), (y + height).toDouble(), 0.0)
            .tex(((u + width) * f).toDouble(), ((v + height) * f1).toDouble()).endVertex()
        worldrenderer.pos((x + width).toDouble(), y.toDouble(), 0.0)
            .tex(((u + width) * f).toDouble(), (v * f1).toDouble()).endVertex()
        worldrenderer.pos(x.toDouble(), y.toDouble(), 0.0).tex((u * f).toDouble(), (v * f1).toDouble()).endVertex()
        tessellator.draw()
        if (linearTexture) {
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST)
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST)
        }
    }
    /**
     * Draws a rectangle using absolute coordinates & a color.
     *
     * See [DrawUtils.drawRect] to use width/height instead.
     */
    /**
     * Draws a rectangle using absolute coordinates & a color.
     *
     * See [DrawUtils.drawRect] to use width/height instead.
     */
    @JvmOverloads
    fun drawRectAbsolute(
        left: Double,
        top: Double,
        right: Double,
        bottom: Double,
        color: Int,
        chroma: Boolean = false
    ) {
        var left = left
        var top = top
        var right = right
        var bottom = bottom
        if (left < right) {
            val savedLeft = left
            left = right
            right = savedLeft
        }
        if (top < bottom) {
            val savedTop = top
            top = bottom
            bottom = savedTop
        }
        drawRectInternal(left, top, right - left, bottom - top, color, chroma)
    }

    /**
     * Draws a rectangle using absolute a starting position and a width/height.
     *
     * See [DrawUtils.drawRectAbsolute] to use absolute coordinates instead.
     */
    fun drawRect(x: Double, y: Double, w: Double, h: Double, color: SkyblockColor, rounding: Int) {
        drawRectInternal(x, y, w, h, color, rounding)
    }

    /**
     * Draws a rectangle using absolute a starting position and a width/height.
     *
     * See [DrawUtils.drawRectAbsolute] to use absolute coordinates instead.
     */
    fun drawRect(x: Double, y: Double, w: Double, h: Double, color: Int) {
        drawRectInternal(x, y, w, h, color, false)
    }

    /**
     * Draws a rectangle using absolute a starting position and a width/height.
     *
     * See [DrawUtils.drawRectAbsolute] to use absolute coordinates instead.
     */
    fun drawRect(x: Double, y: Double, w: Double, h: Double, color: Int, chroma: Boolean) {
        drawRectInternal(x, y, w, h, color, chroma)
    }

    /**
     * Draws a solid color rectangle with the specified coordinates and color (ARGB format). Args: x1, y1, x2, y2, color
     */
    private fun drawRectInternal(x: Double, y: Double, w: Double, h: Double, color: Int, chroma: Boolean) {
        drawRectInternal(
            x,
            y,
            w,
            h,
            ColorUtils.getDummySkyblockColor(
                if (chroma) SkyblockColor.ColorAnimation.CHROMA else SkyblockColor.ColorAnimation.NONE,
                color
            )
        )
    }

    private fun drawRectInternal(x: Double, y: Double, w: Double, h: Double, color: SkyblockColor) {
        drawRectInternal(x, y, w, h, color, 0)
    }

    /**
     * Draws a solid color rectangle with the specified coordinates and color (ARGB format). Args: x1, y1, x2, y2, color
     */
    private fun drawRectInternal(x: Double, y: Double, w: Double, h: Double, color: SkyblockColor, rounding: Int) {
        if (rounding > 0) {
            drawRoundedRectangle(x, y, w, h, color, rounding.toDouble())
            return
        }
        GlStateManager.enableBlend()
        GlStateManager.disableTexture2D()
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0)
        begin2D(GL11.GL_QUADS, color)
        addQuadVertices(x, y, w, h, color)
        end(color)
        GlStateManager.enableTexture2D()
        GlStateManager.disableBlend()
    }

    private fun addQuadVertices(x: Double, y: Double, w: Double, h: Double, color: SkyblockColor) {
        addQuadVerticesAbsolute(x, y, x + w, y + h, color)
    }

    private fun addQuadVerticesAbsolute(
        left: Double,
        top: Double,
        right: Double,
        bottom: Double,
        color: SkyblockColor
    ) {
        addVertex(left, bottom, color)
        addVertex(right, bottom, color)
        addVertex(right, top, color)
        addVertex(left, top, color)
    }

    private fun addVertex(x: Double, y: Double, color: SkyblockColor) {
        if (color.drawMulticolorManually()) {
            val colorInt = color.getColorAtPosition(x.toFloat(), y.toFloat())
            worldRenderer.pos(x, y, 0.0).color(
                ColorUtils.getRed(colorInt),
                ColorUtils.getGreen(colorInt),
                ColorUtils.getBlue(colorInt),
                ColorUtils.getAlpha(colorInt)
            ).endVertex()
        } else {
            worldRenderer.pos(x, y, 0.0).endVertex()
        }
    }

    private fun add3DVertex(x: Double, y: Double, z: Double, color: SkyblockColor) {
        if (color.drawMulticolorManually()) {
            // Add back the player's position to display the correct color
            val viewPosition: Vector3d = Utils.getPlayerViewPosition()
            val colorInt = color.getColorAtPosition(x + viewPosition.x, y + viewPosition.y, z + viewPosition.z)
            worldRenderer.pos(x, y, z).color(
                ColorUtils.getRed(colorInt),
                ColorUtils.getGreen(colorInt),
                ColorUtils.getBlue(colorInt),
                ColorUtils.getAlpha(colorInt)
            ).endVertex()
        } else {
            worldRenderer.pos(x, y, z).endVertex()
        }
    }

    private fun drawRoundedRectangle(
        x: Double,
        y: Double,
        w: Double,
        h: Double,
        color: SkyblockColor,
        rounding: Double
    ) {
        enableBlend()
        disableCull()
        disableTexture()
        var x1: Double
        var y1: Double
        var x2: Double
        var y2: Double
        begin2D(GL11.GL_QUADS, color)
        // Main vertical rectangle
        x1 = x + rounding
        x2 = x + w - rounding
        y1 = y
        y2 = y + h
        addVertex(x1, y2, color)
        addVertex(x2, y2, color)
        addVertex(x2, y1, color)
        addVertex(x1, y1, color)

        // Left rectangle
        x1 = x
        x2 = x + rounding
        y1 = y + rounding
        y2 = y + h - rounding
        addVertex(x1, y2, color)
        addVertex(x2, y2, color)
        addVertex(x2, y1, color)
        addVertex(x1, y1, color)

        // Right rectangle
        x1 = x + w - rounding
        x2 = x + w
        y1 = y + rounding
        y2 = y + h - rounding
        addVertex(x1, y2, color)
        addVertex(x2, y2, color)
        addVertex(x2, y1, color)
        addVertex(x1, y1, color)
        end(color)
        val segments = 64
        val angleStep = HALF_PI / segments.toFloat()
        begin2D(GL11.GL_TRIANGLE_FAN, color)
        // Top left corner
        var startAngle = -HALF_PI
        var startX = x + rounding
        var startY = y + rounding
        addVertex(startX, startY, color)
        for (segment in 0..segments) {
            val angle = startAngle - angleStep * segment
            addVertex(startX + rounding * Math.cos(angle), startY + rounding * Math.sin(angle), color)
        }
        end(color)
        begin2D(GL11.GL_TRIANGLE_FAN, color)
        // Top right corner
        startAngle = 0.0
        startX = x + w - rounding
        startY = y + rounding
        addVertex(startX, startY, color)
        for (segment in 0..segments) {
            val angle = startAngle - angleStep * segment
            addVertex(startX + rounding * Math.cos(angle), startY + rounding * Math.sin(angle), color)
        }
        end(color)
        begin2D(GL11.GL_TRIANGLE_FAN, color)
        // Bottom right corner
        startAngle = HALF_PI
        startX = x + w - rounding
        startY = y + h - rounding
        addVertex(startX, startY, color)
        for (segment in 0..segments) {
            val angle = startAngle - angleStep * segment
            addVertex(startX + rounding * Math.cos(angle), startY + rounding * Math.sin(angle), color)
        }
        end(color)
        begin2D(GL11.GL_TRIANGLE_FAN, color)
        // Bottom right corner
        startAngle = PI
        startX = x + rounding
        startY = y + h - rounding
        addVertex(startX, startY, color)
        for (segment in 0..segments) {
            val angle = startAngle - angleStep * segment
            addVertex(startX + rounding * Math.cos(angle), startY + rounding * Math.sin(angle), color)
        }
        end(color)
        restoreCull()
        restoreTexture()
        restoreBlend()
    }

    /**
     * Draws a solid color rectangle with the specified coordinates and color (ARGB format). Args: x1, y1, x2, y2, color
     */
    fun drawRectOutline(x: Float, y: Float, w: Int, h: Int, thickness: Int, color: Int, chroma: Boolean) {
        drawRectOutline(x, y, w, h, thickness, ColorUtils.getDummySkyblockColor(color, chroma))
    }

    /**
     * Draws a solid color rectangle with the specified coordinates and color (ARGB format). Args: x1, y1, x2, y2, color
     */
    fun drawRectOutline(x: Float, y: Float, w: Int, h: Int, thickness: Int, color: SkyblockColor) {
        GlStateManager.enableBlend()
        GlStateManager.disableTexture2D()
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0)
        begin2D(GL11.GL_QUADS, color)
        if (color.drawMulticolorManually()) {
            drawSegmentedLineVertical(x - thickness, y, thickness.toFloat(), h.toFloat(), color)
            drawSegmentedLineHorizontal(
                x - thickness,
                y - thickness,
                (w + thickness * 2).toFloat(),
                thickness.toFloat(),
                color
            )
            drawSegmentedLineVertical(x + w, y, thickness.toFloat(), h.toFloat(), color)
            drawSegmentedLineHorizontal(x - thickness, y + h, (w + thickness * 2).toFloat(), thickness.toFloat(), color)
        } else {
            addQuadVertices((x - thickness).toDouble(), y.toDouble(), thickness.toDouble(), h.toDouble(), color)
            addQuadVertices(
                (x - thickness).toDouble(),
                (y - thickness).toDouble(),
                (w + thickness * 2).toDouble(),
                thickness.toDouble(),
                color
            )
            addQuadVertices((x + w).toDouble(), y.toDouble(), thickness.toDouble(), h.toDouble(), color)
            addQuadVertices(
                (x - thickness).toDouble(),
                (y + h).toDouble(),
                (w + thickness * 2).toDouble(),
                thickness.toDouble(),
                color
            )
        }
        end(color)
        GlStateManager.enableTexture2D()
        GlStateManager.disableBlend()
    }

    fun drawSegmentedLineHorizontal(x: Float, y: Float, w: Float, h: Float, color: SkyblockColor) {
        val segments = (w * ManualChromaManager.featureScale / 10) as Int
        val length = w / segments
        for (segment in 0 until segments) {
            val start = x + length * segment
            addQuadVertices(start.toDouble(), y.toDouble(), length.toDouble(), h.toDouble(), color)
        }
    }

    fun drawSegmentedLineVertical(x: Float, y: Float, w: Float, h: Float, color: SkyblockColor) {
        val segments = (h * ManualChromaManager.featureScale / 10) as Int
        val length = h / segments
        for (segment in 0 until segments) {
            val start = y + length * segment
            addQuadVertices(x.toDouble(), start.toDouble(), w.toDouble(), length.toDouble(), color)
        }
    }

    fun drawText(text: String?, x: Float, y: Float, color: Int) {
        if (text == null) {
            return
        }
        val fontRenderer = Minecraft.getMinecraft().fontRendererObj
        if (Config.textStyle == Config.TextStyle.STYLE_TWO) {
            val colorAlpha = Math.max(ColorUtils.getAlpha(color), 4)
            val colorBlack = Color(0f, 0f, 0f, colorAlpha / 255f).rgb
            val strippedText: String = StringUtils.stripControlCodes(text)
            fontRenderer.drawString(strippedText, x + 1, y + 0, colorBlack, false)
            fontRenderer.drawString(strippedText, x - 1, y + 0, colorBlack, false)
            fontRenderer.drawString(strippedText, x + 0, y + 1, colorBlack, false)
            fontRenderer.drawString(strippedText, x + 0, y - 1, colorBlack, false)
            fontRenderer.drawString(text, x + 0, y + 0, color, false)
        } else {
            fontRenderer.drawString(text, x + 0, y + 0, color, true)
        }
    }

    fun drawCenteredText(text: String?, x: Float, y: Float, color: Int) {
        drawText(text, x - Minecraft.getMinecraft().fontRendererObj.getStringWidth(text) / 2f, y, color)
    }

    fun printCurrentGLTransformations() {
        val buf = BufferUtils.createFloatBuffer(16)
        GL11.glGetFloat(GL11.GL_MODELVIEW_MATRIX, buf)
        buf.rewind()
        val mat = Matrix4f()
        mat.load(buf)
        val x = mat.m30
        val y = mat.m31
        val z = mat.m32
        val scale = Math.sqrt((mat.m00 * mat.m00 + mat.m01 * mat.m01 + mat.m02 * mat.m02).toDouble()).toFloat()
    }

    fun enableBlend() {
//        previousCullState = GlStateManager.blend.cullFace.currentState;
        GlStateManager.enableBlend()
        GlStateManager.enableAlpha()
    }

    fun disableBlend() {
//        previousCullState = GlStateManager.cullState.cullFace.currentState;
        GlStateManager.disableBlend()
    }

    fun restoreBlend() {}
    fun enableCull() {
        previousCullState = GlStateManager.cullState.cullFace.currentState
        GlStateManager.enableCull()
    }

    fun disableCull() {
        previousCullState = GlStateManager.cullState.cullFace.currentState
        GlStateManager.disableCull()
    }

    fun restoreCull() {
        if (previousCullState) {
            GlStateManager.enableCull()
        } else {
            GlStateManager.disableCull()
        }
    }

    fun enableTexture() {
        previousTextureState = GlStateManager.textureState[GlStateManager.activeTextureUnit].texture2DState.currentState
        GlStateManager.enableTexture2D()
    }

    fun disableTexture() {
        previousTextureState = GlStateManager.textureState[GlStateManager.activeTextureUnit].texture2DState.currentState
        GlStateManager.disableTexture2D()
    }

    fun restoreTexture() {
        if (previousTextureState) {
            GlStateManager.enableTexture2D()
        } else {
            GlStateManager.disableTexture2D()
        }
    }

    private val BUF_FLOAT_4 = BufferUtils.createFloatBuffer(4)
    fun enableOutlineMode() {
        GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL13.GL_COMBINE)
        GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL13.GL_COMBINE_RGB, GL11.GL_REPLACE)
        GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL13.GL_SOURCE0_RGB, GL13.GL_CONSTANT)
        GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL13.GL_OPERAND0_RGB, GL11.GL_SRC_COLOR)
        GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL13.GL_COMBINE_ALPHA, GL11.GL_REPLACE)
        GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL13.GL_SOURCE0_ALPHA, GL11.GL_TEXTURE)
        GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL13.GL_OPERAND0_ALPHA, GL11.GL_SRC_ALPHA)
    }

    fun outlineColor(color: Int) {
        BUF_FLOAT_4.put(0, (color shr 16 and 255).toFloat() / 255.0f)
        BUF_FLOAT_4.put(1, (color shr 8 and 255).toFloat() / 255.0f)
        BUF_FLOAT_4.put(2, (color and 255).toFloat() / 255.0f)
        BUF_FLOAT_4.put(3, 1f)
        GL11.glTexEnv(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_COLOR, BUF_FLOAT_4)
    }

    fun disableOutlineMode() {
        GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_MODULATE)
        GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL13.GL_COMBINE_RGB, GL11.GL_MODULATE)
        GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL13.GL_SOURCE0_RGB, GL11.GL_TEXTURE)
        GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL13.GL_OPERAND0_RGB, GL11.GL_SRC_COLOR)
        GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL13.GL_COMBINE_ALPHA, GL11.GL_MODULATE)
        GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL13.GL_SOURCE0_ALPHA, GL11.GL_TEXTURE)
        GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL13.GL_OPERAND0_ALPHA, GL11.GL_SRC_ALPHA)
    }

    /**
     * Copied from Render.renderLivingLabel
     *
     * @param str the string to render
     * @param x   offset from the player's render position (eyesight)
     * @param y   offset from the player's render position (eyesight)
     * @param z   offset from the player's render position (eyesight)
     */
    fun drawTextInWorld(str: String?, x: Double, y: Double, z: Double) {
        val mc = Minecraft.getMinecraft()
        val fontrenderer = mc.fontRendererObj
        val renderManager = mc.renderManager
        val f = 1.6f
        val f1 = 0.016666668f * f
        GlStateManager.pushMatrix()
        GlStateManager.translate(x, y, z)
        GL11.glNormal3f(0.0f, 1.0f, 0.0f)
        GlStateManager.rotate(-renderManager.playerViewY, 0.0f, 1.0f, 0.0f)
        GlStateManager.rotate(renderManager.playerViewX, 1.0f, 0.0f, 0.0f)
        GlStateManager.scale(-f1, -f1, f1)
        GlStateManager.disableLighting()
        GlStateManager.depthMask(false)
        GlStateManager.disableDepth()
        GlStateManager.enableBlend()
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0)
        val tessellator = Tessellator.getInstance()
        val worldrenderer = tessellator.worldRenderer
        val j = fontrenderer.getStringWidth(str) / 2
        GlStateManager.disableTexture2D()
        worldrenderer.begin(7, DefaultVertexFormats.POSITION_COLOR)
        worldrenderer.pos((-j - 1).toDouble(), -1.0, 0.0).color(0.0f, 0.0f, 0.0f, 0.25f).endVertex()
        worldrenderer.pos((-j - 1).toDouble(), 8.0, 0.0).color(0.0f, 0.0f, 0.0f, 0.25f).endVertex()
        worldrenderer.pos((j + 1).toDouble(), 8.0, 0.0).color(0.0f, 0.0f, 0.0f, 0.25f).endVertex()
        worldrenderer.pos((j + 1).toDouble(), -1.0, 0.0).color(0.0f, 0.0f, 0.0f, 0.25f).endVertex()
        tessellator.draw()
        GlStateManager.enableTexture2D()
        fontrenderer.drawString(str, -fontrenderer.getStringWidth(str) / 2, 0, 553648127)
        GlStateManager.enableDepth()
        GlStateManager.depthMask(true)
        fontrenderer.drawString(str, -fontrenderer.getStringWidth(str) / 2, 0, -1)
        GlStateManager.enableLighting()
        GlStateManager.disableBlend()
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f)
        GlStateManager.popMatrix()
    }

    fun drawScaledString(guiScreen: GuiScreen, text: String?, y: Int, color: Int, scale: Double, xOffset: Int) {
        drawScaledString(guiScreen, text, y, color, scale, xOffset, true)
    }

    /**
     * Draws a centered string at the middle of the screen on the x axis, with a specified scale and location.
     *
     * @param text The text to draw.
     * @param y The y level to draw the text/
     * @param color The text color.
     * @param scale The scale to draw the text.
     * @param xOffset The offset from the center x that the text should be drawn at.
     */
    fun drawScaledString(
        guiScreen: GuiScreen,
        text: String?,
        y: Int,
        color: Int,
        scale: Double,
        xOffset: Int,
        centered: Boolean
    ) {
        GlStateManager.pushMatrix()
        GlStateManager.scale(scale, scale, 1.0)
        if (centered) {
            drawCenteredText(
                text,
                (Math.round(guiScreen.width.toFloat() / 2 / scale) + xOffset).toFloat(),
                Math.round(y.toFloat() / scale).toFloat(), color
            )
        } else {
            Minecraft.getMinecraft().fontRendererObj.drawString(
                text, (Math.round(guiScreen.width.toFloat() / 2 / scale) + xOffset).toFloat(),
                Math.round(y.toFloat() / scale).toFloat(), color, true
            )
        }
        GlStateManager.popMatrix()
    }
}
