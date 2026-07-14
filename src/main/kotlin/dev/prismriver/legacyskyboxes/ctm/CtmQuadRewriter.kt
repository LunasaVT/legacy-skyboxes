package dev.prismriver.legacyskyboxes.ctm

import dev.prismriver.legacyskyboxes.ctm.method.DirectionalCtm
import dev.prismriver.legacyskyboxes.ctm.method.OverlayCtm
import dev.prismriver.legacyskyboxes.ctm.method.RandomCtm
import dev.prismriver.legacyskyboxes.ctm.method.StandardCtm
import net.minecraft.block.state.BlockState
import net.minecraft.client.render.texture.TextureAtlasSprite
import net.minecraft.client.resource.model.BakedQuad
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.world.WorldView

object CtmQuadRewriter {
    fun rewrite(world: WorldView, pos: BlockPos, explicitDirection: Direction?, quads: List<BakedQuad>): List<BakedQuad> {
        if (!CtmManager.isActive || quads.isEmpty()) return quads

        val state = world.getBlockState(pos) ?: return quads
        var result: MutableList<BakedQuad>? = null

        for ((index, quad) in quads.withIndex()) {
            val direction = explicitDirection ?: quad.face
            val vertices = quad.vertices
            val u0 = java.lang.Float.intBitsToFloat(vertices[4])
            val v0 = java.lang.Float.intBitsToFloat(vertices[5])

            val match = CtmManager.findRule(world, pos, state, direction, u0, v0)
            if (match == null) {
                result?.add(quad)
                continue
            }

            val rewritten = applyRule(world, pos, state, direction, quad, match)
            if (rewritten == null) {
                result?.add(quad)
                continue
            }

            if (result == null) {
                result = ArrayList(quads.size + 4)
                for (i in 0 until index) result.add(quads[i])
            }
            result.addAll(rewritten)
        }

        return result ?: quads
    }

    private fun applyRule(
        world: WorldView, pos: BlockPos, state: BlockState, direction: Direction, quad: BakedQuad, match: RuleMatch,
    ): List<BakedQuad>? {
        val rule = match.resolved.rule
        val tiles = match.resolved.resolvedTiles
        val connect = rule.connect ?: if (match.matchedViaTile) ConnectMode.TILE else ConnectMode.BLOCK

        if (rule.method.isOverlay) {
            val tileIndex = OverlayCtm.tileIndex(world, pos, state, direction, rule, tiles.size)
            val tile = tiles.getOrNull(tileIndex.coerceIn(0, tiles.size - 1)) ?: return null
            if (tile.isSkip || tile.sprite == null) return listOf(quad)
            val overlayQuad = remapWholeTile(quad, tile.sprite)
            return listOf(quad, applyTint(overlayQuad, rule))
        }

        val tileIndex = when (rule.method) {
            CtmMethod.CTM -> {
                val mask = StandardCtm.sampleMask(world, pos, state, direction, connect)
                StandardCtm.tileIndexFor(mask)
            }
            CtmMethod.CTM_COMPACT -> {
                val mask = StandardCtm.sampleMask(world, pos, state, direction, connect)
                rule.ctmOverrides[mask] ?: StandardCtm.compactTileIndexFor(mask)
            }
            CtmMethod.HORIZONTAL -> DirectionalCtm.horizontalIndex(world, pos, state, direction, connect)
            CtmMethod.VERTICAL -> DirectionalCtm.verticalIndex(world, pos, state, direction, connect)
            CtmMethod.HORIZONTAL_VERTICAL -> DirectionalCtm.combinedIndex(world, pos, state, direction, connect, horizontalFirst = true)
            CtmMethod.VERTICAL_HORIZONTAL -> DirectionalCtm.combinedIndex(world, pos, state, direction, connect, horizontalFirst = false)
            CtmMethod.TOP -> 0
            CtmMethod.FIXED -> 0
            CtmMethod.RANDOM -> RandomCtm.pickIndex(rule, pos, direction, tiles.size)
            CtmMethod.REPEAT -> RandomCtm.repeatIndex(rule, pos, direction)
            else -> return null
        }

        val tile = tiles.getOrNull(tileIndex.coerceIn(0, (tiles.size - 1).coerceAtLeast(0))) ?: return null
        if (tile.isSkip) return null
        val sprite = tile.sprite ?: return null

        val rewritten = remapWithinSprite(quad, sprite) ?: return null
        return listOf(rewritten)
    }

    private fun remapWithinSprite(quad: BakedQuad, replacement: TextureAtlasSprite): BakedQuad? {
        val source = quad.vertices
        val v0u = java.lang.Float.intBitsToFloat(source[4])
        val v0v = java.lang.Float.intBitsToFloat(source[5])
        val originalSprite = CtmManager.spriteIndexOrNull()?.find(v0u, v0v) ?: return remapWholeTile(quad, replacement)

        val out = source.copyOf()
        val uSpan = (originalSprite.uMax - originalSprite.uMin).takeIf { it != 0f } ?: return null
        val vSpan = (originalSprite.vMax - originalSprite.vMin).takeIf { it != 0f } ?: return null

        for (v in 0 until 4) {
            val base = v * 7
            val u = java.lang.Float.intBitsToFloat(source[base + 4])
            val vv = java.lang.Float.intBitsToFloat(source[base + 5])
            val localU = ((u - originalSprite.uMin) / uSpan * 16.0)
            val localV = ((vv - originalSprite.vMin) / vSpan * 16.0)
            out[base + 4] = java.lang.Float.floatToRawIntBits(replacement.getU(localU))
            out[base + 5] = java.lang.Float.floatToRawIntBits(replacement.getV(localV))
        }
        return BakedQuad(out, quad.tintIndex, quad.face)
    }

    private fun remapWholeTile(quad: BakedQuad, replacement: TextureAtlasSprite): BakedQuad {
        val source = quad.vertices
        val out = source.copyOf()
        for (v in 0 until 4) {
            val base = v * 7
            val u = java.lang.Float.intBitsToFloat(source[base + 4])
            val vv = java.lang.Float.intBitsToFloat(source[base + 5])
            val originalSprite = CtmManager.spriteIndexOrNull()?.find(u, vv)
            val localU: Double
            val localV: Double
            if (originalSprite != null) {
                val uSpan = (originalSprite.uMax - originalSprite.uMin).takeIf { it != 0f } ?: 1f
                val vSpan = (originalSprite.vMax - originalSprite.vMin).takeIf { it != 0f } ?: 1f
                localU = (u - originalSprite.uMin) / uSpan * 16.0
                localV = (vv - originalSprite.vMin) / vSpan * 16.0
            } else {
                localU = 0.0
                localV = 0.0
            }
            out[base + 4] = java.lang.Float.floatToRawIntBits(replacement.getU(localU))
            out[base + 5] = java.lang.Float.floatToRawIntBits(replacement.getV(localV))
        }
        return BakedQuad(out, quad.tintIndex, quad.face)
    }

    private fun applyTint(quad: BakedQuad, rule: CtmRule): BakedQuad {
        if (rule.tintIndex < 0) return quad
        return BakedQuad(quad.vertices, rule.tintIndex, quad.face)
    }
}
