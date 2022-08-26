package com.ineffa.wondrouswilds.blocks.entity;

import com.google.common.collect.Lists;
import com.ineffa.wondrouswilds.WondrousWilds;
import com.ineffa.wondrouswilds.registry.WondrousWildsBlocks;
import com.ineffa.wondrouswilds.screen.BirdhouseScreenHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Iterator;
import java.util.List;

public class BirdhouseBlockEntity extends RandomizableContainerBlockEntity implements InhabitableNestBlockEntity {

    private final List<Inhabitant> inhabitants = Lists.newArrayList();

    private NonNullList<ItemStack> inventory = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);

    public BirdhouseBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(WondrousWildsBlocks.BlockEntities.BIRDHOUSE.get(), blockPos, blockState);
    }

    public static void serverTick(Level world, BlockPos pos, BlockState state, BirdhouseBlockEntity birdhouse) {
        tickInhabitants(world, pos, state, birdhouse.getInhabitants());
    }

    private static void tickInhabitants(Level world, BlockPos pos, BlockState state, List<Inhabitant> inhabitants) {
        boolean released = false;

        Iterator<Inhabitant> iterator = inhabitants.iterator();
        while (iterator.hasNext()) {
            Inhabitant inhabitant = iterator.next();
            if (inhabitant.ticksInNest > inhabitant.minOccupationTicks) {
                if (InhabitableNestBlockEntity.tryReleasingInhabitant(world, pos, state, InhabitantReleaseState.RELEASE, inhabitant, null)) {
                    released = true;
                    iterator.remove();
                }
            }
            ++inhabitant.ticksInNest;
        }

        if (released) setChanged(world, pos, state);
    }

    @Override
    protected void saveAdditional(CompoundTag nbt) {
        super.saveAdditional(nbt);

        nbt.put(INHABITANTS_KEY, this.getInhabitantsNbt());

        if (!this.trySaveLootTable(nbt)) ContainerHelper.saveAllItems(nbt, this.inventory);
    }

    @Override
    public void load(CompoundTag nbt) {
        super.load(nbt);

        this.getInhabitants().clear();

        ListTag nbtList = nbt.getList(INHABITANTS_KEY, 10);
        for (int i = 0; i < nbtList.size(); ++i) {
            CompoundTag nbtCompound = nbtList.getCompound(i);

            Inhabitant inhabitant = new Inhabitant(false, nbtCompound.getCompound(ENTITY_DATA_KEY), nbtCompound.getInt(CAPACITY_WEIGHT_KEY), nbtCompound.getInt(MIN_OCCUPATION_TICKS_KEY), nbtCompound.getInt(TICKS_IN_NEST_KEY));
            this.getInhabitants().add(inhabitant);
        }

        if (!this.tryLoadLootTable(nbt)) ContainerHelper.loadAllItems(nbt, this.inventory);
    }

    @Override
    protected NonNullList<ItemStack> getItems() {
        return this.inventory;
    }

    @Override
    protected void setItems(NonNullList<ItemStack> list) {
        this.inventory = list;
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("container." + WondrousWilds.MOD_ID + ".birdhouse");
    }

    @Override
    protected AbstractContainerMenu createMenu(int syncId, Inventory playerInventory) {
        return new BirdhouseScreenHandler(syncId, playerInventory, this);
    }

    @Override
    public int getContainerSize() {
        return 1;
    }

    @Override
    public List<Inhabitant> getInhabitants() {
        return this.inhabitants;
    }

    @Override
    public Level getNestWorld() {
        return this.getLevel();
    }

    @Override
    public BlockPos getNestPos() {
        return this.getBlockPos();
    }

    @Override
    public BlockState getNestCachedState() {
        return this.getBlockState();
    }

    @Override
    public void markNestDirty() {
        this.setChanged();
    }
}
