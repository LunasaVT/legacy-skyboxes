package dev.prismriver.legacyskyboxes.ctm.method

import dev.prismriver.legacyskyboxes.ctm.CtmRule
import dev.prismriver.legacyskyboxes.ctm.Symmetry
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction

object RandomCtm {
    private fun positionHash(pos: BlockPos, direction: Direction, symmetry: Symmetry, linked: Boolean): Long {
        val x = pos.x.toLong()
        val y = pos.y.toLong()
        val z = pos.z.toLong()
        val faceComponent = if (linked) 0L else directionSalt(direction, symmetry)
        var h = x * 3129871L xor (y * 116129781L) xor z
        h = h * h * 42317861L + h * 11L
        h = h xor faceComponent
        h = h xor (h ushr 15)
        return h
    }

    private fun directionSalt(direction: Direction, symmetry: Symmetry): Long = when (symmetry) {
        Symmetry.ALL -> 0L
        Symmetry.OPPOSITE -> (direction.axis.ordinal + 1).toLong() * 1000003L
        Symmetry.NONE -> (direction.id + 1).toLong() * 1000003L
    }

    fun pickIndex(rule: CtmRule, pos: BlockPos, direction: Direction, tileCount: Int): Int {
        var h = positionHash(pos, direction, rule.symmetry, rule.linked)
        repeat(rule.randomLoops) {
            h = h * 6364136223846793005L + 1442695040888963407L
            h = h xor (h ushr 29)
        }

        val weights = rule.weights
        if (weights.isEmpty()) {
            return Math.floorMod(h, tileCount.toLong()).toInt()
        }

        val avg = (weights.sum() / weights.size.coerceAtLeast(1)).coerceAtLeast(1)
        val fullWeights = IntArray(tileCount) { i -> weights.getOrElse(i) { avg } }
        val total = fullWeights.sum().coerceAtLeast(1)
        var target = Math.floorMod(h, total.toLong()).toInt()
        for (i in fullWeights.indices) {
            target -= fullWeights[i]
            if (target < 0) return i
        }
        return tileCount - 1
    }

    fun repeatIndex(rule: CtmRule, pos: BlockPos, direction: Direction): Int {
        val (right, up) = when (direction) {
            Direction.UP, Direction.DOWN -> Direction.EAST to Direction.SOUTH
            Direction.NORTH, Direction.SOUTH -> Direction.EAST to Direction.UP
            Direction.EAST, Direction.WEST -> Direction.SOUTH to Direction.UP
        }
        val u = Math.floorMod(pos.x * right.offsetX + pos.y * right.offsetY + pos.z * right.offsetZ, rule.repeatWidth)
        val v = Math.floorMod(pos.x * up.offsetX + pos.y * up.offsetY + pos.z * up.offsetZ, rule.repeatHeight)
        return v * rule.repeatWidth + u
    }
}
