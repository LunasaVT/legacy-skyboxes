package dev.prismriver.legacyskyboxes.ctm.method

import dev.prismriver.legacyskyboxes.ctm.ConnectMode
import net.minecraft.block.state.BlockState
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.world.WorldView

object StandardCtm {
    fun sampleMask(pos: BlockPos, direction: Direction, connected: (BlockPos) -> Boolean): Int {
        val n = NeighborSampler.neighborsOf(pos, direction)
        val north = connected(n.north); val south = connected(n.south)
        val west = connected(n.west); val east = connected(n.east)
        val nw = connected(n.northWest) && north && west
        val ne = connected(n.northEast) && north && east
        val sw = connected(n.southWest) && south && west
        val se = connected(n.southEast) && south && east

        var mask = 0
        if (north) mask = mask or 1
        if (south) mask = mask or 2
        if (west) mask = mask or 4
        if (east) mask = mask or 8
        if (nw) mask = mask or 16
        if (ne) mask = mask or 32
        if (sw) mask = mask or 64
        if (se) mask = mask or 128
        return mask
    }

    fun sampleMask(
        world: WorldView, pos: BlockPos, state: BlockState, direction: Direction, connect: ConnectMode,
    ): Int = sampleMask(pos, direction) { p -> NeighborSampler.isConnected(world, pos, state, direction, p, connect) }

    fun tileIndexFor(mask: Int): Int = MASK_TO_TILE[mask] ?: 0

    fun compactTileIndexFor(mask: Int): Int {
        val edgeCount = intArrayOf(1, 2, 4, 8).count { (mask and it) != 0 }
        val diagCount = intArrayOf(16, 32, 64, 128).count { (mask and it) != 0 }
        return when {
            edgeCount == 4 && diagCount == 4 -> 0
            edgeCount == 4 && diagCount < 4 -> 1
            else -> 2
        }
    }

    private val MASK_TO_TILE: Map<Int, Int> = mapOf(
        0 to 0, 1 to 36, 2 to 12, 3 to 24, 4 to 3, 5 to 17, 6 to 5, 7 to 19,
        8 to 1, 9 to 16, 10 to 4, 11 to 6, 12 to 2, 13 to 18, 14 to 7, 15 to 46,
        21 to 39, 23 to 41, 29 to 42, 31 to 20, 41 to 37, 43 to 30, 45 to 40, 47 to 8,
        61 to 38, 63 to 11, 70 to 15, 71 to 43, 78 to 29, 79 to 21, 87 to 27, 95 to 10,
        111 to 34, 127 to 32, 138 to 13, 139 to 28, 142 to 31, 143 to 9, 159 to 35, 171 to 25,
        175 to 23, 191 to 33, 206 to 14, 207 to 22, 223 to 44, 239 to 45, 255 to 26,
    )
}
