package com.ineffa.wondrouswilds.client.rendering.entity;

import com.ineffa.wondrouswilds.entities.WoodpeckerEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib3.core.processor.IBone;
import software.bernie.geckolib3.geo.render.built.GeoBone;
import software.bernie.geckolib3.renderers.geo.ExtendedGeoEntityRenderer;

public class WoodpeckerRenderer extends ExtendedGeoEntityRenderer<WoodpeckerEntity> {

    public static final String HELD_ITEM_BONE_NAME = "heldItem";

    public WoodpeckerRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, new WoodpeckerModel());

        this.shadowRadius = 0.225F;
    }

    @Override
    protected ItemStack getHeldItemForBone(String boneName, WoodpeckerEntity currentEntity) {
        if (boneName.equals(HELD_ITEM_BONE_NAME)) return mainHand;

        return null;
    }

    @Override
    protected ItemTransforms.TransformType getCameraTransformForItemAtBone(ItemStack boneItem, String boneName) {
        if (boneName.equals(HELD_ITEM_BONE_NAME)) return ItemTransforms.TransformType.THIRD_PERSON_RIGHT_HAND;

        return ItemTransforms.TransformType.NONE;
    }

    @Override
    protected void preRenderItem(PoseStack matrixStack, ItemStack item, String boneName, WoodpeckerEntity currentEntity, IBone bone) {
        if (item == this.mainHand) {
            matrixStack.mulPose(Vector3f.XP.rotationDegrees(-90.0F));
            matrixStack.translate(0.0D, 0.0D, -0.035D);
            matrixStack.scale(0.5F, 0.5F, 0.5F);
        }
    }

    @Override
    protected void postRenderItem(PoseStack matrixStack, ItemStack item, String boneName, WoodpeckerEntity currentEntity, IBone bone) {}

    @Override
    protected BlockState getHeldBlockForBone(String boneName, WoodpeckerEntity currentEntity) {
        return null;
    }

    @Override
    protected void preRenderBlock(PoseStack matrixStack, BlockState block, String boneName, WoodpeckerEntity currentEntity) {}

    @Override
    protected void postRenderBlock(PoseStack matrixStack, BlockState block, String boneName, WoodpeckerEntity currentEntity) {}

    @Override
    protected boolean isArmorBone(GeoBone bone) {
        return false;
    }

    @Nullable
    @Override
    protected ResourceLocation getTextureForBone(String boneName, WoodpeckerEntity currentEntity) {
        return null;
    }
}
