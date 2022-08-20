package com.ineffa.wondrouswilds.entities.ai;

import com.ineffa.wondrouswilds.entities.WoodpeckerEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;

public class WoodpeckerAttackGoal extends MeleeAttackGoal {

    private final WoodpeckerEntity woodpecker;

    public WoodpeckerAttackGoal(WoodpeckerEntity woodpecker, double speed, boolean pauseWhenMobIdle) {
        super(woodpecker, speed, pauseWhenMobIdle);

        this.woodpecker = woodpecker;
    }

    @Override
    public void stop() {
        super.stop();

        if (this.woodpecker.isPecking()) this.woodpecker.stopPecking(true);
    }

    @Override
    protected void checkAndPerformAttack(LivingEntity target, double squaredDistance) {
        if (this.woodpecker.isPecking()) return;

        double maxDistance = this.getAttackReachSqr(target);
        if (squaredDistance <= maxDistance && this.isTimeToAttack()) {
            this.resetAttackCooldown();

            this.woodpecker.startPeckChain(1);
        }
    }
}
