package com.ineffa.wondrouswilds.mixin;

import com.ineffa.wondrouswilds.registry.WondrousWildsEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Blocks.class)
public class BlocksMixin {

    @Inject(at = @At("TAIL"), method = "ocelotOrParrot", cancellable = true)
    private static void canSpawnOnLeaves(BlockState state, BlockGetter world, BlockPos pos, EntityType<?> type, CallbackInfoReturnable<Boolean> callback) {
        if (type == WondrousWildsEntities.FIREFLY.get()) callback.setReturnValue(true);
    }
}
