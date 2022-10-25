package com.example.gui

import com.example.config.Config
import com.example.core.Feature
import com.example.core.FeatureManager
import com.example.core.features.BarFeature
import com.example.core.features.ColoredFeature
import com.example.core.features.TextFeature
import com.example.gui.buttons.ButtonColorWheel
import com.example.gui.buttons.ButtonEditGui
import com.example.gui.buttons.ButtonLocation
import com.example.gui.buttons.ButtonResize
import com.example.utils.ColorCode
import com.example.utils.DrawUtils
import com.example.utils.Utils
import com.google.common.collect.Sets
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiButton
import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.gui.ScaledResolution
import org.lwjgl.input.Keyboard
import org.lwjgl.input.Mouse
import java.awt.Color
import java.io.IOException
import java.util.*

internal class LocationEditGui(private val lastScreen: GuiScreen?) : GuiScreen() {
    private var editMode: EditMode? = EditMode.RESCALE
    private var showColorIcons = true
    private var enableSnapping = true
    private var showFeatureNameOnHover = false

    // The feature that is currently being dragged, or null for nothing.
    private var draggedFeature: Feature? = null

    // The feature the mouse is currently hovering over, null for nothing.
    private var hoveredFeature: Feature? = null
    private var resizing = false
    private var resizingCorner: ButtonResize.Corner? = null
    private val originalHeight = 0
    private val originalWidth = 0
    private var xOffset = 0f
    private var yOffset = 0f
    private val buttonLocations: MutableMap<Feature?, ButtonLocation> = mutableMapOf()
    private var closing = false

    override fun initGui() {
        // Add all gui elements that can be edited to the gui.
        for (feature in FeatureManager.guiFeatures) {
            if (feature is TextFeature && !feature.disabled) { // Don't display features that have been disabled
                val buttonLocation = ButtonLocation(feature)
                buttonList.add(buttonLocation)
                buttonLocations[feature] = buttonLocation
            }
        }
        if (editMode == EditMode.RESIZE_BARS) {
            addResizeButtonsToBars()
        } else if (editMode == EditMode.RESCALE) {
            addResizeButtonsToAllFeatures()
        }
        addColorWheelsToAllFeatures()
        val buttonTypes = ButtonEditGui.Companion.Type.values()
        val scaledResolution = ScaledResolution(Minecraft.getMinecraft())
        val boxHeight = 20
        val numButtons = buttonTypes.size
        var x: Int
        var y = scaledResolution.scaledHeight / 2
        // List may change later
        y -= if (numButtons % 2 == 0) {
            (Math.round(numButtons / 2f * (boxHeight + 5)) - 2.5).toInt()
        } else {
            Math.round((numButtons - 1) / 2f * (boxHeight + 5)) + 10
        }
        for (btnType in buttonTypes) {
            val btnMessage: String = btnType.message
            var boxWidth = mc.fontRendererObj.getStringWidth(btnMessage) + 10
            if (boxWidth > Companion.BUTTON_MAX_WIDTH) boxWidth = Companion.BUTTON_MAX_WIDTH
            x = scaledResolution.scaledWidth / 2 - boxWidth / 2
            y += boxHeight + 5
            buttonList.add(ButtonEditGui(x, y, boxWidth, boxHeight, btnType))
        }
    }

    private fun clearAllResizeButtons() {
        buttonList.removeIf { button: GuiButton? -> button is ButtonResize }
    }

    private fun clearAllColorWheelButtons() {
        buttonList.removeIf { button: GuiButton? -> button is ButtonColorWheel }
    }

    private fun addResizeButtonsToAllFeatures() {
        clearAllResizeButtons()
        // Add all gui elements that can be edited to the gui.
        for (feature in FeatureManager.guiFeatures) {
            if (!feature.disabled) { // Don't display features that have been disabled
                addResizeCorners(feature)
            }
        }
    }

    private fun addResizeButtonsToBars() {
        clearAllResizeButtons()
        // Add all gui elements that can be edited to the gui.
        for (feature in FeatureManager.guiFeatures) {
            if (!feature.disabled) { // Don't display features that have been disabled
                if (feature is BarFeature) {
                    addResizeCorners(feature)
                }
            }
        }
    }

