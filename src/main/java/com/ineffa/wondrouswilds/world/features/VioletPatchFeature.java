package com.ineffa.wondrouswilds.world.features;

import com.ineffa.wondrouswilds.blocks.VioletBlock;
import com.ineffa.wondrouswilds.world.features.configs.VioletPatchFeatureConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;

public class VioletPatchFeature extends Feature<VioletPatchFeatureConfig> {

    public VioletPatchFeature() {
        super(VioletPatchFeatureConfig.CODEC);
    }

    @Override
    public boolean place(FeaturePlaceContext<VioletPatchFeatureConfig> context) {
        VioletPatchFeatureConfig config = context.config();
        RandomSource random = context.random();
        WorldGenLevel world = context.level();
        BlockPos origin = context.origin();

        int horizontalSpread = 12 + 1;
        int verticalSpread = 2 + 1;

        int i = 0;
        BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();
        for (int l = 0; l < 280; ++l) {
            mutablePos.setWithOffset(origin, random.nextInt(horizontalSpread) - random.nextInt(horizontalSpread), random.nextInt(verticalSpread) - random.nextInt(verticalSpread), random.nextInt(horizontalSpread) - random.nextInt(horizontalSpread));

            if (world.isEmptyBlock(mutablePos)) {
                BlockState violetState = config.violetProvider.getState(random, mutablePos);

                if (!violetState.canSurvive(world, mutablePos)) continue;

                if (violetState.getBlock() instanceof VioletBlock) violetState = violetState.setValue(VioletBlock.VIOLETS, random.nextIntBetweenInclusive(VioletBlock.MIN_VIOLETS, VioletBlock.MAX_VIOLETS));
                else continue;

                world.setBlock(mutablePos, violetState, Block.UPDATE_IMMEDIATE);
            }

            ++i;
        }

        return i > 0;
    }
}
