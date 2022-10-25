package com.example.core.chroma

import com.example.config.Config
import com.example.shader.ShaderManager
import com.example.shader.chroma.Chroma3DShader
import com.example.shader.chroma.ChromaScreenShader
import com.example.shader.chroma.ChromaScreenTexturedShader
import com.example.shader.chroma.ChromaShader

/**
 * Handles all multicolor shaders in shader mode, as well as
 */
enum class MulticolorShaderManager {
    INSTANCE;

    /** Current chroma rendering state  */
    private var currentState: MulticolorState

    init {
        currentState = MulticolorState()
    }

    private open class MulticolorState {
        var chromaEnabled = false
        var textured = false
        var ignoreTexture = false
        var render3D = false

        constructor() {
            chromaEnabled = false
        }

        constructor(isTextured: Boolean, shouldIgnoreTexture: Boolean, shouldRender3D: Boolean) {
            textured = isTextured
            ignoreTexture = shouldIgnoreTexture
            render3D = shouldRender3D
        }

        open fun setup() {}
        open fun disable() {}
    }

    private class ShaderChromaState(isTextured: Boolean, shouldIgnoreTexture: Boolean, shouldRender3D: Boolean) :
        MulticolorState(isTextured, shouldIgnoreTexture, shouldRender3D) {
        var shaderType: Class<out ChromaShader?>? = null

        init {
            shaderType = if (isTextured) {
                if (shouldRender3D) {
                    // TODO: Actually make a shader that doesn't ignore texture and is 3D
                    ChromaScreenTexturedShader::class.java
                } else {
                    ChromaScreenTexturedShader::class.java
                }
            } else {
                if (shouldRender3D) {
                    Chroma3DShader::class.java
                } else {
                    ChromaScreenShader::class.java
                }
            }
        }

        override fun setup() {
            if (!chromaEnabled) {
                chromaEnabled = true
                if (shaderType == null) throw AssertionError()
                ShaderManager.INSTANCE.enableShader(shaderType!!)
            }
        }

        override fun disable() {
            if (chromaEnabled) {
                chromaEnabled = false
                ShaderManager.INSTANCE.disableShader()
            }
        }
    }

    /**
     *
     * @param ignoreTexture
     * @param is3D
     */
    fun begin(isTextured: Boolean, ignoreTexture: Boolean, is3D: Boolean) {
        // Using shader chroma
        currentState.disable()
        currentState = ShaderChromaState(isTextured, ignoreTexture, is3D)
        currentState.setup()
    }

    fun end() {
        currentState.disable()
    }

    fun shouldUseChromaShaders(): Boolean {
        return ShaderManager.INSTANCE.areShadersSupported() && Config.isNewChromaEnabled
    }

    companion object {
        val instance: MulticolorShaderManager
            get() = INSTANCE
    }
}