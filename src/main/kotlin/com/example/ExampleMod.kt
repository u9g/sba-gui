package com.example

import com.example.core.FeatureManager
import com.example.fonts.FontRendererHook
import com.example.gui.LocationEditGui
import com.example.misc.scheduler.NewScheduler
import com.google.common.util.concurrent.ThreadFactoryBuilder
import net.minecraft.command.CommandBase
import net.minecraft.command.ICommand
import net.minecraft.command.ICommandSender
import net.minecraft.init.Blocks
import net.minecraftforge.client.ClientCommandHandler
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.common.event.FMLInitializationEvent
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

const val MOD_NAME = "examplemod"

private val THREAD_EXECUTOR = ThreadPoolExecutor(
    0,
    1,
    60L,
    TimeUnit.SECONDS,
    LinkedBlockingQueue(),
    ThreadFactoryBuilder().setNameFormat("$MOD_NAME - #%d").build()
)

fun runAsync(runnable: Runnable) {
    THREAD_EXECUTOR.execute(runnable)
}

@Mod(modid = MOD_NAME, version = "1.0.0")
class ExampleMod {
    val renderListener = RenderListener()
    var fullyInitialized = false
    @Mod.EventHandler
    fun init(event: FMLInitializationEvent) {
        println("Dirt: ${Blocks.dirt.unlocalizedName}")

        FeatureManager.addFeature(HealthText())
        MinecraftForge.EVENT_BUS.register(NewScheduler.INSTANCE)
        MinecraftForge.EVENT_BUS.register(renderListener)
        ClientCommandHandler.instance.registerCommand(object : CommandBase() {
            override fun getCommandName() = "gui"

            override fun getCommandUsage(sender: ICommandSender?) = "/gui"

            override fun processCommand(sender: ICommandSender?, args: Array<out String>?) {
                RenderListener.screenToOpen = LocationEditGui(null)
            }

            override fun canCommandSenderUseCommand(sender: ICommandSender?) = true
        })
    }

    fun postInit(event: FMLPostInitializationEvent) {
        fullyInitialized = true
        FontRendererHook.onModInitialized()
    }
}