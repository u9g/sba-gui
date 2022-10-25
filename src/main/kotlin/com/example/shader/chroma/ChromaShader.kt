package com.example.shader.chroma

import com.example.config.Config
import com.example.misc.scheduler.NewScheduler
import com.example.shader.Shader
import com.example.shader.UniformType
import com.example.utils.Utils
import net.minecraft.client.Minecraft

abstract class ChromaShader(shaderName: String) : Shader(shaderName, shaderName) {
    override fun registerUniforms() {
        // Chroma size is made proportionate to the size of the screen (ex. in a 1920px width screen, 100 = 1920)
        registerUniform(UniformType.FLOAT, "chromaSize") {
            Config.Chroma.size * (Minecraft.getMinecraft().displayWidth / 100f)
        }
        registerUniform(UniformType.FLOAT, "timeOffset") {
            val ticks: Float = NewScheduler.INSTANCE.totalTicks as Float + Utils.getPartialTicks()
            val chromaSpeed: Float = Config.Chroma.speed / 360f
            ticks * chromaSpeed
        }
        registerUniform(UniformType.FLOAT, "saturation") { Config.Chroma.saturation }
    }
}