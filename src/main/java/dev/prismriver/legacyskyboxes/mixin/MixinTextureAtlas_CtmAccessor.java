package dev.prismriver.legacyskyboxes.mixin;

import net.minecraft.client.render.texture.TextureAtlas;
import net.minecraft.client.render.texture.TextureAtlasSprite;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(TextureAtlas.class)
public interface MixinTextureAtlas_CtmAccessor {
    @Accessor("stitchedSprites")
    Map<String, TextureAtlasSprite> getCtmStitchedSprites();
}
