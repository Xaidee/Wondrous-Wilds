package com.ineffa.wondrouswilds.world.features.trees.decorators;

import com.ineffa.wondrouswilds.registry.WondrousWildsFeatures;
import com.mojang.serialization.Codec;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.block.BeehiveBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecorator;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecoratorType;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.*;
import java.util.stream.Collectors;

import static com.ineffa.wondrouswilds.util.WondrousWildsUtils.HORIZONTAL_DIRECTIONS;

public class HangingBeeNestTreeDecorator extends TreeDecorator {

    public static final HangingBeeNestTreeDecorator INSTANCE = new HangingBeeNestTreeDecorator();
    public static final Codec<HangingBeeNestTreeDecorator> CODEC = Codec.unit(() -> INSTANCE);

    @Override
    protected TreeDecoratorType<?> type() {
        return WondrousWildsFeatures.Trees.Decorators.HANGING_BEE_NEST_TYPE.get();
    }

    @Override
    public void place(Context generator) {
        LevelSimulatedReader world = generator.level();
        RandomSource random = generator.random();

        List<BlockPos> horizontalLogs = generator.logs().stream().filter(pos -> world.isStateAtPosition(pos, state -> state.hasProperty(RotatedPillarBlock.AXIS) && state.getValue(RotatedPillarBlock.AXIS).isHorizontal())).collect(Collectors.toList());
        Collections.shuffle(horizontalLogs);

        for (BlockPos logPos : horizontalLogs) {
            BlockPos nestPos = logPos.below();
            if (generator.isAir(nestPos)) {
                Set<Direction> suitableFacingDirections = new HashSet<>();
                for (Direction direction : HORIZONTAL_DIRECTIONS) if (generator.isAir(nestPos.relative(direction))) suitableFacingDirections.add(direction);

                Direction nestFacingDirection = Util.getRandom(!suitableFacingDirections.isEmpty() ? suitableFacingDirections.toArray(Direction[]::new) : HORIZONTAL_DIRECTIONS, random);

                generator.setBlock(nestPos, Blocks.BEE_NEST.defaultBlockState().setValue(BeehiveBlock.FACING, nestFacingDirection));
                world.getBlockEntity(nestPos, BlockEntityType.BEEHIVE).ifPresent(blockEntity -> {
                    int beeCount = 2 + random.nextInt(2);
                    for (int i = 0; i < beeCount; ++i) {
                        CompoundTag nbt = new CompoundTag();
                        nbt.putString("id", ForgeRegistries.ENTITY_TYPES.getKey(EntityType.BEE).toString());
                        blockEntity.storeBee(nbt, random.nextInt(599), false);
                    }
                });

                return;
            }
        }
    }
}
