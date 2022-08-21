package com.ineffa.wondrouswilds.world.features.trees.decorators;

import com.ineffa.wondrouswilds.blocks.TreeHollowBlock;
import com.ineffa.wondrouswilds.registry.WondrousWildsBlocks;
import com.ineffa.wondrouswilds.registry.WondrousWildsEntities;
import com.ineffa.wondrouswilds.registry.WondrousWildsFeatures;
import com.mojang.serialization.Codec;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecorator;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecoratorType;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.ineffa.wondrouswilds.util.WondrousWildsUtils.HORIZONTAL_DIRECTIONS;
import static com.ineffa.wondrouswilds.util.WondrousWildsUtils.TREE_HOLLOW_MAP;

public class TreeHollowTreeDecorator extends TreeDecorator {

    public static final TreeHollowTreeDecorator INSTANCE = new TreeHollowTreeDecorator();
    public static final Codec<TreeHollowTreeDecorator> CODEC = Codec.unit(() -> INSTANCE);

    @Override
    protected TreeDecoratorType<?> type() {
        return WondrousWildsFeatures.Trees.Decorators.TREE_HOLLOW_TYPE.get();
    }

    @Override
    public void place(Context generator) {
        LevelSimulatedReader world = generator.level();
        RandomSource random = generator.random();

        List<BlockPos> suitableLogs = generator.logs().stream().filter(pos -> world.isStateAtPosition(pos, state -> TREE_HOLLOW_MAP.containsKey(state.getBlock()) && state.hasProperty(RotatedPillarBlock.AXIS) && state.getValue(RotatedPillarBlock.AXIS).isVertical()) && hasSpaceAround(generator, pos)).toList();

        if (suitableLogs.isEmpty()) return;

        BlockPos chosenLog = suitableLogs.get(random.nextInt(suitableLogs.size()));

        if (chosenLog == null) return;

        Set<Direction> suitableFacingDirections = new HashSet<>();
        for (Direction direction : HORIZONTAL_DIRECTIONS) if (generator.isAir(chosenLog.relative(direction))) suitableFacingDirections.add(direction);

        Direction facingDirection = Util.getRandom(!suitableFacingDirections.isEmpty() ? suitableFacingDirections.toArray(Direction[]::new) : HORIZONTAL_DIRECTIONS, random);

        world.isStateAtPosition(chosenLog, state -> {
            generator.setBlock(chosenLog, TREE_HOLLOW_MAP.get(state.getBlock()).defaultBlockState().setValue(TreeHollowBlock.FACING, facingDirection));
            world.getBlockEntity(chosenLog, WondrousWildsBlocks.BlockEntities.TREE_HOLLOW.get()).ifPresent(treeHollow -> {
                treeHollow.addFreshInhabitant(WondrousWildsEntities.WOODPECKER.get()); // TODO: Make configurable for use with other entities
            });

            return true;
        });
    }

    private static boolean hasSpaceAround(Context generator, BlockPos pos) {
        boolean hasOpenSpace = false;
        for (Direction direction : HORIZONTAL_DIRECTIONS) {
            BlockPos offsetPos = pos.relative(direction);
            if (generator.isAir(offsetPos)) {
                hasOpenSpace = true;
                break;
            }
        }

        return hasOpenSpace;
    }
}