    private fun addColorWheelsToAllFeatures() {
        for (buttonLocation in buttonLocations.values) {
            val feature: Feature = buttonLocation.feature
            if (feature !is ColoredFeature) {
                continue
            }
            val anchorPoint: Config.AnchorPoint = feature.anchorPoint
            val scaleX: Float =
                if (feature is BarFeature) feature.getSizesX() else 1f
            val scaleY: Float =
                if (feature is BarFeature) feature.getSizesY() else 1f
            val boxXOne: Float = buttonLocation.boxXOne * scaleX
            val boxXTwo: Float = buttonLocation.boxXTwo * scaleX
            val boxYOne: Float = buttonLocation.boxYOne * scaleY
            val boxYTwo: Float = buttonLocation.boxYTwo * scaleY
            val y: Float = boxYOne + (boxYTwo - boxYOne) / 2f - ButtonColorWheel.size / 2f
            val x: Float =
                if (anchorPoint === Config.AnchorPoint.TOP_LEFT || anchorPoint === Config.AnchorPoint.BOTTOM_LEFT) {
                    boxXTwo + 2
                } else {
                    boxXOne - ButtonColorWheel.size - 2
                }
            buttonList.add(ButtonColorWheel(Math.round(x).toFloat(), Math.round(y).toFloat(), feature))
        }
    }

    private fun addResizeCorners(feature: Feature) {
        buttonList.removeIf { button: GuiButton -> button is ButtonResize && (button as ButtonResize).feature === feature }
        val buttonLocation: ButtonLocation = buttonLocations[feature] ?: return
        val boxXOne: Float = buttonLocation.boxXOne
        val boxXTwo: Float = buttonLocation.boxXTwo
        val boxYOne: Float = buttonLocation.boxYOne
        val boxYTwo: Float = buttonLocation.boxYTwo
        val scaleX: Float = if (feature is BarFeature) feature.getSizesX() else 1f
        val scaleY: Float = if (feature is BarFeature) feature.getSizesY() else 1f
        buttonList.add(ButtonResize(boxXOne * scaleX, boxYOne * scaleY, feature, ButtonResize.Corner.TOP_LEFT))
        buttonList.add(ButtonResize(boxXTwo * scaleX, boxYOne * scaleY, feature, ButtonResize.Corner.TOP_RIGHT))
        buttonList.add(ButtonResize(boxXOne * scaleX, boxYTwo * scaleY, feature, ButtonResize.Corner.BOTTOM_LEFT))
        buttonList.add(ButtonResize(boxXTwo * scaleX, boxYTwo * scaleY, feature, ButtonResize.Corner.BOTTOM_RIGHT))
    }

    /**
     * Returns the `ButtonLocation` the mouse is currently hovering over. Returns `null` if the mouse is not
     * hovering over a `ButtonLocation`.
     *
     * @param mouseX the x-coordinate of the mouse
     * @param mouseY the y-coordinate of the mouse
     * @return the `ButtonLocation` the mouse is currently hovering over or `null` if the mouse is not hovering
     * over any
     */
    private fun getHoveredFeatureButton(mouseX: Int, mouseY: Int): ButtonLocation? {
        for (button in buttonList) {
            if (button is ButtonLocation) {
                val buttonLocation: ButtonLocation = button as ButtonLocation
                if (mouseX >= buttonLocation.boxXOne && mouseX <= buttonLocation.boxXTwo && mouseY >= buttonLocation.boxYOne && mouseY <= buttonLocation.boxYOne) {
                    return buttonLocation
                }
            }
        }
        return null
    }

    private fun recalculateResizeButtons() {
        for (button in buttonList) {
            if (button is ButtonResize) {
                val buttonResize: ButtonResize = button as ButtonResize
                val corner: ButtonResize.Corner = buttonResize.corner
                val feature: Feature = buttonResize.feature
                val buttonLocation: ButtonLocation = buttonLocations[feature] ?: continue
                val scaleX: Float =
                    if (feature is BarFeature) feature.getSizesX() else 1f
                val scaleY: Float =
                    if (feature is BarFeature) feature.getSizesY() else 1f
                val boxXOne: Float = buttonLocation.boxXOne * scaleX
                val boxXTwo: Float = buttonLocation.boxXTwo * scaleX
                val boxYOne: Float = buttonLocation.boxYOne * scaleY
                val boxYTwo: Float = buttonLocation.boxYTwo * scaleY
                if (corner === ButtonResize.Corner.TOP_LEFT) {
                    buttonResize.x = boxXOne
                    buttonResize.y = boxYOne
                } else if (corner === ButtonResize.Corner.TOP_RIGHT) {
                    buttonResize.x = boxXTwo
                    buttonResize.y = boxYOne
                } else if (corner === ButtonResize.Corner.BOTTOM_LEFT) {
                    buttonResize.x = boxXOne
                    buttonResize.y = boxYTwo
                } else if (corner === ButtonResize.Corner.BOTTOM_RIGHT) {
                    buttonResize.x = boxXTwo
                    buttonResize.y = boxYTwo
                }
            }
        }
    }

