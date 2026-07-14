package dev.prismriver.legacyskyboxes.sky.component

import org.lwjgl.opengl.GL11

enum class Blend(
    val srcFactor: Int,
    val dstFactor: Int,
    private val colorMode: ColorMode,
) {
    ADD(GL11.GL_SRC_ALPHA, GL11.GL_ONE, ColorMode.WHITE_RGB_ALPHA_A),
    SUBTRACT(GL11.GL_ONE_MINUS_DST_COLOR, GL11.GL_ZERO, ColorMode.ALPHA_RGB_ONE_A),
    MULTIPLY(GL11.GL_DST_COLOR, GL11.GL_ONE_MINUS_SRC_ALPHA, ColorMode.ALPHA_RGBA),
    DODGE(GL11.GL_ONE, GL11.GL_ONE, ColorMode.ALPHA_RGB_ONE_A),
    BURN(GL11.GL_ZERO, GL11.GL_ONE_MINUS_SRC_COLOR, ColorMode.ALPHA_RGB_ONE_A),
    SCREEN(GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_COLOR, ColorMode.ALPHA_RGB_ONE_A),
    REPLACE(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, ColorMode.WHITE_RGB_ALPHA_A),
    OVERLAY(GL11.GL_DST_COLOR, GL11.GL_SRC_COLOR, ColorMode.ALPHA_RGB_ONE_A),
    ALPHA(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, ColorMode.WHITE_RGB_ALPHA_A);

    fun writeColor(alpha: Float, out: FloatArray) = colorMode.write(alpha, out)

    enum class ColorMode {
        WHITE_RGB_ALPHA_A {
            override fun write(alpha: Float, out: FloatArray) {
                out[0] = 1.0f; out[1] = 1.0f; out[2] = 1.0f; out[3] = alpha
            }
        },

        ALPHA_RGB_ONE_A {
            override fun write(alpha: Float, out: FloatArray) {
                out[0] = alpha; out[1] = alpha; out[2] = alpha; out[3] = 1.0f
            }
        },

        ALPHA_RGBA {
            override fun write(alpha: Float, out: FloatArray) {
                out[0] = alpha; out[1] = alpha; out[2] = alpha; out[3] = alpha
            }
        };

        abstract fun write(alpha: Float, out: FloatArray)
    }

    companion object {
        val DEFAULT = ADD

        fun fromName(name: String?): Blend {
            if (name != null) {
                val trimmed = name.trim()
                for (blend in entries) {
                    if (blend.name.equals(trimmed, ignoreCase = true)) {
                        return blend
                    }
                }
            }
            return DEFAULT
        }
    }
}
