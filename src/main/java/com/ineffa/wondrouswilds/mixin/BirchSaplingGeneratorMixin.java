package com.ineffa.wondrouswilds.mixin;

import com.ineffa.wondrouswilds.registry.WondrousWildsFeatures;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.grower.BirchTreeGrower;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraftforge.registries.RegistryObject;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BirchTreeGrower.class)
public class BirchSaplingGeneratorMixin {

    @Inject(at = @At("HEAD"), method = "getConfiguredFeature", cancellable = true)
    private void addFancyBirch(RandomSource random, boolean bees, CallbackInfoReturnable<RegistryObject<? extends ConfiguredFeature<?, ?>>> callback) {
        if (random.nextInt(10) == 0) callback.setReturnValue(bees && random.nextInt(20) == 0 ? WondrousWildsFeatures.Trees.FANCY_BIRCH_WITH_BEES_CONFIGURED : WondrousWildsFeatures.Trees.FANCY_BIRCH_CONFIGURED);
    }
}
