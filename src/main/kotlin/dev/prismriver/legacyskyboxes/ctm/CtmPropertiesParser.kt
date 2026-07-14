package dev.prismriver.legacyskyboxes.ctm

import dev.prismriver.legacyskyboxes.LegacySkyboxes
import dev.prismriver.legacyskyboxes.sky.component.Biomes
import dev.prismriver.legacyskyboxes.sky.component.HeightRange
import net.minecraft.resource.Identifier
import net.minecraft.util.math.Direction
import java.util.Properties

class CtmParseException(message: String) : RuntimeException(message)

object CtmPropertiesParser {
    fun parse(props: Properties, propertiesId: Identifier, weight: Int, fileName: String): CtmRule {
        val method = CtmMethod.fromName(props.getProperty("method"))
            ?: throw CtmParseException("missing or unknown method")

        val tiles = props.getProperty("tiles")?.let { TileRef.parseList(it, propertiesId) }
            ?: throw CtmParseException("missing tiles")
        if (tiles.isEmpty()) {
            throw CtmParseException("tiles list is empty")
        }

        val matchTiles = props.getProperty("matchTiles")
            ?.trim()?.split(Regex("\\s+"))?.filter { it.isNotEmpty() }
            ?.map { resolveTileOrBlockPath(it, propertiesId) }
            ?: emptyList()

        val matchBlocks = parseBlockMatchers(props.getProperty("matchBlocks"))
            ?: inferMatchBlocksFromFileName(fileName)

        return CtmRule(
            sourceId = propertiesId,
            fileName = fileName,
            weight = props.getProperty("weight")?.trim()?.toIntOrNull() ?: weight,
            method = method,
            tiles = tiles,
            matchTiles = matchTiles,
            matchBlocks = matchBlocks,
            connect = when (props.getProperty("connect")?.trim()?.lowercase()) {
                "block" -> ConnectMode.BLOCK
                "tile" -> ConnectMode.TILE
                "state" -> ConnectMode.STATE
                else -> null
            },
            faces = parseFaces(props.getProperty("faces")),
            biomes = props.getProperty("biomes")?.let { Biomes.parse(it) } ?: Biomes.ANY,
            heights = parseHeights(props),
            name = props.getProperty("name")?.trim(),
            innerSeams = props.getProperty("innerSeams")?.trim()?.toBoolean() ?: false,
            ctmOverrides = parseCtmOverrides(props),
            weights = props.getProperty("weights")?.trim()?.split(Regex("\\s+"))
                ?.filter { it.isNotEmpty() }?.mapNotNull { it.toIntOrNull() } ?: emptyList(),
            randomLoops = (props.getProperty("randomLoops")?.trim()?.toIntOrNull() ?: 0).coerceIn(0, 9),
            symmetry = when (props.getProperty("symmetry")?.trim()?.lowercase()) {
                "opposite" -> Symmetry.OPPOSITE
                "all" -> Symmetry.ALL
                else -> Symmetry.NONE
            },
            linked = props.getProperty("linked")?.trim()?.toBoolean() ?: false,
            repeatWidth = props.getProperty("width")?.trim()?.toIntOrNull() ?: 1,
            repeatHeight = props.getProperty("height")?.trim()?.toIntOrNull() ?: 1,
            connectTiles = props.getProperty("connectTiles")
                ?.trim()?.split(Regex("\\s+"))?.filter { it.isNotEmpty() }
                ?.map { resolveTileOrBlockPath(it, propertiesId) } ?: emptyList(),
            connectBlocks = parseBlockMatchers(props.getProperty("connectBlocks")) ?: emptyList(),
            tintIndex = props.getProperty("tintIndex")?.trim()?.toIntOrNull() ?: -1,
            tintBlock = props.getProperty("tintBlock")?.trim()?.let { parseBlockMatcher(it) },
            layer = when (props.getProperty("layer")?.trim()?.lowercase()) {
                "translucent" -> OverlayLayer.TRANSLUCENT
                else -> OverlayLayer.CUTOUT
            },
        )
    }

