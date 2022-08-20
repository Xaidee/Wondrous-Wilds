package com.ineffa.wondrouswilds.entities.ai;

import com.ineffa.wondrouswilds.entities.FireflyEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.util.AirAndWaterRandomPos;
import net.minecraft.world.entity.ai.util.HoverRandomPos;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;

public class FireflyWanderFlyingGoal extends Goal {

    private final FireflyEntity firefly;

    public FireflyWanderFlyingGoal(FireflyEntity fireflyEntity) {
        this.setFlags(EnumSet.of(Goal.Flag.MOVE));

        this.firefly = fireflyEntity;
    }

    @Override
    public boolean canUse() {
        if (!this.firefly.isFlying() || !this.firefly.canWander()) return false;

        return this.firefly.getNavigation().isDone() && this.firefly.getRandom().nextInt(10) == 0;
    }

    @Override
    public boolean canContinueToUse() {
        return this.firefly.isFlying() && this.firefly.getNavigation().isInProgress();
    }

    @Override
    public void start() {
        Vec3 vec3 = this.getRandomLocation();
        if (vec3 != null) this.firefly.getNavigation().moveTo(this.firefly.getNavigation().createPath(new BlockPos(vec3), 1), 1.0D);
    }

    @Override
    public void stop() {
        if (!this.firefly.wantsToLand()) {
            if (this.firefly.getRandom().nextInt(10) == 0) this.firefly.setWantsToLand(true);
        }
        else this.firefly.setFlying(false);
    }

    @Nullable
    private Vec3 getRandomLocation() {
        Vec3 vec3d = this.firefly.getViewVector(0.0F);
        Vec3 vec3d3 = HoverRandomPos.getPos(this.firefly, 8, 8, vec3d.x, vec3d.z, 1.5707964F, this.firefly.wantsToLand() ? 1 : 6, 1);

        if (vec3d3 != null) return vec3d3;

        return AirAndWaterRandomPos.getPos(this.firefly, 16, 8, -2, vec3d.x, vec3d.z, 1.5707963705062866D);
    }
}
