package dev.prismriver.legacyskyboxes.ctm

import dev.prismriver.legacyskyboxes.sky.component.HeightRange
import net.minecraft.block.Block
import net.minecraft.block.state.BlockState
import net.minecraft.resource.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.world.WorldView

class RuleMatch(val resolved: ResolvedCtmRule, val matchedViaTile: Boolean)

object CtmManager {
    @Volatile private var blockRules: Map<Identifier, List<ResolvedCtmRule>> = emptyMap()
    @Volatile private var tileRules: List<Pair<Identifier, ResolvedCtmRule>> = emptyList()
    @Volatile private var spriteIndex: AtlasSpriteIndex? = null

    val isActive: Boolean get() = blockRules.isNotEmpty() || tileRules.isNotEmpty()

    fun publish(rules: List<ResolvedCtmRule>, index: AtlasSpriteIndex) {
        val byBlock = HashMap<Identifier, MutableList<ResolvedCtmRule>>()
        val tiles = ArrayList<Pair<Identifier, ResolvedCtmRule>>()

        val ordered = rules.sortedWith(compareByDescending<ResolvedCtmRule> { it.rule.weight }.thenBy { it.rule.fileName })
        for (resolved in ordered) {
            for (tileId in resolved.rule.matchTiles) {
                tiles += tileId to resolved
            }
            for (matcher in resolved.rule.matchBlocks) {
                byBlock.getOrPut(matcher.identifier) { ArrayList() }.add(resolved)
            }
        }

        blockRules = byBlock
        tileRules = tiles
        spriteIndex = index
    }

    fun clear() {
        blockRules = emptyMap()
        tileRules = emptyList()
        spriteIndex = null
    }

    fun spriteIndexOrNull(): AtlasSpriteIndex? = spriteIndex

    fun findRule(world: WorldView, pos: BlockPos, state: BlockState, direction: Direction, quadU: Float, quadV: Float): RuleMatch? {
        if (!isActive) return null

        if (tileRules.isNotEmpty()) {
            val sprite = spriteIndex?.find(quadU, quadV)
            if (sprite != null) {
                val spriteId = Identifier(sprite.name)
                for ((tileId, resolved) in tileRules) {
                    if (tileId == spriteId && passesFilters(resolved.rule, world, pos, direction)) {
                        return RuleMatch(resolved, matchedViaTile = true)
                    }
                }
            }
        }

        val block = state.block
        val blockId = Block.REGISTRY.getKey(block) ?: return null
        val candidates = blockRules[blockId] ?: return null
        for (resolved in candidates) {
            val matcher = resolved.rule.matchBlocks.firstOrNull { it.identifier == blockId } ?: continue
            if (!matchesProperties(matcher, state)) continue
            if (!passesFilters(resolved.rule, world, pos, direction)) continue
            return RuleMatch(resolved, matchedViaTile = false)
        }
        return null
    }

    fun matchesProperties(matcher: BlockMatcher, state: BlockState): Boolean {
        if (matcher.properties.isEmpty()) return true
        for (property in state.properties()) {
            val allowed = matcher.properties[property.name] ?: continue
            @Suppress("UNCHECKED_CAST")
            val value = state.get(property as net.minecraft.block.state.property.Property<Comparable<Any>>)
            val valueName = property.getName(value as Comparable<Any>)
            if (valueName !in allowed) return false
        }
        return true
    }

    private fun passesFilters(rule: CtmRule, world: WorldView, pos: BlockPos, direction: Direction): Boolean {
        if (rule.faces.isNotEmpty() && direction !in rule.faces) return false

        if (!rule.biomes.isEmpty) {
            val biome = world.getBiome(pos)
            if (!rule.biomes.matches(biome?.name)) return false
        }

        if (rule.heights.isNotEmpty()) {
            val y = pos.y.toFloat()
            if (!HeightRange.anyContains(rule.heights, y)) return false
        }

        if (rule.name != null) {
            val entity = world.getBlockEntity(pos) as? net.minecraft.world.Nameable
            val currentName = entity?.takeIf { it.hasCustomName() }?.name
            if (currentName == null || !currentName.equals(rule.name, ignoreCase = true)) return false
        }

        return true
    }
}
