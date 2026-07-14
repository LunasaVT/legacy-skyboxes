package dev.prismriver.legacyskyboxes.mixin;

import dev.prismriver.legacyskyboxes.ctm.CtmLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.render.texture.TextureAtlas;
import net.minecraft.client.resource.manager.ResourceManager;
import net.minecraft.resource.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(TextureAtlas.class)
public class MixinTextureAtlas_CtmStitch {
    @Inject(method = "loadAndStitch", at = @At("HEAD"))
    void impl$beginCtmReload(ResourceManager resourceManager, CallbackInfo ci) {
        if (isNotBlocksAtlas()) return;
        CtmLoader.INSTANCE.beginReload((TextureAtlas) (Object) this, resourceManager);
    }

    @Inject(method = "loadAndStitch", at = @At("RETURN"))
    void impl$finishCtmReload(ResourceManager resourceManager, CallbackInfo ci) {
        if (isNotBlocksAtlas()) return;
        CtmLoader.INSTANCE.finishReload((TextureAtlas) (Object) this);
    }

    @Inject(method = "getResourceId", at = @At("HEAD"), cancellable = true)
    private void impl$resolveCtmTilePath(Identifier id, int mipLevel, CallbackInfoReturnable<Identifier> cir) {
        String path = id.getPath();
        if (path.startsWith("optifine/ctm/") || path.startsWith("mcpatcher/ctm/")) {
            cir.setReturnValue(new Identifier(id.getNamespace(), path + ".png"));
        }
    }

    @Unique
    private boolean isNotBlocksAtlas() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.getTextureManager() == null) {
            return true;
        }
        Object bound = mc.getTextureManager().get(TextureAtlas.BLOCKS_LOCATION);
        return bound != this;
    }
}
