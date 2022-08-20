package com.ineffa.wondrouswilds.entities.ai;

import com.ineffa.wondrouswilds.entities.WoodpeckerEntity;
import com.ineffa.wondrouswilds.registry.WondrousWildsTags;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.MoveToBlockGoal;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.EnumSet;

public class WoodpeckerPlayWithBlockGoal extends MoveToBlockGoal {

    private final WoodpeckerEntity woodpecker;

    private boolean canClingToTarget = false;

    private boolean shouldStop = false;
    private int ticksUnableToReach;
    private int ticksTryingToReach;

    public WoodpeckerPlayWithBlockGoal(WoodpeckerEntity woodpecker, double speed, int range, int maxYDifference) {
        super(woodpecker, speed, range, maxYDifference);
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.JUMP, Flag.LOOK));

        this.woodpecker = woodpecker;
    }

    @Override
    public boolean canUse() {
        return this.woodpecker.getRandom().nextInt(100) == 0 && this.woodpecker.canWander() && this.findNearestBlock();
    }

    @Override
    public void start() {
        super.start();

        this.shouldStop = false;
        this.ticksUnableToReach = 0;
        this.ticksTryingToReach = 0;

        if (this.canClingToTarget && !this.woodpecker.isFlying()) this.woodpecker.setFlying(true);
    }

    @Override
    public boolean canContinueToUse() {
        return !this.shouldStop && this.woodpecker.canWander() && this.isValidTarget(this.woodpecker.getLevel(), this.blockPos);
    }

    @Override
    public void stop() {
        super.stop();

        if (this.canClingToTarget && this.isReachedTarget()) this.woodpecker.tryClingingTo(this.getMoveToTarget());

        if (this.woodpecker.isPecking()) this.woodpecker.stopPecking(true);
    }

    @Override
    public void tick() {
        super.tick();

        Level world = this.woodpecker.getLevel();
        BlockPos lookPos = this.getMoveToTarget();
        if (lookPos != null) {
            BlockState lookState = world.getBlockState(lookPos);
            if (lookState != null) {
                VoxelShape shape = lookState.getInteractionShape(world, lookPos);
                if (shape != null && !shape.isEmpty()) {
                    AABB box = shape.bounds();
                    if (box != null) this.woodpecker.getLookControl().setLookAt(box.getCenter().add(lookPos.getX(), lookPos.getY(), lookPos.getZ()));
                }
            }
        }

        if (this.isReachedTarget()) {
            if (this.woodpecker.isFlying()) this.woodpecker.setDeltaMovement(this.woodpecker.getDeltaMovement().scale(0.5D));

            if (this.canClingToTarget) {
                this.shouldStop = true;
                return;
            }

            if (!this.woodpecker.isPecking()) {
                if (this.woodpecker.getRandom().nextInt(200) == 0) {
                    this.shouldStop = true;

                    if (!this.woodpecker.isTame() && this.woodpecker.getConsecutivePecks() > 0) this.woodpecker.progressTame();

                    return;
                }

                if (this.woodpecker.getRandom().nextInt(40) == 0) this.woodpecker.startPeckChain(1 + this.woodpecker.getRandom().nextInt(4));
            }

            this.ticksUnableToReach = 0;
            this.ticksTryingToReach = 0;
        }
        else {
            if (this.woodpecker.getNavigation().isDone()) {
                if (this.ticksUnableToReach >= 100) {
                    if (!this.woodpecker.isFlying()) {
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
    }

    @Override
    protected boolean isValidTarget(LevelReader world, BlockPos pos) {
        if (!world.getBlockState(pos).is(WondrousWildsTags.Blocks.WOODPECKERS_INTERACT_WITH)) return false;

        this.canClingToTarget = this.woodpecker.canClingToPos(pos, true, null);

        return true;
    }

    @Override
    protected BlockPos getMoveToTarget() {
        return this.blockPos;
    }

    @Override
    public double acceptedDistance() {
        return this.canClingToTarget ? 1.5D : 1.0D;
    }
}
