package com.example

import com.example.config.Config
import com.example.core.features.TextFeature
import com.example.utils.ColorCode
import net.minecraft.client.Minecraft
import net.minecraft.entity.player.EntityPlayer
import java.text.NumberFormat
import java.util.*

private val NUMBER_FORMAT = NumberFormat.getInstance(Locale.US)

class HealthText : TextFeature("Health Text", ColorCode.RED.color, defaultAnchorPoint = Config.AnchorPoint.TOP_LEFT) {
    override fun textToDraw(): String {
        val thePlayer = Minecraft.getMinecraft().thePlayer
        return NUMBER_FORMAT.format(thePlayer.health) + "/" + NUMBER_FORMAT.format(thePlayer.maxHealth);
    }
}