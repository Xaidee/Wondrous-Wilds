package com.ineffa.wondrouswilds.world.features.trees.decorators;

import com.ineffa.wondrouswilds.blocks.BigPolyporeBlock;
import com.ineffa.wondrouswilds.blocks.SmallPolyporeBlock;
import com.ineffa.wondrouswilds.registry.WondrousWildsBlocks;
import com.ineffa.wondrouswilds.registry.WondrousWildsFeatures;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecorator;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecoratorType;
import net.minecraft.world.level.material.Fluids;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static com.ineffa.wondrouswilds.util.WondrousWildsUtils.HORIZONTAL_DIRECTIONS;

public class PolyporeTreeDecorator extends TreeDecorator {

    public static final PolyporeTreeDecorator INSTANCE = new PolyporeTreeDecorator();
    public static final Codec<PolyporeTreeDecorator> CODEC = Codec.unit(() -> INSTANCE);

    private static final BlockState SMALL_POLYPORE_STATE = WondrousWildsBlocks.SMALL_POLYPORE.get().defaultBlockState();
    private static final BlockState BIG_POLYPORE_STATE = WondrousWildsBlocks.BIG_POLYPORE.get().defaultBlockState();

    @Override
    protected TreeDecoratorType<?> type() {
        return WondrousWildsFeatures.Trees.Decorators.POLYPORE_TYPE.get();
    }

    @Override
    public void place(Context generator) {
        RandomSource random = generator.random();
        LevelSimulatedReader world = generator.level();

        List<BlockPos> verticalLogs = generator.logs().stream().filter(pos -> world.isStateAtPosition(pos, state -> state.hasProperty(RotatedPillarBlock.AXIS) && state.getValue(RotatedPillarBlock.AXIS).isVertical()) && canPlacePolyporesAround(generator, world, pos)).collect(Collectors.toList());
        Collections.shuffle(verticalLogs);

        int clusterLimit = 0; while (clusterLimit < 3) if (random.nextBoolean()) ++clusterLimit; else break;
        int clustersPlaced = 0;
        for (BlockPos logPos : verticalLogs) {
            if (clustersPlaced >= clusterLimit) break;

            int steps = 1 + random.nextInt(3);
            Direction nextOffsetDirection = random.nextBoolean() ? Direction.UP : Direction.DOWN;
            int nextUpOffset = 0;
            int nextDownOffset = 0;
            for (int step = 0; step <= steps; ++step) {
                BlockPos polyporesCenter = logPos.relative(nextOffsetDirection, nextOffsetDirection == Direction.UP ? nextUpOffset : nextDownOffset);

                if (nextOffsetDirection == Direction.UP) ++nextUpOffset;
                else if (nextOffsetDirection == Direction.DOWN) ++nextDownOffset;
                nextOffsetDirection = nextOffsetDirection.getOpposite();

                if (!canPlacePolyporesAround(generator, world, polyporesCenter)) continue;

                for (Direction polyporeDirection : HORIZONTAL_DIRECTIONS) {
                    int polyporeScale = random.nextInt(5);
                    if (polyporeScale <= 0) continue;

                    BlockPos polyporePos = polyporesCenter.relative(polyporeDirection);
                    if (!isOpenSpace(generator, world, polyporePos)) continue;

                    generator.setBlock(polyporePos, polyporeScale > 3 ? BIG_POLYPORE_STATE.setValue(BigPolyporeBlock.FACING, polyporeDirection) : SMALL_POLYPORE_STATE.setValue(SmallPolyporeBlock.POLYPORES, polyporeScale).setValue(SmallPolyporeBlock.FACING, polyporeDirection));
                }
            }

            ++clustersPlaced;
        }
    }

    private static boolean canPlacePolyporesAround(Context generator, LevelSimulatedReader world, BlockPos center) {
        if (!world.isStateAtPosition(center, state -> state.is(BlockTags.LOGS))) return false;

        boolean hasOpenSpace = false;
        for (Direction direction : HORIZONTAL_DIRECTIONS) {
            BlockPos offsetPos = center.relative(direction);
            if (isOpenSpace(generator, world, offsetPos)) {
                hasOpenSpace = true;
                break;
            }
        }

        return hasOpenSpace;
    }

    private static boolean isOpenSpace(Context generator, LevelSimulatedReader world, BlockPos pos) {
        return generator.isAir(pos) || world.isFluidAtPosition(pos, state -> state.is(Fluids.WATER) && state.isSource());
    }
}
