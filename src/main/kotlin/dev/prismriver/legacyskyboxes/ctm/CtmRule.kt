package dev.prismriver.legacyskyboxes.ctm

import dev.prismriver.legacyskyboxes.sky.component.Biomes
import dev.prismriver.legacyskyboxes.sky.component.HeightRange
import net.minecraft.client.render.texture.TextureAtlasSprite
import net.minecraft.resource.Identifier
import net.minecraft.util.math.Direction

data class BlockMatcher(
    val namespace: String,
    val path: String,
    val properties: Map<String, Set<String>>,
) {
    val identifier: Identifier get() = Identifier(namespace, path)
}

data class CtmRule(
    val sourceId: Identifier,
    val fileName: String,
    val weight: Int,
    val method: CtmMethod,
    val tiles: List<TileRef>,
    val matchTiles: List<Identifier>,
    val matchBlocks: List<BlockMatcher>,
    val connect: ConnectMode?,
    val faces: Set<Direction>,
    val biomes: Biomes,
    val heights: List<HeightRange>,
    val name: String?,
    // ctm / ctm_compact
    val innerSeams: Boolean,
    val ctmOverrides: Map<Int, Int>,
    // random
    val weights: List<Int>,
    val randomLoops: Int,
    val symmetry: Symmetry,
    val linked: Boolean,
    // repeat
    val repeatWidth: Int,
    val repeatHeight: Int,
    // overlay
    val connectTiles: List<Identifier>,
    val connectBlocks: List<BlockMatcher>,
    val tintIndex: Int,
    val tintBlock: BlockMatcher?,
    val layer: OverlayLayer,
)

class ResolvedTile(val sprite: TextureAtlasSprite?, val isSkip: Boolean)

class ResolvedCtmRule(val rule: CtmRule, val resolvedTiles: List<ResolvedTile>)
