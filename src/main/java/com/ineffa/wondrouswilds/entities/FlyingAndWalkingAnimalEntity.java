package com.ineffa.wondrouswilds.entities;

import com.ineffa.wondrouswilds.entities.ai.BetterFlyNavigation;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.FlyingAnimal;
import net.minecraft.world.level.Level;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class FlyingAndWalkingAnimalEntity extends Animal implements FlyingAnimal {

    public static final String IS_FLYING_KEY = "IsFlying";
    public static final String WANTS_TO_LAND_KEY = "WantsToLand";

    private static final EntityDataAccessor<Boolean> IS_FLYING = SynchedEntityData.defineId(FlyingAndWalkingAnimalEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> WANTS_TO_LAND = SynchedEntityData.defineId(FlyingAndWalkingAnimalEntity.class, EntityDataSerializers.BOOLEAN);

    private final FlyingMoveControl airMoveControl;
    private final MoveControl landMoveControl;

    private final BetterFlyNavigation flyNavigation;
    private final GroundPathNavigation landNavigation;

    public FlyingAndWalkingAnimalEntity(EntityType<? extends FlyingAndWalkingAnimalEntity> entityType, Level world) {
        super(entityType, world);

        BetterFlyNavigation flyNavigation = new BetterFlyNavigation(this, world);
        flyNavigation.setCanOpenDoors(false);
        flyNavigation.setCanPassDoors(true);
        flyNavigation.setCanFloat(false);
        this.flyNavigation = flyNavigation;
        this.landNavigation = new GroundPathNavigation(this, world);

        this.airMoveControl = new FlyingMoveControl(this, 20, true);
        this.landMoveControl = new MoveControl(this);
    }

    @Override
    protected PathNavigation createNavigation(Level world) {
        if (this.isFlying()) {
            BetterFlyNavigation flyNavigation = new BetterFlyNavigation(this, world);
            flyNavigation.setCanOpenDoors(false);
            flyNavigation.setCanPassDoors(true);
            flyNavigation.setCanFloat(false);

            return flyNavigation;
        }

        return new GroundPathNavigation(this, world);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();

        this.entityData.define(IS_FLYING, false);
        this.entityData.define(WANTS_TO_LAND, false);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag nbt) {
        super.addAdditionalSaveData(nbt);

        nbt.putBoolean(IS_FLYING_KEY, this.isFlying());
        nbt.putBoolean(WANTS_TO_LAND_KEY, this.wantsToLand());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag nbt) {
        super.readAdditionalSaveData(nbt);

        boolean isFlying = nbt.getBoolean(IS_FLYING_KEY);
        if (this.isFlying() != isFlying) this.setFlying(isFlying);

        this.setWantsToLand(nbt.getBoolean(WANTS_TO_LAND_KEY));
    }

    public boolean isAiFlying() {
        return this.entityData.get(IS_FLYING);
    }

    public void setIsFlying(boolean isFlying) {
        this.entityData.set(IS_FLYING, isFlying);
    }

    public void setFlying(boolean flying) {
        this.setIsFlying(flying);

        if (!flying) {
            this.setNoGravity(false);
            this.setWantsToLand(false);
        }

        this.moveControl = flying ? this.airMoveControl : this.landMoveControl;
        this.navigation = flying ? this.flyNavigation : this.landNavigation;
    }

    public boolean wantsToLand() {
        return this.entityData.get(WANTS_TO_LAND);
    }

    public void setWantsToLand(boolean wantsToLand) {
        this.entityData.set(WANTS_TO_LAND, wantsToLand);
    }

    @Override
    public boolean isFlying() {
        return !this.onGround;
    }

    @Override
    protected boolean isFlapping() {
        return this.isAiFlying();
    }
}