    private fun recalculateColorWheels() {
        for (button in buttonList) {
            if (button is ButtonColorWheel) {
                val buttonColorWheel: ButtonColorWheel = button as ButtonColorWheel
                val feature: Feature = buttonColorWheel.feature
                val buttonLocation: ButtonLocation = buttonLocations[feature] ?: continue
                val anchorPoint: Config.AnchorPoint = feature.anchorPoint
                val scaleX: Float =
                    if (feature is BarFeature) feature.getSizesX() else 1f
                val scaleY: Float =
                    if (feature is BarFeature) feature.getSizesY() else 1f
                val boxXOne: Float = buttonLocation.boxXOne * scaleX
                val boxXTwo: Float = buttonLocation.boxXTwo * scaleX
                val boxYOne: Float = buttonLocation.boxYOne * scaleY
                val boxYTwo: Float = buttonLocation.boxYTwo * scaleY
                val y: Float = boxYOne + (boxYTwo - boxYOne) / 2f - ButtonColorWheel.size / 2f
                val x: Float =
                    if (anchorPoint === Config.AnchorPoint.TOP_LEFT || anchorPoint === Config.AnchorPoint.BOTTOM_LEFT) {
                        boxXTwo + 2
                    } else {
                        boxXOne - ButtonColorWheel.size - 2
                    }
                buttonColorWheel.x = x
                buttonColorWheel.y = y
            }
        }
    }

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        val snaps = checkSnapping()
        onMouseMove(mouseX, mouseY, snaps)
        if (editMode == EditMode.RESCALE) {
            recalculateResizeButtons()
        }
        recalculateColorWheels()
        val startColor = Color(0, 0, 0, 64).rgb
        val endColor = Color(0, 0, 0, 128).rgb
        drawGradientRect(0, 0, width, height, startColor, endColor)
        for (anchorPoint in Config.AnchorPoint.values()) {
            val sr = ScaledResolution(Minecraft.getMinecraft())
            val x: Int = anchorPoint.getX(sr.scaledWidth)
            val y: Int = anchorPoint.getY(sr.scaledHeight)
            var color: Int = ColorCode.RED.getColor(127)
            val lastHovered: Feature? = ButtonLocation.lastHoveredFeature
            if (lastHovered != null && lastHovered.anchorPoint === anchorPoint) {
                color = ColorCode.YELLOW.getColor(127)
            }
            DrawUtils.drawRectAbsolute((x - 4).toDouble(), (y - 4).toDouble(), (x + 4).toDouble(), (y + 4).toDouble(), color)
        }
        super.drawScreen(mouseX, mouseY, partialTicks) // Draw buttons.
        if (snaps != null) {
            for (snap in snaps) {
                if (snap != null) {
                    var left: Float = snap.rectangle[Edge.LEFT]!!
                    var top: Float = snap.rectangle[Edge.TOP]!!
                    var right: Float = snap.rectangle[Edge.RIGHT]!!
                    var bottom: Float = snap.rectangle[Edge.BOTTOM]!!
                    if (snap.getWidth() < 0.5) {
                        val averageX = (left + right) / 2
                        left = averageX - 0.25f
                        right = averageX + 0.25f
                    }
                    if (snap.getHeight() < 0.5) {
                        val averageY = (top + bottom) / 2
                        top = averageY - 0.25f
                        bottom = averageY + 0.25f
                    }
                    if ((right - left).toDouble() == 0.5 || (bottom - top).toDouble() == 0.5) {
                        DrawUtils.drawRectAbsolute(left.toDouble(), top.toDouble(), right.toDouble(), bottom.toDouble(), -0xff0100)
                    } else {
                        DrawUtils.drawRectAbsolute(left.toDouble(), top.toDouble(), right.toDouble(), bottom.toDouble(), -0x10000)
                    }
                }
            }
        }
        if (showFeatureNameOnHover) {
            val hoveredButton: ButtonLocation? = getHoveredFeatureButton(mouseX, mouseY)
            if (hoveredButton != null) {
                drawHoveringText(listOf(hoveredButton.feature.message), mouseX, mouseY)
            }
        }
    }

    fun checkSnapping(): Array<Snap?>? {
        if (!enableSnapping) return null
        if (draggedFeature != null) {
            val thisButton: ButtonLocation = buttonLocations[draggedFeature] ?: return null
            var horizontalSnap: Snap? = null
            var verticalSnap: Snap? = null
            for (otherButton in buttonLocations.values) {
                if (otherButton === thisButton) continue
                for (otherEdge in Edge.horizontalEdges) {
                    for (thisEdge in Edge.horizontalEdges) {
                        val deltaX = otherEdge.getCoordinate(otherButton) - thisEdge.getCoordinate(thisButton)
                        if (Math.abs(deltaX) <= SNAP_PULL) {
                            val deltaY = Edge.TOP.getCoordinate(otherButton) - Edge.TOP.getCoordinate(thisButton)
                            var topY: Float
                            var bottomY: Float
                            if (deltaY > 0) {
                                topY = Edge.BOTTOM.getCoordinate(thisButton)
                                bottomY = Edge.TOP.getCoordinate(otherButton)
                            } else {
                                topY = Edge.BOTTOM.getCoordinate(otherButton)
                                bottomY = Edge.TOP.getCoordinate(thisButton)
                            }
                            val snapX = otherEdge.getCoordinate(otherButton)
                            val thisSnap = Snap(
                                otherEdge.getCoordinate(otherButton),
                                topY,
                                thisEdge.getCoordinate(thisButton),
                                bottomY,
                                thisEdge,
                                otherEdge,
                                snapX
                            )
                            if (thisSnap.getHeight() < SNAPPING_RADIUS) {
                                if (horizontalSnap == null || thisSnap.getHeight() < horizontalSnap.getHeight()) {
//                                    if (main.getConfigValues().isEnabled(Feature.DEVELOPER_MODE)) {
//                                        DrawUtils.drawRectAbsolute(
//                                            snapX - 0.5,
//                                            0,
//                                            snapX + 0.5,
//                                            mc.displayHeight,
//                                            -0xffff01
//                                        )
//                                    }
                                    horizontalSnap = thisSnap
                                }
                            }
                        }
                    }
                }
                for (otherEdge in Edge.verticalEdges) {
                    for (thisEdge in Edge.verticalEdges) {
                        val deltaY = otherEdge.getCoordinate(otherButton) - thisEdge.getCoordinate(thisButton)
                        if (Math.abs(deltaY) <= SNAP_PULL) {
                            val deltaX = Edge.LEFT.getCoordinate(otherButton) - Edge.LEFT.getCoordinate(thisButton)
                            var leftX: Float
                            var rightX: Float
                            if (deltaX > 0) {
                                leftX = Edge.RIGHT.getCoordinate(thisButton)
                                rightX = Edge.LEFT.getCoordinate(otherButton)
                            } else {
                                leftX = Edge.RIGHT.getCoordinate(otherButton)
                                rightX = Edge.LEFT.getCoordinate(thisButton)
                            }
                            val snapY = otherEdge.getCoordinate(otherButton)
                            val thisSnap = Snap(
                                leftX,
                                otherEdge.getCoordinate(otherButton),
                                rightX,
                                thisEdge.getCoordinate(thisButton),
                                thisEdge,
                                otherEdge,
                                snapY
                            )
                            if (thisSnap.getWidth() < SNAPPING_RADIUS) {
                                if (verticalSnap == null || thisSnap.getWidth() < verticalSnap.getWidth()) {
//                                    if (main.getConfigValues().isEnabled(Feature.DEVELOPER_MODE)) {
//                                        DrawUtils.drawRectAbsolute(
//                                            0,
//                                            snapY - 0.5,
//                                            mc.displayWidth,
//                                            snapY + 0.5,
//                                            -0xffff01
//                                        )
//                                    }
                                    verticalSnap = thisSnap
                                }
                            }
                        }
                    }
                }
            }
            return arrayOf(horizontalSnap, verticalSnap)
        }
        return null
    }

    /**
     * Called during each frame a [ButtonLocation] in this GUI is being hovered over by the mouse.
     *
     * @param button the button being hovered over
     */
    fun onButtonHoverFrame(button: ButtonLocation) {
        if (showFeatureNameOnHover && (hoveredFeature == null || hoveredFeature !== button.feature)) {
            hoveredFeature = button.feature
        }
    }

    internal enum class Edge {
        LEFT, TOP, RIGHT, BOTTOM, HORIZONTAL_MIDDLE, VERTICAL_MIDDLE;

        fun getCoordinate(button: ButtonLocation): Float {
            return when (this) {
                LEFT -> button.boxXOne * button.scale
                TOP -> button.boxYOne * button.scale
                RIGHT -> button.boxXTwo * button.scale
                BOTTOM -> button.boxYTwo * button.scale
                HORIZONTAL_MIDDLE -> getCoordinate(button) + (getCoordinate(button) - getCoordinate(button)) / 2f
                VERTICAL_MIDDLE -> getCoordinate(button) + (getCoordinate(button) - getCoordinate(button)) / 2f
                else -> 0f
            }
        }

        companion object {
            val verticalEdges: Set<Edge> = Sets.newHashSet(TOP, BOTTOM, HORIZONTAL_MIDDLE)

            val horizontalEdges: Set<Edge> = Sets.newHashSet(LEFT, RIGHT, VERTICAL_MIDDLE)
        }
    }

    /**
     * Set the coordinates when the mouse moves.
     */
    protected fun onMouseMove(mouseX: Int, mouseY: Int, snaps: Array<Snap?>?) {
        val sr = ScaledResolution(mc)
        val minecraftScale = sr.scaleFactor.toFloat()
        val floatMouseX = Mouse.getX() / minecraftScale
        val floatMouseY = (mc.displayHeight - Mouse.getY()) / minecraftScale
        if (resizing) {
            val x = mouseX - xOffset
            val y = mouseY - yOffset
            if (editMode == EditMode.RESIZE_BARS) {
                val buttonLocation: ButtonLocation = buttonLocations[draggedFeature] ?: return
                val middleX: Float = (buttonLocation.boxXTwo + buttonLocation.boxXOne) / 2
                val middleY: Float = (buttonLocation.boxYTwo + buttonLocation.boxYOne) / 2
                var scaleX = (floatMouseX - middleX) / (xOffset - middleX)
                var scaleY = (floatMouseY - middleY) / (yOffset - middleY)
                scaleX = Math.max(Math.min(scaleX, 5f).toDouble(), .25).toFloat()
                scaleY = Math.max(Math.min(scaleY, 5f).toDouble(), .25).toFloat()
                //main.getConfigValues().setScaleX(draggedFeature, scaleX)
                //main.getConfigValues().setScaleY(draggedFeature, scaleY)
                // TODO: is this the right replacement?
                if (draggedFeature is BarFeature) {
                    (draggedFeature as BarFeature).barSize = Pair(scaleX, scaleY)
                }
                buttonLocation.drawButton(mc, mouseX, mouseY)
                recalculateResizeButtons()
            } else if (editMode == EditMode.RESCALE) {
                val buttonLocation: ButtonLocation = buttonLocations[draggedFeature] ?: return
                val scale = buttonLocation.scale
                val scaledX1 = buttonLocation.boxXOne * buttonLocation.scale
                val scaledY1 = buttonLocation.boxYOne * buttonLocation.scale
                val scaledX2 = buttonLocation.boxXTwo * buttonLocation.scale
                val scaledY2 = buttonLocation.boxYTwo * buttonLocation.scale
                val scaledWidth = scaledX2 - scaledX1
                val scaledHeight = scaledY2 - scaledY1
                val width: Float = buttonLocation.boxXTwo - buttonLocation.boxXOne
                val height: Float = buttonLocation.boxYTwo - buttonLocation.boxYOne
                val middleX = scaledX1 + scaledWidth / 2f
                val middleY = scaledY1 + scaledHeight / 2f
                var xOffset = floatMouseX - xOffset * scale - middleX
                var yOffset = floatMouseY - yOffset * scale - middleY
                if (resizingCorner === ButtonResize.Corner.TOP_LEFT) {
                    xOffset *= -1f
                    yOffset *= -1f
                } else if (resizingCorner === ButtonResize.Corner.TOP_RIGHT) {
                    yOffset *= -1f
                } else if (resizingCorner === ButtonResize.Corner.BOTTOM_LEFT) {
                    xOffset *= -1f
                }
                val newWidth = xOffset * 2f
                val newHeight = yOffset * 2f
                val scaleX = newWidth / width
                val scaleY = newHeight / height
                val newScale = Math.max(scaleX, scaleY)
                val normalizedScale: Float = Utils.normalizeValueNoStep(newScale)
                draggedFeature!!.guiScale = normalizedScale
                buttonLocation.drawButton(mc, mouseX, mouseY)
                recalculateResizeButtons()
            }
        } else if (draggedFeature != null) {
            val buttonLocation: ButtonLocation = buttonLocations[draggedFeature] ?: return
            var horizontalSnap: Snap? = null
            var verticalSnap: Snap? = null
            if (snaps != null) {
                horizontalSnap = snaps[0]
                verticalSnap = snaps[1]
            }
            var x: Float = floatMouseX - draggedFeature!!.anchorPoint.getX(sr.scaledWidth)
            var y: Float = floatMouseY - draggedFeature!!.anchorPoint.getY(sr.scaledHeight)
            val scaledX1: Float = buttonLocation.boxXOne * buttonLocation.scale
            val scaledY1: Float = buttonLocation.boxYOne * buttonLocation.scale
            val scaledX2: Float = buttonLocation.boxXTwo * buttonLocation.scale
            val scaledY2: Float = buttonLocation.boxYTwo * buttonLocation.scale
            val scaledWidth = scaledX2 - scaledX1
            val scaledHeight = scaledY2 - scaledY1
            var xSnapped = false
            var ySnapped = false
            if (horizontalSnap != null) {
                val snapX: Float = horizontalSnap.snapValue
                if (horizontalSnap.thisSnapEdge === Edge.LEFT) {
                    val snapOffset = Math.abs(floatMouseX - xOffset - (snapX + scaledWidth / 2f))
                    if (snapOffset <= SNAP_PULL * minecraftScale) {
                        xSnapped = true
                        x = snapX - draggedFeature!!.anchorPoint
                            .getX(sr.scaledWidth) + scaledWidth / 2f
                    }
                } else if (horizontalSnap.thisSnapEdge === Edge.RIGHT) {
                    val snapOffset = Math.abs(floatMouseX - xOffset - (snapX - scaledWidth / 2f))
                    if (snapOffset <= SNAP_PULL * minecraftScale) {
                        xSnapped = true
                        x = snapX - draggedFeature!!.anchorPoint
                            .getX(sr.scaledWidth) - scaledWidth / 2f
                    }
                } else if (horizontalSnap.thisSnapEdge === Edge.VERTICAL_MIDDLE) {
                    val snapOffset = Math.abs(floatMouseX - xOffset - snapX)
                    if (snapOffset <= SNAP_PULL * minecraftScale) {
                        xSnapped = true
                        x = snapX - draggedFeature!!.anchorPoint.getX(sr.scaledWidth)
                    }
                }
            }
            if (verticalSnap != null) {
                val snapY: Float = verticalSnap.snapValue
                if (verticalSnap.thisSnapEdge === Edge.TOP) {
                    val snapOffset = Math.abs(floatMouseY - yOffset - (snapY + scaledHeight / 2f))
                    if (snapOffset <= SNAP_PULL * minecraftScale) {
                        ySnapped = true
                        y = snapY - draggedFeature!!.anchorPoint.getY(sr.scaledHeight) + scaledHeight / 2f
                    }
                } else if (verticalSnap.thisSnapEdge === Edge.BOTTOM) {
                    val snapOffset = Math.abs(floatMouseY - yOffset - (snapY - scaledHeight / 2f))
                    if (snapOffset <= SNAP_PULL * minecraftScale) {
                        ySnapped = true
                        y = snapY - draggedFeature!!.anchorPoint
                            .getY(sr.scaledHeight) - scaledHeight / 2f
                    }
                } else if (verticalSnap.thisSnapEdge === Edge.HORIZONTAL_MIDDLE) {
                    val snapOffset = Math.abs(floatMouseY - yOffset - snapY)
                    if (snapOffset <= SNAP_PULL * minecraftScale) {
                        ySnapped = true
                        y = snapY - draggedFeature!!.anchorPoint.getY(sr.scaledHeight)
                    }
                }
            }
            if (!xSnapped) {
                x -= xOffset
            }
            if (!ySnapped) {
                y -= yOffset
            }
            if (xSnapped || ySnapped) {
                val xChange: Float = Math.abs(draggedFeature!!.coordinates.first - x)
                val yChange: Float = Math.abs(draggedFeature!!.coordinates.second - y)
                if (xChange < 0.001 && yChange < 0.001) {
                    return
                }
            }
            draggedFeature!!.coordinates = Pair(x, y)
            Utils.setClosestAnchorPoint(draggedFeature!!)
//            if (draggedFeature === Feature.HEALTH_BAR || draggedFeature === Feature.MANA_BAR || draggedFeature === Feature.DRILL_FUEL_BAR) {
//                addResizeCorners(draggedFeature)
//            }
        }
    }

    /**
     * If button is pressed, update the currently dragged button.
     * Otherwise, they clicked the reset button, so reset the coordinates.
     */
    override fun actionPerformed(abstractButton: GuiButton) {
        if (abstractButton is ButtonLocation) {
            val buttonLocation: ButtonLocation = abstractButton as ButtonLocation
            draggedFeature = buttonLocation.feature
            val sr = ScaledResolution(mc)
            val minecraftScale = sr.scaleFactor.toFloat()
            val floatMouseX = Mouse.getX() / minecraftScale
            val floatMouseY = (mc.displayHeight - Mouse.getY()) / minecraftScale
            xOffset = floatMouseX - buttonLocation.feature.getActualX()
            yOffset = floatMouseY - buttonLocation.feature.getActualY()
        } else if (abstractButton is ButtonEditGui) {
            val buttonSolid: ButtonEditGui = abstractButton as ButtonEditGui
            val feature = buttonSolid.type
            if (feature === ButtonEditGui.Companion.Type.RESET_LOCATION) {
                FeatureManager.guiFeatures.forEach { it.resetPosition() }
//                for (guiFeature in FeatureManager.guiFeatures) {
//                    if (!main.getConfigValues()
//                            .isDisabled(guiFeature)
//                    ) { // Don't display features that have been disabled
//                        if (guiFeature === Feature.HEALTH_BAR || guiFeature === Feature.MANA_BAR || guiFeature === Feature.DRILL_FUEL_BAR) {
//                            addResizeCorners(guiFeature)
//                        }
//                    }
//                }
            } else if (feature === ButtonEditGui.Companion.Type.RESIZE_BARS) {
                if (editMode != EditMode.RESIZE_BARS) {
                    editMode = EditMode.RESIZE_BARS
                    addResizeButtonsToBars()
                } else {
                    editMode = null
                    clearAllResizeButtons()
                }
            } else if (feature === ButtonEditGui.Companion.Type.RESCALE_FEATURES) {
                if (editMode != EditMode.RESCALE) {
                    editMode = EditMode.RESCALE
                    addResizeButtonsToAllFeatures()
                } else {
                    editMode = null
                    clearAllResizeButtons()
                }
            } else if (feature === ButtonEditGui.Companion.Type.SHOW_COLOR_ICONS) {
                if (showColorIcons) {
                    showColorIcons = false
                    clearAllColorWheelButtons()
                } else {
                    showColorIcons = true
                    addColorWheelsToAllFeatures()
                }
            } else if (feature === ButtonEditGui.Companion.Type.ENABLE_FEATURE_SNAPPING) {
                enableSnapping = !enableSnapping
            } else if (feature === ButtonEditGui.Companion.Type.SHOW_FEATURE_NAMES_ON_HOVER) {
                showFeatureNameOnHover = !showFeatureNameOnHover
            }
        } else if (abstractButton is ButtonResize) {
            val buttonResize: ButtonResize = abstractButton as ButtonResize
            draggedFeature = buttonResize.feature
            resizing = true
            val sr = ScaledResolution(mc)
            val minecraftScale = sr.scaleFactor.toFloat()
            val floatMouseX = Mouse.getX() / minecraftScale
            val floatMouseY = (mc.displayHeight - Mouse.getY()) / minecraftScale
            val scale: Float = buttonResize.feature.guiScale
            if (editMode == EditMode.RESCALE) {
                xOffset = (floatMouseX - buttonResize.x * scale) / scale
                yOffset = (floatMouseY - buttonResize.y * scale) / scale
            } else {
                xOffset = floatMouseX
                yOffset = floatMouseY
            }
            resizingCorner = buttonResize.corner
        } else if (abstractButton is ButtonColorWheel) {
            val buttonColorWheel: ButtonColorWheel = abstractButton as ButtonColorWheel
            closing = true
            mc.displayGuiScreen(
                ColorSelectionGui(
                    buttonColorWheel.feature as ColoredFeature, this
                )
            )
        }
    }

    internal class Snap(
        left: Float,
        top: Float,
        right: Float,
        bottom: Float,
        thisSnapEdge: Edge,
        otherSnapEdge: Edge,
        snapValue: Float
    ) {
        val thisSnapEdge: Edge
        val otherSnapEdge: Edge
        val snapValue: Float
        val rectangle: MutableMap<Edge, Float> = EnumMap(
            Edge::class.java
        )

        init {
            rectangle[Edge.LEFT] = left
            rectangle[Edge.TOP] = top
            rectangle[Edge.RIGHT] = right
            rectangle[Edge.BOTTOM] = bottom
            rectangle[Edge.HORIZONTAL_MIDDLE] = top + getHeight() / 2
            rectangle[Edge.VERTICAL_MIDDLE] = left + getWidth() / 2
            this.otherSnapEdge = otherSnapEdge
            this.thisSnapEdge = thisSnapEdge
            this.snapValue = snapValue
        }

        fun getHeight(): Float {
            return rectangle[Edge.BOTTOM]!! - rectangle[Edge.TOP]!!
        }

        fun getWidth(): Float {
            return rectangle[Edge.RIGHT]!! - rectangle[Edge.LEFT]!!
        }
    }

    /**
     * Allow moving the last hovered feature with arrow keys.
     */
    @Throws(IOException::class)
    override fun keyTyped(typedChar: Char, keyCode: Int) {
        super.keyTyped(typedChar, keyCode)
        val hoveredFeature: Feature? = ButtonLocation.lastHoveredFeature
        if (hoveredFeature != null) {
            var xOffset = 0
            var yOffset = 0
            if (keyCode == Keyboard.KEY_LEFT) {
                xOffset--
            } else if (keyCode == Keyboard.KEY_UP) {
                yOffset--
            } else if (keyCode == Keyboard.KEY_RIGHT) {
                xOffset++
            } else if (keyCode == Keyboard.KEY_DOWN) {
                yOffset++
            }
            if (keyCode == Keyboard.KEY_A) {
                xOffset -= 10
            } else if (keyCode == Keyboard.KEY_W) {
                yOffset -= 10
            } else if (keyCode == Keyboard.KEY_D) {
                xOffset += 10
            } else if (keyCode == Keyboard.KEY_S) {
                yOffset += 10
            }
            hoveredFeature.coordinates = Pair(
                hoveredFeature!!.coordinates.first + xOffset,
                hoveredFeature!!.coordinates.second + yOffset
            )
        }
    }

    /**
     * Reset the dragged feature when the mouse is released.
     */
    override fun mouseReleased(mouseX: Int, mouseY: Int, state: Int) {
        super.mouseReleased(mouseX, mouseY, state)
        draggedFeature = null
        resizing = false
    }

    /**
     * Open up the last GUI (main), and save the config.
     */
    override fun onGuiClosed() {
        println("todo: save config")
//        main.getConfigValues().saveConfig()
//        if (lastTab != null && !closing) {
//            main.getRenderListener().setGuiToOpen(GUIType.MAIN, lastPage, lastTab)
//        }
    }

    private enum class EditMode {
        RESCALE, RESIZE_BARS
    }

    companion object {
        private const val SNAPPING_RADIUS = 120
        private const val SNAP_PULL = 1
        const val BUTTON_MAX_WIDTH = 140
    }
}
