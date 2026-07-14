package dev.prismriver.legacyskyboxes.mixin;

import dev.prismriver.legacyskyboxes.ctm.CtmDiscovery;
import net.minecraft.client.resource.manager.SimpleReloadableResourceManager;
import net.minecraft.client.resource.pack.ResourcePack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(SimpleReloadableResourceManager.class)
public class MixinSimpleReloadableResourceManager_CtmPacks {
    @Inject(method = "reload", at = @At("HEAD"))
    void impl$captureActivePacks(List<ResourcePack> resourcePacks, CallbackInfo ci) {
        CtmDiscovery.INSTANCE.setActivePacks(resourcePacks);
    }
}
