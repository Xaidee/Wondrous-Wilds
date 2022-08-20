package com.ineffa.wondrouswilds.entities.ai;

import com.ineffa.wondrouswilds.entities.FireflyEntity;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;

public class FireflyWanderLandGoal extends RandomStrollGoal {

    private final FireflyEntity firefly;

    public FireflyWanderLandGoal(FireflyEntity fireflyEntity, double speed) {
        super(fireflyEntity, speed);

        this.firefly = fireflyEntity;
    }

    @Override
    public boolean canUse() {
        if (this.firefly.isFlying() || !this.firefly.canWander()) return false;

        return super.canUse();
    }

    @Override
    public boolean canContinueToUse() {
        return !this.firefly.isFlying() && super.canContinueToUse();
    }

    @Override
    public void stop() {
        super.stop();

        if (this.firefly.getRandom().nextBoolean()) this.firefly.setFlying(true);
    }
}
