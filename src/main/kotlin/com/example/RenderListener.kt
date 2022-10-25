package com.example

import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiScreen
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent

class RenderListener {
    @SubscribeEvent
    fun onRender(e: TickEvent.RenderTickEvent) {
        screenToOpen?.let {
            Minecraft.getMinecraft().displayGuiScreen(it)
            screenToOpen = null
        }
    }

    companion object {
        var screenToOpen: GuiScreen? = null
    }
}