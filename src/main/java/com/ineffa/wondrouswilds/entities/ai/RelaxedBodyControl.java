package com.ineffa.wondrouswilds.entities.ai;

import com.ineffa.wondrouswilds.entities.FireflyEntity;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.control.BodyRotationControl;

public class RelaxedBodyControl extends BodyRotationControl {

    private final Mob entity;
    private static final float BODY_KEEP_UP_THRESHOLD = 15.0F;
    private float lastHeadYaw;

    public RelaxedBodyControl(Mob entity) {
        super(entity);
        this.entity = entity;
    }

    @Override
    public void clientTick() {
        if (this.isMoving()) {
            this.entity.yBodyRot = this.entity.getYRot();
            this.keepUpHead();
            this.lastHeadYaw = this.entity.yHeadRot;
            return;
        }
        if (this.isIndependent()) {
            if (Math.abs(this.entity.yHeadRot - this.lastHeadYaw) > BODY_KEEP_UP_THRESHOLD) {
                this.lastHeadYaw = this.entity.yHeadRot;
                this.keepUpBody();
            }
        }
    }

    private void keepUpBody() {
        this.entity.yBodyRot = Mth.clampedLerp(this.entity.yBodyRot, this.entity.yHeadRot, this.entity.getMaxHeadYRot());
    }

    private void keepUpHead() {
        this.entity.yHeadRot = Mth.clampedLerp(this.entity.yHeadRot, this.entity.yBodyRot, this.entity.getMaxHeadYRot());
    }

    private boolean isIndependent() {
        Entity firstPassenger = this.entity.getFirstPassenger();

        if (firstPassenger instanceof FireflyEntity) return true;

        return !(firstPassenger instanceof Mob);
    }

    private boolean isMoving() {
        double e;
        double d = this.entity.getX() - this.entity.xOld;
        return d * d + (e = this.entity.getZ() - this.entity.zOld) * e > 2.500000277905201E-7;
    }
}
