package com.ineffa.wondrouswilds.util;

import com.google.common.collect.ImmutableMap;
import com.ineffa.wondrouswilds.registry.WondrousWildsBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class WondrousWildsUtils {

    public static final Direction[] HORIZONTAL_DIRECTIONS = Arrays.stream(Direction.values()).filter((direction) -> direction.getAxis().isHorizontal()).toArray(Direction[]::new);

    public static final Map<Block, Block> TREE_HOLLOW_MAP = new ImmutableMap.Builder<Block, Block>()
            .put(Blocks.BIRCH_LOG, WondrousWildsBlocks.BIRCH_TREE_HOLLOW.get())
            .build();

    public static Set<BlockPos> getCenteredCuboid(BlockPos center, int horizontalRadius) {
        return getCenteredCuboid(center, horizontalRadius, 0);
    }

    public static Set<BlockPos> getCenteredCuboid(BlockPos center, int horizontalRadius, int verticalRadius) {
        Set<BlockPos> positions = new HashSet<>();

        for (int y = -verticalRadius; y <= verticalRadius; ++y) {
            for (int x = -horizontalRadius; x <= horizontalRadius; ++x) {
                for (int z = -horizontalRadius; z <= horizontalRadius; ++z) {
                    BlockPos pos = center.offset(x, y, z);
                    positions.add(pos);
                }
            }
        }

        return positions;
    }

    public static Set<BlockPos> getEdges(BlockPos center, int edgeDistance, int edgeRadius) {
        Set<BlockPos> positions = new HashSet<>();

        for (Direction direction : HORIZONTAL_DIRECTIONS) {
            BlockPos offsetPos = center.relative(direction, edgeDistance);

            positions.add(offsetPos);

            if (edgeRadius < 1) continue;

            for (int distance = 1; distance <= edgeRadius; ++distance) {
                positions.add(offsetPos.relative(direction.getClockWise(), distance));
                positions.add(offsetPos.relative(direction.getCounterClockWise(), distance));
            }
        }

        return positions;
    }
}
