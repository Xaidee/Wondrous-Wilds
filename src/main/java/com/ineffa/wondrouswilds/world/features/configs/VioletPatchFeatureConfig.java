package com.ineffa.wondrouswilds.world.features.configs;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;

public class VioletPatchFeatureConfig implements FeatureConfiguration {

    public final BlockStateProvider violetProvider;

    public static final Codec<VioletPatchFeatureConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            BlockStateProvider.CODEC.fieldOf("violet_provider").forGetter(config -> config.violetProvider)
    ).apply(instance, VioletPatchFeatureConfig::new));

    public VioletPatchFeatureConfig(BlockStateProvider violetProvider) {
        this.violetProvider = violetProvider;
    }
}
