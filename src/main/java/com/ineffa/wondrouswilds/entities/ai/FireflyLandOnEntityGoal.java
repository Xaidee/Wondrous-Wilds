package com.ineffa.wondrouswilds.entities.ai;

import com.ineffa.wondrouswilds.entities.FireflyEntity;
import net.minecraft.network.protocol.game.ClientboundSetPassengersPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.EnumSet;

public class FireflyLandOnEntityGoal extends Goal {

    private final FireflyEntity firefly;
    private final Level world;
    private final Class<? extends LivingEntity> classToTarget;

    private LivingEntity entityToLandOn;

    private final TargetingConditions LAND_ON_PREDICATE = TargetingConditions.forNonCombat().range(8.0D).selector(this::canEntityBeLandedOn);

    public FireflyLandOnEntityGoal(FireflyEntity firefly, Class<? extends LivingEntity> classToTarget) {
        this.firefly = firefly;
        this.world = firefly.getLevel();
        this.classToTarget = classToTarget;

        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        if (!this.firefly.canSearchForEntityToLandOn()) return false;

        if (this.firefly.getRandom().nextInt(400) != 0) return false;

        return this.findEntityToLandOn();
    }

    @Override
    public void start() {
        if (!this.firefly.isFlying()) this.firefly.setFlying(true);
    }

    @Override
    public boolean canContinueToUse() {
        return this.firefly.canSearchForEntityToLandOn() && this.canEntityBeLandedOn(this.entityToLandOn) && this.firefly.hasLineOfSight(this.entityToLandOn) && this.firefly.distanceTo(this.entityToLandOn) <= 16.0F;
    }

    @Override
    public void stop() {
        super.stop();
    }

    @Override
    public void tick() {
        this.firefly.getNavigation().moveTo(this.entityToLandOn, 1.0D);

        if (this.firefly.distanceTo(this.entityToLandOn) <= 1.0F) {
            this.firefly.setLandOnEntityCooldown(2400);

            this.firefly.setFlying(false);

            this.firefly.startRiding(this.entityToLandOn);

            if (!this.world.isClientSide && this.entityToLandOn instanceof Player) {
                ServerLevel serverWorld = (ServerLevel) this.world;
                for (ServerPlayer player : serverWorld.players()) player.connection.send(new ClientboundSetPassengersPacket(this.entityToLandOn));
            }
        }
    }

    private boolean findEntityToLandOn() {
        return (this.entityToLandOn = this.world.getNearestEntity(this.classToTarget, LAND_ON_PREDICATE, this.firefly, this.firefly.getX(), this.firefly.getY(), this.firefly.getZ(), this.firefly.getBoundingBox().inflate(8.0D))) != null;
    }

    private boolean canEntityBeLandedOn(LivingEntity entity) {
        return entity.getClass() != this.firefly.getClass() && !entity.hasControllingPassenger();
    }
}
