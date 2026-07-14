package dev.prismriver.legacyskyboxes.sky

import dev.prismriver.legacyskyboxes.sky.component.Blend
import net.minecraft.resource.Identifier

class CelestialOverride(
    val texture: Identifier,
    val blend: Blend,
) {
    companion object {
        val SUN_TEXTURE: Identifier = Identifier("textures/environment/sun.png")
        val MOON_PHASES_TEXTURE: Identifier = Identifier("textures/environment/moon_phases.png")
    }
}
