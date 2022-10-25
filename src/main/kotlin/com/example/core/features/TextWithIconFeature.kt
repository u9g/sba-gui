package com.example.core.features

import com.example.utils.ColorCode
import net.minecraft.item.ItemStack

abstract class TextWithIconFeature(message: String, color: Int) : TextFeature(message, color) {
    abstract fun getItem(): ItemStack
}