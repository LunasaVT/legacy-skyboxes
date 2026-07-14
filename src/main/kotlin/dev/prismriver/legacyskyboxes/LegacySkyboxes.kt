package dev.prismriver.legacyskyboxes

import dev.prismriver.legacyskyboxes.sky.CelestialOverride
import dev.prismriver.legacyskyboxes.sky.Skybox
import dev.prismriver.legacyskyboxes.sky.SkyboxLoader
import dev.prismriver.legacyskyboxes.sky.SkyboxManager
import dev.prismriver.legacyskyboxes.sky.render.CelestialRenderer
import dev.prismriver.legacyskyboxes.sky.render.SkyboxRenderer
import net.fabricmc.api.ClientModInitializer
import net.minecraft.client.Minecraft
import net.minecraft.client.resource.manager.ReloadableResourceManager
import net.minecraft.client.resource.manager.ResourceManager
import net.minecraft.resource.Identifier
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

class LegacySkyboxes : ClientModInitializer {
    override fun onInitializeClient() {
        config = LegacySkyboxesConfig.load()
    }

    companion object {
        @JvmField
        val LOGGER: Logger = LogManager.getLogger("legacy-skyboxes")

        @JvmField
        val manager = SkyboxManager()

        @JvmField
        var config = LegacySkyboxesConfig()

        private var activeCelestialSkybox: Skybox? = null
        private var celestialBrightness: Float = 1.0f

        @JvmStatic
        fun registerResourceReloadListener(resourceManager: ResourceManager) {
            (resourceManager as ReloadableResourceManager).addListener(SkyboxLoader(manager))
        }

        @JvmStatic
        fun renderActiveSkyboxes(partialTick: Float) {
            if (!config.enabled) return
            val mc = Minecraft.getInstance()
            val world = mc.world ?: return

            val allowFallback = config.showOverworldForUnknownDimension
            manager.update(world, allowFallback)

            val dimensionId = world.dimension.getId()
            val active = manager.getActive(dimensionId, allowFallback)
            SkyboxRenderer.render(mc, world, active, partialTick)
        }

        @JvmStatic
        fun prepareCelestial(partialTick: Float) {
            activeCelestialSkybox = null
            if (!config.enabled) return
            val world = Minecraft.getInstance().world ?: return

            val allowFallback = config.showOverworldForUnknownDimension
            activeCelestialSkybox = manager.getActive(world.dimension.getId(), allowFallback).firstOrNull()
            celestialBrightness = 1.0f - world.getRain(partialTick).coerceIn(0.0f, 1.0f)
        }

        @JvmStatic
        fun resolveCelestialTexture(default: Identifier): Identifier {
            val skybox = activeCelestialSkybox ?: return default
            val override: CelestialOverride = when (default) {
                CelestialOverride.SUN_TEXTURE -> skybox.sun
                CelestialOverride.MOON_PHASES_TEXTURE -> skybox.moonPhases
                else -> null
            } ?: return default

            CelestialRenderer.applyOverride(override.blend, celestialBrightness)
            return override.texture
        }
    }
}
