package com.ineffa.wondrouswilds.entities.ai;

import com.ineffa.wondrouswilds.blocks.entity.InhabitableNestBlockEntity;
import com.ineffa.wondrouswilds.entities.FlyingAndWalkingAnimalEntity;
import com.ineffa.wondrouswilds.entities.TreeHollowNester;
import net.minecraft.entity.ai.goal.MoveToTargetPosGoal;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldView;

public class FindOrReturnToTreeHollowGoal extends MoveToBlockGoal {

    private final BlockNester nester;
    private final Mob nesterEntity;

    private int nextCheckDelay = 40;
    private boolean shouldStop = false;

    private boolean lookingForNest = false;

    public FindOrReturnToTreeHollowGoal(TreeHollowNester nester, double speed, int range, int maxYDifference) {
        super((PathAwareEntity) nester, speed, range, maxYDifference);

        this.nester = nester;
        this.nesterEntity = (Mob) nester;
    }

    @Override
    public boolean canUse() {
        if (this.nextCheckDelay > 0) {
            --this.nextCheckDelay;
            return false;
        }
        else this.nextCheckDelay = 40;

        if (!(this.nester.shouldReturnToNest() || (this.nester.shouldFindNest() && this.nester.getCannotInhabitNestTicks() <= 0))) return false;

        this.lookingForNest = this.nester.shouldFindNest();

        return this.findNearestBlock();
    }

    @Override
    public void start() {
        super.start();

        this.shouldStop = false;

        if (this.nesterEntity instanceof FlyingAndWalkingAnimalEntity flyingEntity && !flyingEntity.isFlying()) flyingEntity.setFlying(true);
    }

    @Override
    public boolean canContinueToUse() {
        return !this.shouldStop && this.isValidTarget(this.nesterEntity.getLevel(), this.blockPos);
    }

    @Override
    public void stop() {
        super.stop();

        this.nextCheckDelay = 40;

        if (!(this.nesterEntity.getLevel().getBlockEntity(this.getMoveToTarget()) instanceof TreeHollowBlockEntity treeHollow)) return;

        if (this.isReachedTarget()) {
            if (!treeHollow.tryAddingInhabitant(this.nester)) {
                this.nester.setCannotInhabitNestTicks(this.nester.getMinTicksOutOfNest());
                this.nester.clearNestPos();
            }
        }
    }

    @Override
    public void tick() {
        super.tick();

        if (this.isReachedTarget()) this.shouldStop = true;
    }

    @Override
    protected boolean isValidTarget(LevelReader world, BlockPos pos) {
        if (!this.lookingForNest) return true;

        return world.getBlockEntity(pos) instanceof InhabitableNestBlockEntity;
    }

    @Override
    protected BlockPos getMoveToTarget() {
        return this.blockPos;
    }

    @Override
    protected boolean findNearestBlock() {
        if (!this.lookingForNest) {
            this.blockPos = this.nester.getNestPos();
            return true;
        }

        return super.findNearestBlock();
    }

    @Override
    public double acceptedDistance() {
        return 1.5D;
    }
}
