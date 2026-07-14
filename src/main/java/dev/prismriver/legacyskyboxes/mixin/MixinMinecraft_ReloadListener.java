package dev.prismriver.legacyskyboxes.mixin;

import dev.prismriver.legacyskyboxes.LegacySkyboxes;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class MixinMinecraft_ReloadListener {
    @Inject(method = "init", at = @At("RETURN"))
    void impl$init(CallbackInfo ci) {
        Minecraft self = (Minecraft) (Object) this;
        LegacySkyboxes.registerResourceReloadListener(self.getResourceManager());
    }
}
