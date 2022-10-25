package com.example.shader

import com.example.utils.Utils
import net.minecraft.client.Minecraft
import net.minecraft.util.ResourceLocation
import org.apache.commons.lang3.StringUtils
import org.lwjgl.BufferUtils
import org.lwjgl.opengl.OpenGLException
import java.io.BufferedInputStream

class ShaderLoader private constructor(
    private val shaderType: ShaderType,
    private val shader: Int,
    private val fileName: String
) {
    private var shaderAttachCount = 0

    fun attachShader(shader: Shader) {
        ++shaderAttachCount
        ShaderHelper.glAttachShader(shader.program, this.shader)
    }

    fun deleteShader() {
        --shaderAttachCount
        if (shaderAttachCount <= 0) {
            ShaderHelper.glDeleteShader(shader)
            savedShaderLoaders.remove(fileName)
        }
    }


    enum class ShaderType(val shaderExtension: String, val glShaderType: Int) {
        VERTEX(".vsh", ShaderHelper.GL_VERTEX_SHADER),
        FRAGMENT(".fsh", ShaderHelper.GL_FRAGMENT_SHADER);
    }

    companion object {
        private val savedShaderLoaders: MutableMap<String, ShaderLoader> = HashMap()

        @Throws(Exception::class)
        fun load(type: ShaderType, fileName: String): ShaderLoader {
            var shaderLoader = savedShaderLoaders.get(fileName)
            if (shaderLoader == null) {
                println("trying to load shader: skyblockaddons:shaders/program/$fileName${type.shaderExtension}")
                val resourceLocation =
                    ResourceLocation("skyblockaddons", "shaders/program/" + fileName + type.shaderExtension)
                val bufferedInputStream =
                    BufferedInputStream(Minecraft.getMinecraft().resourceManager.getResource(resourceLocation).inputStream)
                val bytes: ByteArray = Utils.toByteArray(bufferedInputStream)
                val buffer = BufferUtils.createByteBuffer(bytes.size)
                buffer.put(bytes)
                buffer.position(0)
                val shaderID = ShaderHelper.glCreateShader(type.glShaderType)
                ShaderHelper.glShaderSource(shaderID, buffer)
                ShaderHelper.glCompileShader(shaderID)
                if (ShaderHelper.glGetShaderi(shaderID, ShaderHelper.GL_COMPILE_STATUS) == 0) {
                    throw OpenGLException(
                        "An error occurred while compiling shader " + fileName + ": " +
                                StringUtils.trim(ShaderHelper.glGetShaderInfoLog(shaderID, 32768))
                    )
                }
                shaderLoader = ShaderLoader(type, shaderID, fileName)
                savedShaderLoaders[fileName] = shaderLoader
            }
            return shaderLoader
        }
    }
}