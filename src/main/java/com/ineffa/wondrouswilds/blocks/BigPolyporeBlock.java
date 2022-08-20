package com.ineffa.wondrouswilds.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class BigPolyporeBlock extends LadderBlock {

    private static final VoxelShape NORTH_SHAPE = Shapes.box(1.0D, 7.0D, 4.0D, 15.0D, 9.0D, 16.0D);
    private static final VoxelShape SOUTH_SHAPE = Shapes.box(1.0D, 7.0D, 0.0D, 15.0D, 9.0D, 12.0D);
    private static final VoxelShape EAST_SHAPE = Shapes.box(0.0D, 7.0D, 1.0D, 12.0D, 9.0D, 15.0D);
    private static final VoxelShape WEST_SHAPE = Shapes.box(4.0D, 7.0D, 1.0D, 16.0D, 9.0D, 15.0D);

    public BigPolyporeBlock(Properties properties) {
        super(properties);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return switch (state.getValue(FACING)) {
            default -> NORTH_SHAPE;
            case SOUTH -> SOUTH_SHAPE;
            case EAST -> EAST_SHAPE;
            case WEST -> WEST_SHAPE;
        };
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean isPathfindable(BlockState state, BlockGetter world, BlockPos pos, PathComputationType type) {
        return false;
    }
}
