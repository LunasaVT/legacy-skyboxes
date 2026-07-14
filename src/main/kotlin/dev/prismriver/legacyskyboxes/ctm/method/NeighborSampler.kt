package dev.prismriver.legacyskyboxes.ctm.method

import dev.prismriver.legacyskyboxes.ctm.ConnectMode
import dev.prismriver.legacyskyboxes.ctm.CtmManager
import net.minecraft.block.Block
import net.minecraft.block.state.BlockState
import net.minecraft.client.Minecraft
import net.minecraft.resource.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.world.WorldView

class FaceNeighbors(
    val north: BlockPos, val south: BlockPos, val east: BlockPos, val west: BlockPos,
    val northEast: BlockPos, val northWest: BlockPos, val southEast: BlockPos, val southWest: BlockPos,
)

object NeighborSampler {
    private fun basis(direction: Direction): Pair<Direction, Direction> = when (direction) {
        Direction.DOWN -> Direction.EAST to Direction.SOUTH
        Direction.UP -> Direction.EAST to Direction.NORTH
        Direction.NORTH -> Direction.WEST to Direction.UP
        Direction.SOUTH -> Direction.EAST to Direction.UP
        Direction.WEST -> Direction.SOUTH to Direction.UP
        Direction.EAST -> Direction.NORTH to Direction.UP
    }

    fun neighborsOf(pos: BlockPos, direction: Direction): FaceNeighbors {
        val (right, top) = basis(direction)
        val n = pos.offset(top)
        val s = pos.offset(top.opposite)
        val e = pos.offset(right)
        val w = pos.offset(right.opposite)
        return FaceNeighbors(
            north = n, south = s, east = e, west = w,
            northEast = n.offset(right), northWest = n.offset(right.opposite),
            southEast = s.offset(right), southWest = s.offset(right.opposite),
        )
    }

    fun isConnected(
        world: WorldView,
        sourcePos: BlockPos,
        sourceState: BlockState,
        direction: Direction,
        neighborPos: BlockPos,
        connect: ConnectMode,
    ): Boolean {
        val neighborState = world.getBlockState(neighborPos) ?: return false
        return when (connect) {
            ConnectMode.BLOCK -> neighborState.block === sourceState.block
            ConnectMode.STATE -> neighborState == sourceState
            ConnectMode.TILE -> tileMatches(sourceState, neighborState, direction)
        }
    }

    private fun tileMatches(sourceState: BlockState, neighborState: BlockState, direction: Direction): Boolean {
        val index = CtmManager.spriteIndexOrNull() ?: return neighborState.block === sourceState.block
        val sourceUv = firstQuadUv(sourceState, direction) ?: return neighborState.block === sourceState.block
        val neighborUv = firstQuadUv(neighborState, direction) ?: return false

        val sourceSprite = index.find(sourceUv.first, sourceUv.second) ?: return false
        val neighborSprite = index.find(neighborUv.first, neighborUv.second) ?: return false
        return sourceSprite.name == neighborSprite.name
    }

    private fun firstQuadUv(state: BlockState, direction: Direction): Pair<Float, Float>? {
        val model = Minecraft.getInstance().blockRenderDispatcher.modelShaper.getModel(state)
        val quad = model.getQuads(direction).firstOrNull() ?: return null
        val vertices = quad.vertices
        return java.lang.Float.intBitsToFloat(vertices[4]) to java.lang.Float.intBitsToFloat(vertices[5])
    }

    fun matchBlockOrTileIdentity(block: Block): Identifier? = Block.REGISTRY.getKey(block)
}
