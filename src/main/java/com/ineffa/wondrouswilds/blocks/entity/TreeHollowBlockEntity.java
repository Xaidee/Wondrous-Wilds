package com.ineffa.wondrouswilds.blocks.entity;

import com.google.common.collect.Lists;
import com.ineffa.wondrouswilds.blocks.TreeHollowBlock;
import com.ineffa.wondrouswilds.entities.FlyingAndWalkingAnimalEntity;
import com.ineffa.wondrouswilds.entities.TreeHollowNester;
import com.ineffa.wondrouswilds.registry.WondrousWildsBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import java.util.Iterator;
import java.util.List;

public class TreeHollowBlockEntity extends BlockEntity implements InhabitableNestBlockEntity {

    private final List<Inhabitant> inhabitants = Lists.newArrayList();

    public TreeHollowBlockEntity(BlockPos pos, BlockState state) {
        super(WondrousWildsBlocks.BlockEntities.TREE_HOLLOW.get(), pos, state);
    }

    public static void serverTick(World world, BlockPos pos, BlockState state, TreeHollowBlockEntity treeHollow) {
        tickInhabitants(world, pos, state, treeHollow.getInhabitants());
    }

    private static void tickInhabitants(World world, BlockPos pos, BlockState state, List<Inhabitant> inhabitants) {
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

        if (released) markDirty(world, pos, state);
    }

    @Override
    protected void saveAdditional(CompoundTag nbt) {
        super.saveAdditional(nbt);

        nbt.put(INHABITANTS_KEY, this.getInhabitantsNbt());
    }

    @Override
    public void load(CompoundTag nbt) {
        super.load(nbt);

        this.getInhabitants().clear();

        ListTag nbtList = nbt.getList(INHABITANTS_KEY, ListTag.TAG_COMPOUND);
        for (int i = 0; i < nbtList.size(); ++i) {
            CompoundTag nbtCompound = nbtList.getCompound(i);

            Inhabitant inhabitant = new Inhabitant(false, nbtCompound.getCompound(ENTITY_DATA_KEY), nbtCompound.getInt(CAPACITY_WEIGHT_KEY), nbtCompound.getInt(MIN_OCCUPATION_TICKS_KEY), nbtCompound.getInt(TICKS_IN_NEST_KEY));
            this.getInhabitants().add(inhabitant);
        }
    }

    @Override
    public List<Inhabitant> getInhabitants() {
        return this.inhabitants;
    }

    @Override
    public World getNestWorld() {
        return this.getWorld();
    }

    @Override
    public BlockPos getNestPos() {
        return this.getPos();
    }

    @Override
    public BlockState getNestCachedState() {
        return this.getCachedState();
    }

    @Override
    public void markNestDirty() {
        this.markDirty();
    }
}