    private fun resolveTileOrBlockPath(raw: String, propertiesId: Identifier): Identifier {
        if (raw.endsWith(".png") || raw.contains('/')) {
            return (TileRef.parseList(raw, propertiesId).firstOrNull() as? TileRef.Named)?.id
                ?: Identifier(propertiesId.namespace, raw.removeSuffix(".png"))
        }
        if (raw.contains(':')) {
            val parts = raw.split(':', limit = 2)
            return Identifier(parts[0], "blocks/${parts[1]}")
        }
        return Identifier("minecraft", "blocks/$raw")
    }

    private fun inferMatchBlocksFromFileName(fileName: String): List<BlockMatcher> {
        val stem = fileName.removeSuffix(".properties")
        val inferred = if (stem.startsWith("block_")) stem.substring("block_".length) else return emptyList()
        if (inferred.isEmpty()) return emptyList()
        return listOf(BlockMatcher("minecraft", inferred, emptyMap()))
    }

    private fun parseBlockMatchers(raw: String?): List<BlockMatcher>? {
        if (raw == null) return null
        val tokens = raw.trim().split(Regex("\\s+")).filter { it.isNotEmpty() }
        if (tokens.isEmpty()) return null
        return tokens.map { parseBlockMatcher(it) }
    }

    private fun parseBlockMatcher(token: String): BlockMatcher {
        val parts = token.split(':')
        var index: Int
        val namespace: String
        val name: String
        if (parts.size == 1) {
            namespace = "minecraft"
            name = parts[0]
            index = 1
        } else if (parts[1].contains('=')) {
            namespace = "minecraft"
            name = parts[0]
            index = 1
        } else {
            namespace = parts[0]
            name = parts[1]
            index = 2
        }

        val properties = LinkedHashMap<String, Set<String>>()
        while (index < parts.size) {
            val clause = parts[index]
            val eq = clause.indexOf('=')
            if (eq > 0) {
                val key = clause.substring(0, eq)
                val values = clause.substring(eq + 1).split(',').filter { it.isNotEmpty() }.toSet()
                properties[key] = values
            }
            index++
        }

        return BlockMatcher(namespace, name, properties)
    }

    private fun parseFaces(raw: String?): Set<Direction> {
        if (raw == null) return emptySet()
        val result = LinkedHashSet<Direction>()
        for (token in raw.trim().split(Regex("\\s+"))) {
            when (token.lowercase()) {
                "top" -> result += Direction.UP
                "bottom" -> result += Direction.DOWN
                "north" -> result += Direction.NORTH
                "south" -> result += Direction.SOUTH
                "east" -> result += Direction.EAST
                "west" -> result += Direction.WEST
                "sides" -> result += listOf(Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST)
                "all" -> result += Direction.entries.toTypedArray()
                "" -> {}
                else -> LegacySkyboxes.LOGGER.warn("Unknown CTM face '{}'", token)
            }
        }
        return result
    }

    private fun parseHeights(props: Properties): List<HeightRange> {
        props.getProperty("heights")?.let { return HeightRange.parseList(it, allowNegative = true) }

        val min = props.getProperty("minHeight")?.trim()?.toIntOrNull()
        val max = props.getProperty("maxHeight")?.trim()?.toIntOrNull()
        if (min == null && max == null) return emptyList()
        return listOf(HeightRange((min ?: 0).toFloat(), (max ?: 255).toFloat()))
    }

    private fun parseCtmOverrides(props: Properties): Map<Int, Int> {
        val result = LinkedHashMap<Int, Int>()
        for (name in props.stringPropertyNames()) {
            if (!name.startsWith("ctm.")) continue
            val caseIndex = name.substring("ctm.".length).toIntOrNull() ?: continue
            val tileIndex = props.getProperty(name)?.trim()?.toIntOrNull() ?: continue
            result[caseIndex] = tileIndex
        }
        return result
    }
}
