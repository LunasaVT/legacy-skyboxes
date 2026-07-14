package dev.prismriver.legacyskyboxes.sky.render

import dev.prismriver.legacyskyboxes.sky.component.Blend
import net.minecraft.client.render.platform.GlStateManager

object CelestialRenderer {
    private val scratch = FloatArray(4)

    fun applyOverride(blend: Blend, brightness: Float) {
        GlStateManager.blendFunc(blend.srcFactor, blend.dstFactor)
        blend.writeColor(brightness, scratch)
        GlStateManager.color4f(scratch[0], scratch[1], scratch[2], scratch[3])
    }
}
