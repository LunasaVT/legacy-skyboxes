package dev.prismriver.legacyskyboxes.mixin;

import dev.prismriver.legacyskyboxes.LegacySkyboxes;
import net.minecraft.client.render.world.WorldRenderer;
import net.minecraft.resource.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldRenderer.class)
public class MixinWorldRenderer_CustomCelestial {
    @Inject(method = "renderSky(FI)V", at = @At("HEAD"))
    private void impl$prepareCelestial(float partialTick, int skyType, CallbackInfo ci) {
        LegacySkyboxes.prepareCelestial(partialTick);
    }

    @ModifyArg(
            method = "renderSky(FI)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/render/texture/TextureManager;bind(Lnet/minecraft/resource/Identifier;)V",
                    ordinal = 0))
    private Identifier impl$sun(Identifier location) {
        return LegacySkyboxes.resolveCelestialTexture(location);
    }

    @ModifyArg(
            method = "renderSky(FI)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/render/texture/TextureManager;bind(Lnet/minecraft/resource/Identifier;)V",
                    ordinal = 1))
    private Identifier impl$moon(Identifier location) {
        return LegacySkyboxes.resolveCelestialTexture(location);
    }
}
