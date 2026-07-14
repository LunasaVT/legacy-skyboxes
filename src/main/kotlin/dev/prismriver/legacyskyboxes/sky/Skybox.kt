package dev.prismriver.legacyskyboxes.sky

import net.minecraft.entity.Entity
import net.minecraft.world.World

class Skybox(
    val dimensionId: Int,
    val layers: List<SkyboxLayer>,
    val sun: CelestialOverride? = null,
    val moonPhases: CelestialOverride? = null,
) {
    fun updateConditions(world: World, camera: Entity?, elapsedTicks: Int) {
        for (layer in layers) {
            layer.updateCondition(world, camera, elapsedTicks)
        }
    }

    fun resetConditions() {
        for (layer in layers) {
            layer.resetCondition()
        }
    }
}
