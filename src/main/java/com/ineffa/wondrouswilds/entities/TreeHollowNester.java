package com.ineffa.wondrouswilds.entities;

import net.minecraft.core.BlockPos;

public interface TreeHollowNester {

    int getNestCapacityWeight();

    int getMinTicksInNest();

    int getMinTicksOutOfNest();

    int getCannotInhabitNestTicks();

    void setCannotInhabitNestTicks(int ticks);

    BlockPos getNestPos();

    void setNestPos(BlockPos pos);

    default void clearNestPos() {
        this.setNestPos(BlockPos.ZERO);
    }

    default boolean hasNestPos() {
        return !(this.getNestPos() == null || this.getNestPos() == BlockPos.ZERO);
    }

    default boolean shouldFindNest() {
        return !this.hasNestPos();
    }

    boolean shouldReturnToNest();

    boolean defendsNest();

    int getWanderRadiusFromNest();
}
