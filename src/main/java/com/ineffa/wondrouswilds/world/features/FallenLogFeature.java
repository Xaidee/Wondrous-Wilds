package com.ineffa.wondrouswilds.world.features;

import com.ineffa.wondrouswilds.world.features.configs.FallenLogFeatureConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.TreeFeature;

import java.util.HashSet;
import java.util.Set;

public class FallenLogFeature extends Feature<FallenLogFeatureConfig> {
    private static final BlockState MOSS_CARPET_STATE = Blocks.MOSS_CARPET.defaultBlockState();

    public FallenLogFeature() {
        super(FallenLogFeatureConfig.CODEC);
    }

    @Override
    public boolean place(FeaturePlaceContext<FallenLogFeatureConfig> context) {
        RandomSource random = context.random();
        WorldGenLevel world = context.level();
        BlockPos origin = context.origin();
        FallenLogFeatureConfig config = context.config();
        int minLength = config.minLength;
        int maxLength = config.maxLength;

        Set<BlockPos> logs = new HashSet<>();

        int logLimit = random.nextInt(minLength, maxLength);
        int nextPositiveOffsetDistance = 0;
        int nextNegativeOffsetDistance = 0;
        Direction nextOffsetDirection = Direction.from2DDataValue(random.nextInt(4));
        boolean oneDirectional = false;
        while (logs.size() < logLimit) {
            Direction.AxisDirection axisDirection = nextOffsetDirection.getAxisDirection();

            BlockPos tryLogPos = origin.relative(nextOffsetDirection, axisDirection == Direction.AxisDirection.POSITIVE ? nextPositiveOffsetDistance : nextNegativeOffsetDistance);

            if (!oneDirectional) nextOffsetDirection = nextOffsetDirection.getOpposite();

            if (!TreeFeature.validTreePos(world, tryLogPos) || !(world.isStateAtPosition(tryLogPos.below(), state -> state.isCollisionShapeFullBlock(world, tryLogPos.below())))) {
                if (oneDirectional) break;

                oneDirectional = true;
                continue;
            }

            if (axisDirection == Direction.AxisDirection.POSITIVE) ++nextPositiveOffsetDistance;
            else if (axisDirection == Direction.AxisDirection.NEGATIVE) ++nextNegativeOffsetDistance;

            logs.add(tryLogPos);
        }

        if (logs.size() < minLength) return false;

        for (BlockPos pos : logs) {
            BlockState state = config.logProvider.getState(random, pos);
            if (state.getBlock() instanceof RotatedPillarBlock) state = state.setValue(RotatedPillarBlock.AXIS, nextOffsetDirection.getAxis());

            this.setBlock(world, pos, state);

            if (random.nextInt(3) == 0) {
                BlockPos tryMossCarpetPos = pos.above();
                if (world.isEmptyBlock(tryMossCarpetPos)) this.setBlock(world, tryMossCarpetPos, MOSS_CARPET_STATE);
            }
        }

        return true;
    }
}
