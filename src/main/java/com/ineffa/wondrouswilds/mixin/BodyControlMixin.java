package com.ineffa.wondrouswilds.mixin;

import com.ineffa.wondrouswilds.entities.FireflyEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.control.MoveControl;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MoveControl.class)
public class BodyControlMixin {

    @Shadow @Final protected Mob mob;

    @Inject(at = @At("HEAD"), method = "hasWanted", cancellable = true)
    private void stopFireflyFromControlling(CallbackInfoReturnable<Boolean> callback) {
        if (this.mob.getFirstPassenger() instanceof FireflyEntity) callback.setReturnValue(true);
    }
}
