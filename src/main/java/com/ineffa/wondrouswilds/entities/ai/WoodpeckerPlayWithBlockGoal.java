package com.ineffa.wondrouswilds.entities.ai;

import com.ineffa.wondrouswilds.entities.WoodpeckerEntity;
import com.ineffa.wondrouswilds.registry.WondrousWildsTags;
import net.minecraft.block.BlockState;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.MoveToTargetPosGoal;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;

import java.util.EnumSet;

public class WoodpeckerPlayWithBlockGoal extends MoveToTargetPosGoal {

    private final WoodpeckerEntity woodpecker;

    private boolean canClingToTarget = false;

    private boolean shouldStop = false;
    private int ticksUnableToReach;
    private int ticksTryingToReach;

    public WoodpeckerPlayWithBlockGoal(WoodpeckerEntity woodpecker, double speed, int range, int maxYDifference) {
        super(woodpecker, speed, range, maxYDifference);
        this.setControls(EnumSet.of(Goal.Control.MOVE, Goal.Control.JUMP, Control.LOOK));

        this.woodpecker = woodpecker;
    }

    @Override
    public boolean canStart() {
        return this.woodpecker.getRandom().nextInt(100) == 0 && this.woodpecker.canWander() && this.findTargetPos();
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
    public boolean shouldContinue() {
        return !this.shouldStop && this.woodpecker.canWander() && this.isTargetPos(this.woodpecker.getWorld(), this.targetPos);
    }

    @Override
    public void stop() {
        super.stop();

        if (this.canClingToTarget && this.hasReached()) this.woodpecker.tryClingingTo(this.getTargetPos());

        if (this.woodpecker.isPecking()) this.woodpecker.stopPecking(true);
    }

    @Override
    public void tick() {
        super.tick();

        World world = this.woodpecker.getWorld();
        BlockPos lookPos = this.getTargetPos();
        if (lookPos != null) {
            BlockState lookState = world.getBlockState(lookPos);
            if (lookState != null) {
                VoxelShape shape = lookState.getOutlineShape(world, lookPos);
                if (shape != null && !shape.isEmpty()) {
                    Box box = shape.getBoundingBox();
                    if (box != null) this.woodpecker.getLookControl().lookAt(box.getCenter().add(lookPos.getX(), lookPos.getY(), lookPos.getZ()));
                }
            }
        }

        if (this.hasReached()) {
            if (this.woodpecker.isFlying()) this.woodpecker.setVelocity(this.woodpecker.getVelocity().multiply(0.5D));

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
            if (this.woodpecker.getNavigation().isIdle()) {
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
    protected boolean isTargetPos(WorldView world, BlockPos pos) {
        if (!world.getBlockState(pos).isIn(WondrousWildsTags.Blocks.WOODPECKERS_INTERACT_WITH)) return false;

        this.canClingToTarget = this.woodpecker.canClingToPos(pos, true, null);

        return true;
    }

    @Override
    protected BlockPos getTargetPos() {
        return this.targetPos;
    }

    @Override
    public double getDesiredDistanceToTarget() {
        return this.canClingToTarget ? 1.5D : 1.0D;
    }
}
