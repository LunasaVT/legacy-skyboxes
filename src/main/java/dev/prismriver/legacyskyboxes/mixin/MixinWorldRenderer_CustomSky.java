package dev.prismriver.legacyskyboxes.mixin;

import dev.prismriver.legacyskyboxes.LegacySkyboxes;
import net.minecraft.client.render.world.WorldRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldRenderer.class)
public class MixinWorldRenderer_CustomSky {
    @Inject(method = "renderSky(FI)V", at = @At("RETURN"))
    void impl$renderSky(float partialTick, int skyType, CallbackInfo ci) {
        LegacySkyboxes.renderActiveSkyboxes(partialTick);
    }
}
