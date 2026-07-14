package dev.prismriver.legacyskyboxes.ctm.method

import dev.prismriver.legacyskyboxes.ctm.CtmManager
import dev.prismriver.legacyskyboxes.ctm.CtmMethod
import dev.prismriver.legacyskyboxes.ctm.CtmRule
import net.minecraft.block.Block
import net.minecraft.block.state.BlockState
import net.minecraft.resource.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.world.WorldView

object OverlayCtm {
    fun connectivityPredicate(world: WorldView, rule: CtmRule, sourceState: BlockState, direction: Direction): (BlockPos) -> Boolean {
        val restrictToBlocks = rule.connectBlocks.map { it.identifier }.toSet()
        val restrictToTiles = rule.connectTiles.toSet()
        val hasRestriction = restrictToBlocks.isNotEmpty() || restrictToTiles.isNotEmpty()

        return { pos ->
            val neighborState = world.getBlockState(pos)
            if (neighborState == null) {
                false
            } else if (hasRestriction) {
                val neighborId = Block.REGISTRY.getKey(neighborState.block)
                val blockOk = restrictToBlocks.isNotEmpty() && neighborId in restrictToBlocks
                val tileOk = restrictToTiles.isNotEmpty() && neighborTileMatches(neighborState, direction, restrictToTiles)
                blockOk || tileOk
            } else {
                neighborState.block === sourceState.block
            }
        }
    }

    private fun neighborTileMatches(neighborState: BlockState, direction: Direction, allowed: Set<Identifier>): Boolean {
        val index = CtmManager.spriteIndexOrNull() ?: return false
        val model = net.minecraft.client.Minecraft.getInstance().blockRenderDispatcher.modelShaper.getModel(neighborState)
        val quad = model.getQuads(direction).firstOrNull() ?: return false
        val u = java.lang.Float.intBitsToFloat(quad.vertices[4])
        val v = java.lang.Float.intBitsToFloat(quad.vertices[5])
        val sprite = index.find(u, v) ?: return false
        return Identifier(sprite.name) in allowed
    }

    fun tileIndex(world: WorldView, pos: BlockPos, state: BlockState, direction: Direction, rule: CtmRule, tileCount: Int): Int {
        val connected = connectivityPredicate(world, rule, state, direction)
        return when (rule.method) {
            CtmMethod.OVERLAY_CTM -> StandardCtm.tileIndexFor(StandardCtm.sampleMask(pos, direction, connected))
            CtmMethod.OVERLAY_RANDOM -> RandomCtm.pickIndex(rule, pos, direction, tileCount)
            CtmMethod.OVERLAY_REPEAT -> RandomCtm.repeatIndex(rule, pos, direction)
            CtmMethod.OVERLAY -> edgeTransitionIndex(pos, direction, connected)
            CtmMethod.OVERLAY_FIXED -> 0
            else -> 0
        }
    }

    private fun edgeTransitionIndex(pos: BlockPos, direction: Direction, connected: (BlockPos) -> Boolean): Int {
        val n = NeighborSampler.neighborsOf(pos, direction)
        var mask = 0
        if (connected(n.north)) mask = mask or 1
        if (connected(n.east)) mask = mask or 2
        if (connected(n.south)) mask = mask or 4
        if (connected(n.west)) mask = mask or 8
        return mask
    }
}
