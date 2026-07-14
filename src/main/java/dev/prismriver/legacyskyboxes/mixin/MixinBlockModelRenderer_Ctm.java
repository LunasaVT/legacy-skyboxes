package dev.prismriver.legacyskyboxes.mixin;

import dev.prismriver.legacyskyboxes.ctm.CtmQuadRewriter;
import net.minecraft.block.Block;
import net.minecraft.client.render.block.BlockModelRenderer;
import net.minecraft.client.resource.model.BakedQuad;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.WorldView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.List;

@Mixin(BlockModelRenderer.class)
public class MixinBlockModelRenderer_Ctm {
    @ModifyVariable(method = "tesselateFaceWithAmbientOcclusion", at = @At("HEAD"), ordinal = 0, argsOnly = true)
    private List<BakedQuad> impl$rewriteAoQuads(
        List<BakedQuad> quads,
        WorldView view,
        Block block,
        BlockPos pos
    ) {
        return CtmQuadRewriter.INSTANCE.rewrite(view, pos, null, quads);
    }

    @ModifyVariable(method = "tesselateFaceWithoutAmbientOcclusion", at = @At("HEAD"), ordinal = 0, argsOnly = true)
    private List<BakedQuad> impl$rewriteFlatQuads(
        List<BakedQuad> quads,
        WorldView view,
        Block block,
        BlockPos pos,
        Direction direction
    ) {
        return CtmQuadRewriter.INSTANCE.rewrite(view, pos, direction, quads);
    }
}
