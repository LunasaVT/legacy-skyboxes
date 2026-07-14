package dev.prismriver.legacyskyboxes.mixin;

import net.minecraft.client.resource.pack.CustomResourcePack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.io.File;

@Mixin(CustomResourcePack.class)
public interface MixinCustomResourcePack_CtmAccessor {
    @Accessor("file")
    File getCtmFile();
}
