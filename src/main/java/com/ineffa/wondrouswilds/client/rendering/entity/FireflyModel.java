package com.ineffa.wondrouswilds.client.rendering.entity;

import com.ineffa.wondrouswilds.WondrousWilds;
import com.ineffa.wondrouswilds.entities.FireflyEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.model.AnimatedGeoModel;
import software.bernie.geckolib3.resource.GeckoLibCache;
import software.bernie.shadowed.eliotlash.molang.MolangParser;

public class FireflyModel extends AnimatedGeoModel<FireflyEntity> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(WondrousWilds.MOD_ID, "textures/entity/firefly/firefly.png");

    @Override
    public ResourceLocation getModelResource(FireflyEntity entity) {
        return new ResourceLocation(WondrousWilds.MOD_ID, "geo/firefly.geo.json");
    }

    @Override
    public ResourceLocation getAnimationResource(FireflyEntity entity) {
        return new ResourceLocation(WondrousWilds.MOD_ID, "animations/firefly.animation.json");
    }

    @Override
    public ResourceLocation getTextureResource(FireflyEntity entity) {
        return TEXTURE;
    }

    @Override
    public void setMolangQueries(IAnimatable animatable, double currentTick) {
        super.setMolangQueries(animatable, currentTick);

        MolangParser parser = GeckoLibCache.getInstance().parser;

        FireflyEntity fireflyEntity = (FireflyEntity) animatable;

        float delta = Minecraft.getInstance().getDeltaFrameTime();
        float limbSwing = fireflyEntity.animationPosition - fireflyEntity.animationSpeed * (1.0F - delta);
        float limbSwingAmount = Mth.lerp(delta, fireflyEntity.animationSpeedOld, fireflyEntity.animationPosition);

        parser.setValue("query.limb_swing", limbSwing * 1.25D);
        parser.setValue("query.limb_swing_amount", limbSwingAmount + (0.9F * Mth.clamp(limbSwingAmount * 10.0F, 0.0F, 1.0F)));
    }
}
