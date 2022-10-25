package com.example.gui.buttons

import com.example.core.Feature
import net.minecraft.client.gui.GuiButton

/**
 * Create a button that is assigned a feature (to toggle/change color etc.).
 */
open class ButtonFeature internal constructor(buttonId: Int, x: Int, y: Int, buttonText: String?, var feature: Feature) // The feature that this button moves
    : GuiButton(buttonId, x, y, buttonText)