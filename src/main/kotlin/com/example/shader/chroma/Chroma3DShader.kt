package com.example.shader.chroma

import com.example.config.Config
import com.example.shader.UniformType
import com.example.utils.Utils
import javax.vecmath.Vector3d

/**
 * This shader shows a chroma color on a pixel depending on its position in the world
 *
 * This shader does:
 * - Take in account its position in 3-dimensional space
 *
 * This shader does not:
 * - Preserve the brightness and saturation of the original color
 * - Work with textures
 */
class Chroma3DShader : ChromaShader("chroma_3d") {
    var alpha = 1f
    override fun registerUniforms() {
        super.registerUniforms()
        registerUniform(UniformType.VEC3, "playerWorldPosition") {
            val viewPosition: Vector3d = Utils.getPlayerViewPosition()
            arrayOf(viewPosition.x.toFloat(), viewPosition.y.toFloat(), viewPosition.z.toFloat())
        }
        registerUniform(UniformType.FLOAT, "alpha") { alpha }
        registerUniform(UniformType.FLOAT, "brightness") { Config.Chroma.brightness }
    }
}