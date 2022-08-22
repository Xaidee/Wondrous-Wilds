package com.ineffa.wondrouswilds.world.features.configs;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;

public class FallenLogFeatureConfig implements FeatureConfiguration {

    public final BlockStateProvider logProvider;
    public final int minLength, maxLength;

    public static final Codec<FallenLogFeatureConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            BlockStateProvider.CODEC.fieldOf("log_provider").forGetter(config -> config.logProvider),
            Codec.INT.fieldOf("min_length").orElse(3).forGetter(config -> config.minLength),
            Codec.INT.fieldOf("max_length").orElse(8).forGetter(config -> config.maxLength)
    ).apply(instance, FallenLogFeatureConfig::new));

    public FallenLogFeatureConfig(BlockStateProvider logProvider, int minLength, int maxLength) {
        this.logProvider = logProvider;
        this.minLength = minLength;
        this.maxLength = maxLength;
    }
}
