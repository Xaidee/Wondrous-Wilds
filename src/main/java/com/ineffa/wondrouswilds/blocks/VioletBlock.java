package com.ineffa.wondrouswilds.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FlowerBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class VioletBlock extends FlowerBlock {

    private static final VoxelShape SINGLE_VIOLET_SHAPE = Shapes.box(5.0D, 0.0D, 5.0D, 11.0D, 5.0D, 11.0D);
    private static final VoxelShape DOUBLE_VIOLET_SHAPE = Shapes.box(2.0D, 0.0D, 2.0D, 14.0D, 5.0D, 14.0D);
    private static final VoxelShape TRIPLE_VIOLET_SHAPE = Shapes.box(1.0D, 0.0D, 1.0D, 15.0D, 5.0D, 15.0D);
    private static final VoxelShape QUADRUPLE_VIOLET_SHAPE = Shapes.box(0.0D, 0.0D, 0.0D, 16.0D, 5.0D, 16.0D);

    public static final int MIN_VIOLETS = 1;
    public static final int MAX_VIOLETS = 4;

    public static final IntegerProperty VIOLETS = IntegerProperty.create("violets", MIN_VIOLETS, MAX_VIOLETS);

    public VioletBlock(Properties properties) {
        super(MobEffects.REGENERATION, 8, properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(VIOLETS, MIN_VIOLETS));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(VIOLETS);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        BlockState blockState = ctx.getLevel().getBlockState(ctx.getClickedPos());
        if (blockState.is(this)) return blockState.setValue(VIOLETS, Math.min(MAX_VIOLETS, blockState.getValue(VIOLETS) + 1));

        return super.getStateForPlacement(ctx);
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean canBeReplaced(BlockState state, BlockPlaceContext context) {
        if (!context.isSecondaryUseActive() && context.getItemInHand().is(this.asItem()) && state.getValue(VIOLETS) < MAX_VIOLETS) return true;

        return super.canBeReplaced(state, context);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return switch (state.getValue(VIOLETS)) {
            default -> SINGLE_VIOLET_SHAPE;
            case 2 -> DOUBLE_VIOLET_SHAPE;
            case 3 -> TRIPLE_VIOLET_SHAPE;
            case 4 -> QUADRUPLE_VIOLET_SHAPE;
        };
    }
}
