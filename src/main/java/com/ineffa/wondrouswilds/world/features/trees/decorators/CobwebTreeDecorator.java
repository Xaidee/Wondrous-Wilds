package com.ineffa.wondrouswilds.world.features.trees.decorators;

import com.ineffa.wondrouswilds.registry.WondrousWildsFeatures;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecorator;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecoratorType;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static com.ineffa.wondrouswilds.util.WondrousWildsUtils.HORIZONTAL_DIRECTIONS;

public class CobwebTreeDecorator extends TreeDecorator {

    public static final CobwebTreeDecorator INSTANCE = new CobwebTreeDecorator();
    public static final Codec<CobwebTreeDecorator> CODEC = Codec.unit(() -> INSTANCE);

    @Override
    protected TreeDecoratorType<?> type() {
        return WondrousWildsFeatures.Trees.Decorators.COBWEB_TYPE.get();
    }

    @Override
    public void place(Context generator) {
        RandomSource random = generator.random();
        LevelSimulatedReader world = generator.level();

        List<BlockPos> horizontalLogs = generator.logs().stream().filter(pos -> world.isStateAtPosition(pos, state -> state.hasProperty(RotatedPillarBlock.AXIS) && state.getValue(RotatedPillarBlock.AXIS).isHorizontal())).collect(Collectors.toList());
        Collections.shuffle(horizontalLogs);

        for (BlockPos logPos : horizontalLogs) {
            BlockPos cobwebPos = logPos.below();
            if (generator.isAir(cobwebPos) && canSupportCobwebFromSide(world, cobwebPos)) {
                if (random.nextInt(30) != 0) continue;
                generator.setBlock(cobwebPos, Blocks.COBWEB.defaultBlockState());
            }
        }
    }

    private static boolean canSupportCobwebFromSide(LevelSimulatedReader world, BlockPos center) {
        boolean canSupport = false;
        for (Direction direction : HORIZONTAL_DIRECTIONS) {
            BlockPos checkPos = center.relative(direction);
            if (world.isStateAtPosition(checkPos, BlockBehaviour.BlockStateBase::canOcclude)) {
                canSupport = true;
                break;
            }
        }

        return canSupport;
    }
}
