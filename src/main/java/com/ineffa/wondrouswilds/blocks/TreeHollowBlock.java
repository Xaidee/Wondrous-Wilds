package com.ineffa.wondrouswilds.blocks;

import com.ineffa.wondrouswilds.blocks.entity.TreeHollowBlockEntity;
import com.ineffa.wondrouswilds.registry.WondrousWildsBlocks;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("deprecation")
public class TreeHollowBlock extends InhabitableNestBlock {

    public TreeHollowBlock(Settings settings) {
        super(settings);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TreeHollowBlockEntity(pos, state);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        return this.defaultBlockState().setValue(FACING, ctx.getNearestLookingDirection().getOpposite());
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return world.isClient() ? null : checkType(type, WondrousWildsBlocks.BlockEntities.TREE_HOLLOW, TreeHollowBlockEntity::serverTick);
    }

    @Override
    public void playerWillDestroy(Level world, BlockPos pos, BlockState state, Player player) {
        if (!world.isClientSide && player.isCreative() && world.getGameRules().getBoolean(GameRules.RULE_DOBLOCKDROPS) && world.getBlockEntity(pos) instanceof TreeHollowBlockEntity treeHollow) {
            ItemStack itemStack = new ItemStack(this);
            if (treeHollow.hasInhabitants()) {
                CompoundTag nbtCompound = new CompoundTag();

                nbtCompound.put(TreeHollowBlockEntity.INHABITANTS_KEY, treeHollow.getInhabitantsNbt());
                BlockItem.setBlockEntityData(itemStack, WondrousWildsBlocks.BlockEntities.TREE_HOLLOW.get(), nbtCompound);

                itemStack.addTagElement("BlockStateTag", nbtCompound);

                ItemEntity itemEntity = new ItemEntity(world, pos.getX(), pos.getY(), pos.getZ(), itemStack);
                itemEntity.setDefaultPickUpDelay();
                world.addFreshEntity(itemEntity);
            }
        }

        super.playerWillDestroy(world, pos, state, player);
    }

    @Override
    public void playerDestroy(Level world, Player player, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity, ItemStack stack) {
        super.playerDestroy(world, player, pos, state, blockEntity, stack);

        if (!world.isClientSide && blockEntity instanceof TreeHollowBlockEntity treeHollow) {
            treeHollow.alertInhabitants(player, state, TreeHollowBlockEntity.InhabitantReleaseState.EMERGENCY);
            world.updateNeighbourForOutputSignal(pos, this);
        }
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(BlockState state, Level world, BlockPos pos) {
        BlockEntity blockEntity = world.getBlockEntity(pos);

        if (blockEntity instanceof TreeHollowBlockEntity treeHollow && treeHollow.hasInhabitants()) return 15;

        return 0;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }
}
