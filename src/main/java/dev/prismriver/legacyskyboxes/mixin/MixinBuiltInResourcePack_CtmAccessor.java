package dev.prismriver.legacyskyboxes.mixin;

import net.minecraft.client.resource.pack.BuiltInResourcePack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.io.File;
import java.util.Map;

@Mixin(BuiltInResourcePack.class)
public interface MixinBuiltInResourcePack_CtmAccessor {
    @Accessor("assets")
    Map<String, File> getCtmAssets();
}
