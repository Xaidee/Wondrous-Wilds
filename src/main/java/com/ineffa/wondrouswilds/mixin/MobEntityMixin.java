package com.ineffa.wondrouswilds.mixin;

import com.ineffa.wondrouswilds.entities.FireflyEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Mob.class)
public abstract class MobEntityMixin extends Entity {

    @Shadow @Final public GoalSelector goalSelector;

    private MobEntityMixin(EntityType<? extends Mob> entityType, Level world) {
        super(entityType, world);
    }

    @Inject(at = @At("HEAD"), method = "updateControlFlags", cancellable = true)
    private void stopFireflyFromControlling(CallbackInfo callback) {
        boolean shouldNotBeControlled = !(this.getControllingPassenger() instanceof Mob) || this.getControllingPassenger() instanceof FireflyEntity;
        boolean isNotRidingBoat = !(this.getVehicle() instanceof Boat);

        this.goalSelector.setControlFlag(Goal.Flag.MOVE, shouldNotBeControlled);
        this.goalSelector.setControlFlag(Goal.Flag.JUMP, shouldNotBeControlled && isNotRidingBoat);
        this.goalSelector.setControlFlag(Goal.Flag.LOOK, shouldNotBeControlled);

        callback.cancel();
    }
}
