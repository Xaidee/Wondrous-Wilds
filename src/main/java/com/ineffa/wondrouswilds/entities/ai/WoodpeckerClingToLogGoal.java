package com.ineffa.wondrouswilds.entities.ai;

import com.ineffa.wondrouswilds.entities.WoodpeckerEntity;
import com.ineffa.wondrouswilds.registry.WondrousWildsTags;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.goal.MoveToBlockGoal;
import net.minecraft.world.level.LevelReader;

public class WoodpeckerClingToLogGoal extends MoveToBlockGoal {

    private final WoodpeckerEntity woodpecker;

    private boolean shouldStop = false;
    private int ticksUnableToReach;
    private int ticksTryingToReach;

    public WoodpeckerClingToLogGoal(WoodpeckerEntity woodpecker, double speed, int range, int maxYDifference) {
        super(woodpecker, speed, range, maxYDifference);

        this.woodpecker = woodpecker;
    }

    @Override
    public boolean canUse() {
        return this.woodpecker.getRandom().nextInt(200) == 0 && this.woodpecker.canWander() && this.findNearestBlock();
    }

    @Override
    public void start() {
        super.start();

        this.shouldStop = false;
        this.ticksUnableToReach = 0;
        this.ticksTryingToReach = 0;

        if (!this.woodpecker.isFlying()) this.woodpecker.setFlying(true);
    }

    @Override
    public boolean canContinueToUse() {
        return !this.shouldStop && this.woodpecker.canWander() && this.isValidTarget(this.woodpecker.getLevel(), this.blockPos);
    }

    @Override
    public void stop() {
        super.stop();

        if (this.isReachedTarget()) this.woodpecker.tryClingingTo(this.getMoveToTarget());
    }

    @Override
    public void tick() {
        super.tick();

        if (this.isReachedTarget()) {
            this.shouldStop = true;
            this.ticksUnableToReach = 0;
            return;
        }

        if (this.woodpecker.getNavigation().isDone()) {
            if (this.ticksUnableToReach >= 100) {
                if (this.woodpecker.isFlying()) {
                    this.woodpecker.setFlying(true);
                    this.ticksUnableToReach = 0;
                }
                else this.shouldStop = true;

                return;
            }
            ++this.ticksUnableToReach;
        }
        else this.ticksUnableToReach = 0;

        if (this.ticksTryingToReach >= 400) {
            this.shouldStop = true;
            return;
        }
        ++this.ticksTryingToReach;
    }

    @Override
    protected boolean isValidTarget(LevelReader world, BlockPos pos) {
        return !world.getBlockState(pos).is(WondrousWildsTags.Blocks.WOODPECKERS_INTERACT_WITH) && this.woodpecker.canClingToPos(pos, true, null);
    }

    @Override
    protected BlockPos getMoveToTarget() {
        return this.blockPos;
    }

    @Override
    public double acceptedDistance() {
        return 1.5D;
    }
}
