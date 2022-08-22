package com.ineffa.wondrouswilds.client.rendering.entity;

import com.ineffa.wondrouswilds.WondrousWilds;
import com.ineffa.wondrouswilds.entities.WoodpeckerEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.model.AnimatedGeoModel;
import software.bernie.geckolib3.resource.GeckoLibCache;
import software.bernie.shadowed.eliotlash.molang.MolangParser;

public class WoodpeckerModel extends AnimatedGeoModel<WoodpeckerEntity> {

    @Override
    public ResourceLocation getModelResource(WoodpeckerEntity entity) {
        return new ResourceLocation(WondrousWilds.MOD_ID, "geo/woodpecker.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(WoodpeckerEntity entity) {
        return new ResourceLocation(WondrousWilds.MOD_ID, "textures/entity/woodpecker.png");
    }

    @Override
    public ResourceLocation getAnimationResource(WoodpeckerEntity entity) {
        return new ResourceLocation(WondrousWilds.MOD_ID, "animations/woodpecker.animation.json");
    }

    @Override
    public void setMolangQueries(IAnimatable animatable, double currentTick) {
        super.setMolangQueries(animatable, currentTick);

        MolangParser parser = GeckoLibCache.getInstance().parser;

        WoodpeckerEntity entity = (WoodpeckerEntity) animatable;
        boolean flying = entity.isFlying();

        float delta = Minecraft.getInstance().getDeltaFrameTime();

        float headPitch = Mth.lerp(delta, entity.yRotO, entity.getYRot());
        float f = Mth.rotLerp(delta, entity.yBodyRotO, entity.yBodyRot);
        float f1 = Mth.rotLerp(delta, entity.yRotO, entity.getYRot());
        float headYaw = f1 - f;

        parser.setValue("query.head_pitch", headPitch);
        parser.setValue("query.head_yaw", headYaw);

        float swing = entity.animationPosition - entity.animationSpeed * (1.0F - delta);
        float swingAmount = Mth.lerp(delta, entity.animationSpeedOld, entity.animationPosition);
        float extraSwing = 0.0F;
        if (!flying) extraSwing = 0.5F * Mth.clamp(swingAmount * 10.0F, 0.0F, 1.0F);

        parser.setValue("query.swing", swing * 0.15D);
        parser.setValue("query.swing_amount", Mth.clamp(swingAmount + extraSwing, 0.0D, flying ? 1.0D : 1.25D));

        float flapAngle = Mth.lerp(delta, entity.prevFlapAngle, entity.flapAngle);

        parser.setValue("query.flap_angle", flapAngle);
    }
}
