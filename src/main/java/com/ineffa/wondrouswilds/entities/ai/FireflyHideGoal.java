package com.ineffa.wondrouswilds.entities.ai;

import com.ineffa.wondrouswilds.entities.FireflyEntity;
import com.ineffa.wondrouswilds.registry.WondrousWildsTags;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.goal.MoveToBlockGoal;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LevelEvent;

@MethodsReturnNonnullByDefault
public class FireflyHideGoal extends MoveToBlockGoal {

    public FireflyHideGoal(FireflyEntity mob, double speed, int range, int maxYDifference) {
        super(mob, speed, range, maxYDifference);
    }

    @Override
    public boolean canUse() {
        if (!((FireflyEntity) this.mob).shouldHide()) return false;

        return this.findNearestBlock();
    }

    @Override
    public boolean canContinueToUse() {
        return this.isValidTarget(this.mob.getLevel(), this.getMoveToTarget());
    }

    @Override
    public void tick() {
        if (this.isReachedTarget()) {
            BlockPos pos = this.getMoveToTarget();
            this.mob.getLevel().levelEvent(LevelEvent.PARTICLES_DESTROY_BLOCK, pos, Block.getId(this.mob.getLevel().getBlockState(pos)));

            this.mob.discard();
        }

        if (!((FireflyEntity) this.mob).isFlying()) {
            ((FireflyEntity) this.mob).setFlying(true);
        }

        super.tick();
    }

    @Override
    protected boolean isValidTarget(LevelReader world, BlockPos pos) {
        return world.getBlockState(pos).is(WondrousWildsTags.Blocks.FIREFLIES_HIDE_IN);
    }

    @Override
    protected BlockPos getMoveToTarget() {
        return this.blockPos;
    }
}
