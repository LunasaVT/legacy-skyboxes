package dev.prismriver.legacyskyboxes.ctm

import dev.prismriver.legacyskyboxes.LegacySkyboxes
import net.minecraft.client.resource.manager.ResourceManager
import net.minecraft.client.render.texture.TextureAtlas
import net.minecraft.resource.Identifier
import java.io.IOException
import java.util.Properties

object CtmLoader {
    private var pendingRules: List<CtmRule> = emptyList()

    fun beginReload(atlas: TextureAtlas, resourceManager: ResourceManager) {
        if (!LegacySkyboxes.config.ctmEnabled) {
            pendingRules = emptyList()
            return
        }

        val ids = CtmDiscovery.discoverPropertiesIds()
        val rules = ArrayList<CtmRule>(ids.size)

        for (id in ids) {
            val resource = try {
                resourceManager.getResource(id)
            } catch (e: IOException) {
                LegacySkyboxes.LOGGER.warn("Failed to read CTM properties '{}': {}", id, e.message)
                null
            } ?: continue

            val props = Properties()
            try {
                resource.asStream().use { props.load(it) }
            } catch (e: IOException) {
                LegacySkyboxes.LOGGER.warn("Failed to read CTM properties '{}': {}", id, e.message)
                continue
            }

            val fileName = id.path.substringAfterLast('/')
            try {
                val rule = CtmPropertiesParser.parse(props, id, weight = 0, fileName = fileName)
                rules += rule
                if (LegacySkyboxes.config.ctmDebug) {
                    LegacySkyboxes.LOGGER.info(
                        "Parsed CTM rule '{}': method={} matchBlocks={} matchTiles={}",
                        id, rule.method, rule.matchBlocks, rule.matchTiles,
                    )
                }
            } catch (e: CtmParseException) {
                LegacySkyboxes.LOGGER.warn("Failed to parse CTM properties '{}': {}", id, e.message)
            }
        }

        pendingRules = rules

        val tileIds = LinkedHashSet<Identifier>()
        for (rule in rules) {
            for (ref in rule.tiles) {
                if (ref is TileRef.Named) tileIds += ref.id
            }
        }
        for (id in tileIds) {
            atlas.registerSprite(id)
        }
    }

    fun finishReload(atlas: TextureAtlas) {
        val rules = pendingRules
        pendingRules = emptyList()
        if (rules.isEmpty()) {
            CtmManager.clear()
            return
        }

        val resolved = rules.map { rule -> resolveTiles(atlas, rule) }
        val index = AtlasSpriteIndex.build(atlas)
        CtmManager.publish(resolved, index)

        LegacySkyboxes.LOGGER.info("Loaded {} connected-texture rule(s)", resolved.size)
    }

    private fun resolveTiles(atlas: TextureAtlas, rule: CtmRule): ResolvedCtmRule {
        val missing = atlas.missingSprite
        val tiles = rule.tiles.map { ref ->
            when (ref) {
                is TileRef.Skip -> ResolvedTile(null, isSkip = true)
                is TileRef.Default -> ResolvedTile(null, isSkip = false)
                is TileRef.Named -> {
                    val sprite = atlas.getSprite(ref.id.toString())
                    if (sprite === missing) {
                        LegacySkyboxes.LOGGER.warn("CTM tile '{}' failed to stitch (missing texture)", ref.id)
                    }
                    ResolvedTile(sprite, isSkip = false)
                }
            }
        }
        return ResolvedCtmRule(rule, tiles)
    }
}
