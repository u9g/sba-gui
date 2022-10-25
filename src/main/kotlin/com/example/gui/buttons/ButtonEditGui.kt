package com.example.gui.buttons

import net.minecraft.client.gui.GuiButton

class ButtonEditGui(x: Int, y: Int, width: Int, height: Int, val type: Type) : GuiButton(99, x, y, width, height, type.message) {
    companion object {
        enum class Type(val message: String) {
            RESET_LOCATION("Reset Locations"),
            RESCALE_FEATURES("Rescale Features"),
            RESIZE_BARS("Resize Bars"),
            SHOW_COLOR_ICONS("Show Color Icons"),
            ENABLE_FEATURE_SNAPPING("Enable Feature Snapping"),
            SHOW_FEATURE_NAMES_ON_HOVER("Show Feature Name on Hover")
        }
    }
}