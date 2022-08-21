package com.ineffa.wondrouswilds.mixin;

import net.minecraft.data.worldgen.features.TreeFeatures;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(TreeFeatures.class)
public interface TreeConfiguredFeaturesInvoker {

    @Invoker("createSuperBirch")
    static TreeConfiguration.TreeConfigurationBuilder tallBirchConfig() {
        throw new AssertionError();
    }
}
