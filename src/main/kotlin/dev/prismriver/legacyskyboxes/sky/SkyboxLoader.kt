package dev.prismriver.legacyskyboxes.sky

import dev.prismriver.legacyskyboxes.LegacySkyboxes
import net.minecraft.client.resource.Resource
import net.minecraft.client.resource.manager.ResourceManager
import net.minecraft.client.resource.manager.ResourceReloadListener
import net.minecraft.resource.Identifier
import java.io.IOException
import java.util.Properties

class SkyboxLoader(private val manager: SkyboxManager) : ResourceReloadListener {
    override fun reload(resourceManager: ResourceManager) {
        val loaded = ArrayList<Skybox>()
        for (dimensionId in SUPPORTED_DIMENSIONS) {
            val dimension = loadDimension(resourceManager, dimensionId)
            if (dimension != null) {
                loaded.add(dimension)
            }
        }

        manager.replaceAll(loaded)

        if (loaded.isEmpty()) {
            LegacySkyboxes.LOGGER.info("No custom skies found in the active resource packs :(")
        } else {
            val layerCount = loaded.sumOf { it.layers.size }
            val celestialCount = loaded.count { it.sun != null || it.moonPhases != null }
            LegacySkyboxes.LOGGER.info(
                "Loaded {} custom sky layer(s) and {} celestial override(s) across {} dimension(s)",
                layerCount, celestialCount, loaded.size
            )
        }
    }

    private fun loadDimension(resourceManager: ResourceManager, dimensionId: Int): Skybox? {
        var scan = scanPrefix(resourceManager, OPTIFINE_PREFIX, dimensionId)
        if (scan.layers.isEmpty()) {
            scan = scanPrefix(resourceManager, MCPATCHER_PREFIX, dimensionId)
        }

        val sun = loadCelestial(resourceManager, OPTIFINE_PREFIX, dimensionId, "sun")
            ?: loadCelestial(resourceManager, MCPATCHER_PREFIX, dimensionId, "sun")
        val moonPhases = loadCelestial(resourceManager, OPTIFINE_PREFIX, dimensionId, "moon_phases")
            ?: loadCelestial(resourceManager, MCPATCHER_PREFIX, dimensionId, "moon_phases")

        if (scan.layers.isEmpty() && sun == null && moonPhases == null) {
            return null
        }
        return Skybox(dimensionId, scan.layers, sun, moonPhases)
    }

    private fun loadCelestial(
        resourceManager: ResourceManager,
        prefix: String,
        dimensionId: Int,
        name: String,
    ): CelestialOverride? {
        val id = Identifier(DEFAULT_NAMESPACE, "$prefix/world$dimensionId/$name.properties")
        val resource = tryGetResource(resourceManager, id) ?: return null

        val properties = Properties()
        try {
            resource.asStream().use { properties.load(it) }
        } catch (e: IOException) {
            LegacySkyboxes.LOGGER.warn("Failed to read custom sky properties '{}': {}", id, e.message)
            return null
        }

        val override = try {
            SkyboxPropertiesParser.parseCelestial(properties, id)
        } catch (e: RuntimeException) {
            LegacySkyboxes.LOGGER.warn("Failed to parse custom sky '{}': {}", id, e.message)
            return null
        }

        if (!exists(resourceManager, override.texture)) {
            LegacySkyboxes.LOGGER.warn("Skipping custom sky '{}' due to the lack of texture '{}'", id, override.texture)
            return null
        }

        if (LegacySkyboxes.config.debug) {
            LegacySkyboxes.LOGGER.info("Loaded custom sky '{}' with texture '{}'", id, override.texture)
        }

        return override
    }

    private fun scanPrefix(resourceManager: ResourceManager, prefix: String, dimensionId: Int): ScanResult {
        val folder = "world$dimensionId"
        val layers = ArrayList<SkyboxLayer>()
        var consecutiveMisses = 0
        var index = 1

        while (index <= MAX_SKY_INDEX && consecutiveMisses < CONSECUTIVE_MISS_LIMIT) {
            val id = Identifier(DEFAULT_NAMESPACE, "$prefix/$folder/sky$index.properties")
            val resource = tryGetResource(resourceManager, id)
            if (resource == null) {
                consecutiveMisses++
                index++
                continue
            }

            consecutiveMisses = 0
            parseLayer(resourceManager, resource, id)?.let {
                layers += it
            }
            index++
        }

        return ScanResult(layers)
    }

    private fun parseLayer(resourceManager: ResourceManager, resource: Resource, id: Identifier): SkyboxLayer? {
        val properties = Properties()
        try {
            resource.asStream().use { properties.load(it) }
        } catch (e: IOException) {
            LegacySkyboxes.LOGGER.warn("Failed to read custom sky properties '{}': {}", id, e.message)
            return null
        }

        val layer = try {
            SkyboxPropertiesParser.parse(properties, id)
        } catch (e: RuntimeException) {
            LegacySkyboxes.LOGGER.warn("Failed to parse custom sky layer '{}': {}", id, e.message)
            return null
        }

        if (!exists(resourceManager, layer.texture)) {
            LegacySkyboxes.LOGGER.warn("Skipping custom sky layer '{}' due to the lack of texture '{}'", id, layer.texture)
            return null
        }

        if (LegacySkyboxes.config.debug) {
            LegacySkyboxes.LOGGER.info("Loaded custom sky layer '{}' with texture '{}'", id, layer.texture)
        }

        return layer
    }

    private fun tryGetResource(resourceManager: ResourceManager, id: Identifier): Resource? {
        return try {
            resourceManager.getResource(id)
        } catch (_: IOException) {
            null
        }
    }

    private fun exists(resourceManager: ResourceManager, id: Identifier): Boolean {
        return try {
            resourceManager.getResource(id) != null
        } catch (_: IOException) {
            false
        }
    }

    private class ScanResult(val layers: List<SkyboxLayer>)

    companion object {
        private const val DEFAULT_NAMESPACE = "minecraft"
        private const val OPTIFINE_PREFIX = "optifine/sky"
        private const val MCPATCHER_PREFIX = "mcpatcher/sky"

        private val SUPPORTED_DIMENSIONS = intArrayOf(0, -1, 1)

        private const val MAX_SKY_INDEX = 1000
        // this number not being high causes missing layers
        private const val CONSECUTIVE_MISS_LIMIT = 200
    }
}
