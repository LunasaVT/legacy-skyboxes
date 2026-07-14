package dev.prismriver.legacyskyboxes.ctm.method

import dev.prismriver.legacyskyboxes.ctm.ConnectMode
import net.minecraft.block.state.BlockState
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.world.WorldView

object DirectionalCtm {
    private fun axisIndex(negative: Boolean, positive: Boolean): Int = when {
        negative && positive -> 0
        negative -> 1
        positive -> 2
        else -> 3
    }

    fun horizontalIndex(pos: BlockPos, direction: Direction, connected: (BlockPos) -> Boolean): Int {
        val n = NeighborSampler.neighborsOf(pos, direction)
        return axisIndex(connected(n.west), connected(n.east))
    }

    fun verticalIndex(pos: BlockPos, direction: Direction, connected: (BlockPos) -> Boolean): Int {
        val n = NeighborSampler.neighborsOf(pos, direction)
        return axisIndex(connected(n.south), connected(n.north))
    }

    fun combinedIndex(pos: BlockPos, direction: Direction, horizontalFirst: Boolean, connected: (BlockPos) -> Boolean): Int {
        val h = horizontalIndex(pos, direction, connected)
        val v = verticalIndex(pos, direction, connected)
        return if (horizontalFirst) {
            if (h != 3) h else if (v == 3) 3 else 3 + v
        } else {
            if (v != 3) v else if (h == 3) 3 else 3 + h
        }
    }

    fun horizontalIndex(world: WorldView, pos: BlockPos, state: BlockState, direction: Direction, connect: ConnectMode): Int =
        horizontalIndex(pos, direction) { p -> NeighborSampler.isConnected(world, pos, state, direction, p, connect) }

    fun verticalIndex(world: WorldView, pos: BlockPos, state: BlockState, direction: Direction, connect: ConnectMode): Int =
        verticalIndex(pos, direction) { p -> NeighborSampler.isConnected(world, pos, state, direction, p, connect) }

    fun combinedIndex(
        world: WorldView, pos: BlockPos, state: BlockState, direction: Direction, connect: ConnectMode, horizontalFirst: Boolean,
    ): Int = combinedIndex(pos, direction, horizontalFirst) { p -> NeighborSampler.isConnected(world, pos, state, direction, p, connect) }
}
