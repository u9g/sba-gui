package com.example.gui

import com.example.RenderListener
import com.example.config.Config
import com.example.core.features.ColoredFeature
import com.example.gui.buttons.ButtonColorBox
import com.example.gui.buttons.NewButtonSlider
import com.example.utils.ColorCode
import com.example.utils.ColorUtils
import com.example.utils.DrawUtils
import com.example.utils.Utils
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiButton
import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.gui.GuiTextField
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.texture.TextureUtil
import net.minecraft.util.ResourceLocation
import org.lwjgl.input.Keyboard
import java.awt.Color
import java.awt.image.BufferedImage
import java.io.IOException

class ColorSelectionGui constructor(
    // The feature that this color is for.
    val feature: ColoredFeature,
    private val lastGUI: GuiScreen?,
) :
    GuiScreen() {
    private var COLOR_PICKER_IMAGE: BufferedImage? = null

    // Previous pages for when they return.
    private var imageX = 0
    private var imageY = 0
    private var hexColorField: GuiTextField? = null
    private var chromaCheckbox: CheckBox? = null

    /**
     * Creates a gui to allow you to select a color for a specific feature.
     *
     * @param feature The feature that this color is for.
     * @param lastTab The previous tab that you came from.
     * @param lastPage The previous page.
     */
    init {
        try {
            COLOR_PICKER_IMAGE = TextureUtil.readBufferedImage(
                Minecraft.getMinecraft().resourceManager.getResource(
                    COLOR_PICKER
                ).inputStream
            )
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    override fun initGui() {
        chromaCheckbox = CheckBox(mc, width / 2 + 88, 170, 12, "Chroma", false)
        chromaCheckbox!!.value = feature.isChroma
        chromaCheckbox!!.setOnToggleListener { value ->
            feature.isChroma = value
            removeChromaButtons()
            if (value) {
                addChromaButtons()
            }
        }
        hexColorField = GuiTextField(0, Minecraft.getMinecraft().fontRendererObj, width / 2 + 110 - 50, 220, 100, 15)
        hexColorField!!.maxStringLength = 7
        hexColorField!!.isFocused = true

        // Set the current color in the text box after creating it.
        setTextBoxHex(feature.getColor())
        if (feature.colorsRestricted) {

            // This creates the 16 buttons for all the color codes.
            var collumn = 1
            var x = width / 2 - 160
            var y = 120
            for (colorCode in ColorCode.values()) {
                if (colorCode.isFormat || colorCode === ColorCode.RESET) continue
                buttonList.add(ButtonColorBox(x, y, colorCode))
                if (collumn < 6) { // 6 buttons per row.
                    collumn++ // Go to the next collumn once the 6 are over.
                    x += ButtonColorBox.WIDTH + 15 // 15 spacing.
                } else {
                    y += ButtonColorBox.HEIGHT + 20 // Go to next row.
                    collumn = 1 // Reset the collumn.
                    x = width / 2 - 160 // Reset the x vlue.
                }
            }
        }
        if (feature.isChroma && !feature.colorsRestricted
        ) {
            addChromaButtons()
        }
        Keyboard.enableRepeatEvents(true)
        super.initGui()
    }

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        // Draw background and default text.
        val startColor = Color(0, 0, 0, 128).rgb
        val endColor = Color(0, 0, 0, 192).rgb
        drawGradientRect(0, 0, width, height, startColor, endColor)
        val defaultBlue = Color(160, 225, 229, 255).rgb
        if (feature.colorsRestricted) {
            DrawUtils.drawScaledString(
                this, "Choose a Color", 90,
                defaultBlue, 1.5, 0
            )
        } else {
            val pickerWidth = COLOR_PICKER_IMAGE!!.width
            val pickerHeight = COLOR_PICKER_IMAGE!!.height
            imageX = width / 2 - 200
            imageY = 90
            if (feature.isChroma) { // Fade out color picker if chroma enabled
                GlStateManager.color(0.5f, 0.5f, 0.5f, 0.7f)
                GlStateManager.enableBlend()
            } else {
                GlStateManager.color(1f, 1f, 1f, 1f)
            }

            // Draw the color picker with no scaling so the size is the exact same.
            mc.textureManager.bindTexture(COLOR_PICKER)
            drawModalRectWithCustomSizedTexture(
                imageX,
                imageY,
                0f,
                0f,
                pickerWidth,
                pickerHeight,
                pickerWidth.toFloat(),
                pickerHeight.toFloat()
            )
            DrawUtils.drawScaledString(
                this,
                "Selected Color",
                120,
                defaultBlue,
                1.5,
                75
            )
            drawRect(width / 2 + 90, 140, width / 2 + 130, 160, feature.getColor())
            chromaCheckbox?.draw()
            if (!feature.isChroma) { // Disabled cause chroma is enabled
                DrawUtils.drawScaledString(
                    this,
                    "Set Hex Color",
                    200,
                    defaultBlue,
                    1.5,
                    75
                )
                hexColorField!!.drawTextBox()
            }
            if (feature.isChroma) {
                DrawUtils.drawScaledString(
                    this,
                    "Chroma Speed",
                    170 + 25,
                    defaultBlue,
                    1.0,
                    110
                )
                DrawUtils.drawScaledString(
                    this,
                    "Chroma Fade Width",
                    170 + 35 + 25,
                    defaultBlue,
                    1.0,
                    110
                )
            }
        }
        super.drawScreen(mouseX, mouseY, partialTicks)
    }

    @Throws(IOException::class)
    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        if (!feature.colorsRestricted && !feature.isChroma) {
            val xPixel = mouseX - imageX
            val yPixel = mouseY - imageY

            // If the mouse is over the color picker.
            if (xPixel > 0 && xPixel < COLOR_PICKER_IMAGE!!.width && yPixel > 0 && yPixel < COLOR_PICKER_IMAGE!!.height) {

                // Get the color of the clicked pixel.
                val selectedColor = COLOR_PICKER_IMAGE!!.getRGB(xPixel, yPixel)

                // Choose this color.
                if (ColorUtils.getAlpha(selectedColor) == 255) {
                    feature.staticColor = selectedColor
                    setTextBoxHex(selectedColor)
                    Utils.playSound("gui.button.press", 0.25, 1.0)
                }
            }
            hexColorField!!.mouseClicked(mouseX, mouseY, mouseButton)
        }
        chromaCheckbox?.onMouseClick(mouseX, mouseY, mouseButton)
        super.mouseClicked(mouseX, mouseY, mouseButton)
    }

    private fun setTextBoxHex(color: Int) {
        hexColorField!!.text =
            String.format(
                "#%02x%02x%02x",
                ColorUtils.getRed(color),
                ColorUtils.getGreen(color),
                ColorUtils.getBlue(color)
            )
    }

    @Throws(IOException::class)
    override fun keyTyped(typedChar: Char, keyCode: Int) {
        super.keyTyped(typedChar, keyCode)
        if (hexColorField!!.isFocused) {
            hexColorField!!.textboxKeyTyped(typedChar, keyCode)
            var text = hexColorField!!.text
            if (text.startsWith("#")) { // Get rid of the #.
                text = text.substring(1)
            }
            if (text.length == 6) {
                val typedColor: Int = try {
                    text.toInt(16) // Try to read the hex value and put it in an integer.
                } catch (ex: NumberFormatException) {
                    ex.printStackTrace() // This just means it wasn't in the format of a hex number- that's fine!
                    return
                }
                feature.staticColor = typedColor
            }
        }
    }

    @Throws(IOException::class)
    override fun actionPerformed(button: GuiButton) {
        if (button is ButtonColorBox) {
            val colorBox: ButtonColorBox = button as ButtonColorBox
            feature.isChroma = colorBox.getColor() === ColorCode.CHROMA
            feature.staticColor = colorBox.getColor().color
            mc.displayGuiScreen(null)
        }
        super.actionPerformed(button)
    }

    override fun updateScreen() {
        hexColorField!!.updateCursorCounter()
        super.updateScreen()
    }

    override fun onGuiClosed() {
        Keyboard.enableRepeatEvents(false)

        RenderListener.screenToOpen = lastGUI
    }

    private fun removeChromaButtons() {
        buttonList.removeIf { button: GuiButton? -> button is NewButtonSlider }
    }

    private fun addChromaButtons() {
        buttonList.add(NewButtonSlider(
            (width / 2 + 76).toDouble(), (170 + 35).toDouble(), 70, 15, Config.Chroma.speed,
            0.5f, 20f, 0.5f
        ) { updatedValue -> Config.Chroma.speed = updatedValue })
        buttonList.add(NewButtonSlider(
            (width / 2 + 76).toDouble(), (170 + 35 + 35).toDouble(), 70, 15, Config.Chroma.size,
            1f, 100f, 1f
        ) { updatedValue -> Config.Chroma.size = updatedValue })
    }

    companion object {
        private val COLOR_PICKER = ResourceLocation("examplemod", "gui/colorpicker.png")
    }
}
