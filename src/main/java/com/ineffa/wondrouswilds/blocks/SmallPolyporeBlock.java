package com.ineffa.wondrouswilds.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class SmallPolyporeBlock extends LadderBlock {

    private static final VoxelShape NORTH_SINGLE_SHAPE = Shapes.box(3.0D, 7.0D, 8.0D, 13.0D, 9.0D, 16.0D);
    private static final VoxelShape NORTH_DUAL_SHAPE = Shapes.box(1.0D, 6.0D, 8.0D, 15.0D, 10.0D, 16.0D);
    private static final VoxelShape NORTH_TRIPLE_SHAPE = Shapes.box(1.0D, 4.0D, 8.0D, 15.0D, 12.0D, 16.0D);

    private static final VoxelShape SOUTH_SINGLE_SHAPE = Shapes.box(3.0D, 7.0D, 0.0D, 13.0D, 9.0D, 8.0D);
    private static final VoxelShape SOUTH_DUAL_SHAPE = Shapes.box(1.0D, 6.0D, 0.0D, 15.0D, 10.0D, 8.0D);
    private static final VoxelShape SOUTH_TRIPLE_SHAPE = Shapes.box(1.0D, 4.0D, 0.0D, 15.0D, 12.0D, 8.0D);

    private static final VoxelShape EAST_SINGLE_SHAPE = Shapes.box(0.0D, 7.0D, 3.0D, 8.0D, 9.0D, 13.0D);
    private static final VoxelShape EAST_DUAL_SHAPE = Shapes.box(0.0D, 6.0D, 1.0D, 8.0D, 10.0D, 15.0D);
    private static final VoxelShape EAST_TRIPLE_SHAPE = Shapes.box(0.0D, 4.0D, 1.0D, 8.0D, 12.0D, 15.0D);

    private static final VoxelShape WEST_SINGLE_SHAPE = Shapes.box(8.0D, 7.0D, 3.0D, 16.0D, 9.0D, 13.0D);
    private static final VoxelShape WEST_DUAL_SHAPE = Shapes.box(8.0D, 6.0D, 1.0D, 16.0D, 10.0D, 15.0D);
    private static final VoxelShape WEST_TRIPLE_SHAPE = Shapes.box(8.0D, 4.0D, 1.0D, 16.0D, 12.0D, 15.0D);

    public static final int MIN_POLYPORES = 1;
    public static final int MAX_POLYPORES = 3;

    public static final IntegerProperty POLYPORES = IntegerProperty.create("polypores", MIN_POLYPORES, MAX_POLYPORES);

    public SmallPolyporeBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(POLYPORES, MIN_POLYPORES).setValue(FACING, Direction.NORTH).setValue(WATERLOGGED, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(POLYPORES);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        BlockState blockState = ctx.getLevel().getBlockState(ctx.getClickedPos());
        if (blockState.is(this)) return blockState.setValue(POLYPORES, Math.min(MAX_POLYPORES, blockState.getValue(POLYPORES) + 1));

        return super.getStateForPlacement(ctx);
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean canBeReplaced(BlockState state, BlockPlaceContext context) {
        if (!context.isSecondaryUseActive() && context.getItemInHand().is(this.asItem()) && state.getValue(POLYPORES) < MAX_POLYPORES) return true;

        return super.canBeReplaced(state, context);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        int polypores = state.getValue(POLYPORES);

        return switch (state.getValue(FACING)) {
            default -> switch (polypores) {
                default -> NORTH_SINGLE_SHAPE;
                case 2 -> NORTH_DUAL_SHAPE;
                case 3 -> NORTH_TRIPLE_SHAPE;
            };
            case SOUTH -> switch (polypores) {
                default -> SOUTH_SINGLE_SHAPE;
                case 2 -> SOUTH_DUAL_SHAPE;
                case 3 -> SOUTH_TRIPLE_SHAPE;
            };
            case EAST -> switch (polypores) {
                default -> EAST_SINGLE_SHAPE;
                case 2 -> EAST_DUAL_SHAPE;
                case 3 -> EAST_TRIPLE_SHAPE;
            };
            case WEST -> switch (polypores) {
                default -> WEST_SINGLE_SHAPE;
                case 2 -> WEST_DUAL_SHAPE;
                case 3 -> WEST_TRIPLE_SHAPE;
            };
        };
    }
}
