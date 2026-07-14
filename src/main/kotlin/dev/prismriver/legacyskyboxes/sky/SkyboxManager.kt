package dev.prismriver.legacyskyboxes.sky

import net.minecraft.client.Minecraft
import net.minecraft.world.World

class SkyboxManager {
    private val skyboxes = ArrayList<Skybox>()
    private var lastWorldTime = Long.MIN_VALUE

    fun replaceAll(loaded: List<Skybox>) {
        skyboxes.clear()
        skyboxes.addAll(loaded)
        lastWorldTime = Long.MIN_VALUE
    }

    fun update(world: World, allowOverworldFallback: Boolean) {
        val elapsed = computeElapsedTicks(world.time)
        val camera = Minecraft.getInstance().camera
        val worldDimId = world.dimension.getId()
        for (skybox in skyboxes) {
            if (isApplicable(skybox, worldDimId, allowOverworldFallback)) {
                skybox.updateConditions(world, camera, elapsed)
            } else {
                skybox.resetConditions()
            }
        }
    }

    fun getActive(worldDimId: Int, allowOverworldFallback: Boolean): List<Skybox> {
        if (skyboxes.isEmpty()) return emptyList()
        return skyboxes.filter { isApplicable(it, worldDimId, allowOverworldFallback) }
    }

    private fun isApplicable(skybox: Skybox, worldDimId: Int, allowOverworldFallback: Boolean): Boolean {
        if (skybox.dimensionId == worldDimId) return true
        return allowOverworldFallback &&
            skybox.dimensionId == OVERWORLD &&
            worldDimId != NETHER &&
            worldDimId != END
    }

    private fun computeElapsedTicks(worldTime: Long): Int {
        if (lastWorldTime == Long.MIN_VALUE) {
            lastWorldTime = worldTime
            return 1
        }
        val delta = worldTime - lastWorldTime
        lastWorldTime = worldTime
        return when {
            delta <= 0L -> 0
            delta > MAX_CATCHUP_TICKS -> MAX_CATCHUP_TICKS
            else -> delta.toInt()
        }
    }

    companion object {
        private const val OVERWORLD = 0
        private const val NETHER = -1
        private const val END = 1
        private const val MAX_CATCHUP_TICKS = 200
    }
}
