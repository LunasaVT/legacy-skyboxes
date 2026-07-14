package dev.prismriver.legacyskyboxes.ctm

import dev.prismriver.legacyskyboxes.mixin.MixinTextureAtlas_CtmAccessor
import net.minecraft.client.render.texture.TextureAtlas
import net.minecraft.client.render.texture.TextureAtlasSprite

class AtlasSpriteIndex private constructor(private val buckets: Map<Long, List<TextureAtlasSprite>>) {
    fun find(u: Float, v: Float): TextureAtlasSprite? {
        val bx = bucketOf(u)
        val by = bucketOf(v)
        for (dx in -1..1) {
            for (dy in -1..1) {
                val key = bucketKey(bx + dx, by + dy)
                val candidates = buckets[key] ?: continue
                for (sprite in candidates) {
                    if (u >= sprite.uMin - EPSILON && u <= sprite.uMax + EPSILON &&
                        v >= sprite.vMin - EPSILON && v <= sprite.vMax + EPSILON
                    ) {
                        return sprite
                    }
                }
            }
        }
        return null
    }

    companion object {
        private const val GRID = 128
        private const val EPSILON = 1.0e-5f

        private fun bucketOf(coord: Float): Int = (coord * GRID).toInt()
        private fun bucketKey(bx: Int, by: Int): Long = (bx.toLong() shl 32) or (by.toLong() and 0xFFFFFFFFL)

        fun build(atlas: TextureAtlas): AtlasSpriteIndex {
            val sprites = (atlas as MixinTextureAtlas_CtmAccessor).ctmStitchedSprites.values
            val buckets = HashMap<Long, MutableList<TextureAtlasSprite>>()
            for (sprite in sprites) {
                val minBx = bucketOf(sprite.uMin)
                val maxBx = bucketOf(sprite.uMax)
                val minBy = bucketOf(sprite.vMin)
                val maxBy = bucketOf(sprite.vMax)
                for (bx in minBx..maxBx) {
                    for (by in minBy..maxBy) {
                        buckets.getOrPut(bucketKey(bx, by)) { ArrayList() }.add(sprite)
                    }
                }
            }
            return AtlasSpriteIndex(buckets)
        }
    }
}
