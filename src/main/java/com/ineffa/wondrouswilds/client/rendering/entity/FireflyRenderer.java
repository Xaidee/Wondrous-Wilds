package com.ineffa.wondrouswilds.client.rendering.entity;

import com.ineffa.wondrouswilds.WondrousWilds;
import com.ineffa.wondrouswilds.client.rendering.WondrousWildsRenderTypes;
import com.ineffa.wondrouswilds.entities.FireflyEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import software.bernie.geckolib3.geo.render.built.GeoModel;
import software.bernie.geckolib3.model.provider.GeoModelProvider;
import software.bernie.geckolib3.renderers.geo.GeoEntityRenderer;
import software.bernie.geckolib3.renderers.geo.GeoLayerRenderer;
import software.bernie.geckolib3.renderers.geo.IGeoRenderer;

public class FireflyRenderer extends GeoEntityRenderer<FireflyEntity> {

    public FireflyRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, new FireflyModel());
        this.addLayer(new FireflyGlowLayer(this));
        this.shadowRadius = 0.2F;
    }

    @Override
    public RenderType getRenderType(FireflyEntity entity, float partialTicks, PoseStack stack, MultiBufferSource renderTypeBuffer, VertexConsumer vertexBuilder, int packedLightIn, ResourceLocation textureLocation) {
        return RenderType.entityTranslucent(this.getTextureLocation(entity));
    }

    private static class FireflyGlowLayer extends GeoLayerRenderer<FireflyEntity> {
        private static final ResourceLocation GLOW_TEXTURE = new ResourceLocation(WondrousWilds.MOD_ID, "textures/entity/firefly/firefly_glow.png");

        public FireflyGlowLayer(IGeoRenderer<FireflyEntity> renderer) {
            super(renderer);
        }

        @Override
        public void render(PoseStack matrixStack, MultiBufferSource vertexConsumerProvider, int packedLight, FireflyEntity entity, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
            this.renderModel(this.getEntityModel(), GLOW_TEXTURE, matrixStack, vertexConsumerProvider, packedLight, entity, partialTicks, 1.0F, 1.0F, ageInTicks);
        }

        @Override
        protected void renderModel(GeoModelProvider<FireflyEntity> modelProviderIn, ResourceLocation textureLocationIn, PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn, FireflyEntity entityIn, float partialTicks, float red, float green, float ageInTicks) {
            GeoModel model = modelProviderIn.getModel(modelProviderIn.getModelResource(entityIn));
            RenderType renderType = getRenderType(textureLocationIn);
            VertexConsumer ivertexbuilder = bufferIn.getBuffer(renderType);

            float alpha = Mth.clamp((Mth.cos(ageInTicks * 0.1F) * 2.0F) - 1.0F, 0.0F, 1.0F);

            this.getRenderer().render(model, entityIn, partialTicks, renderType, matrixStackIn, bufferIn, ivertexbuilder, packedLightIn, LivingEntityRenderer.getOverlayCoords(entityIn, 0.0F), 1.0F, 1.0F, 1.0F, alpha);
        }

        @Override
        public RenderType getRenderType(ResourceLocation textureLocation) {
            return WondrousWildsRenderTypes.getTranslucentGlow(textureLocation);
        }
    }
}
