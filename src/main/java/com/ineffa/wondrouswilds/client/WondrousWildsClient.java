package com.ineffa.wondrouswilds.client;

import com.ineffa.wondrouswilds.WondrousWilds;
import com.ineffa.wondrouswilds.client.rendering.WondrousWildsColorProviders;
import com.ineffa.wondrouswilds.client.rendering.entity.FireflyRenderer;
import com.ineffa.wondrouswilds.client.rendering.entity.WoodpeckerRenderer;
import com.ineffa.wondrouswilds.networking.WondrousWildsNetwork;
import com.ineffa.wondrouswilds.registry.WondrousWildsBlocks;
import com.ineffa.wondrouswilds.registry.WondrousWildsEntities;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@OnlyIn(Dist.CLIENT)
public class WondrousWildsClient {

    @SubscribeEvent
    public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(WondrousWildsEntities.FIREFLY.get(), FireflyRenderer::new);
        event.registerEntityRenderer(WondrousWildsEntities.WOODPECKER.get(), WoodpeckerRenderer::new);
    }

    public void onInitializeClient() {
        /*EntityRendererRegistry.register(WondrousWildsEntities.FIREFLY, FireflyRenderer::new);
        EntityRendererRegistry.register(WondrousWildsEntities.WOODPECKER, WoodpeckerRenderer::new);

        BlockRenderLayerMap.INSTANCE.putBlock(WondrousWildsBlocks.SMALL_POLYPORE, RenderLayer.getCutout());
        BlockRenderLayerMap.INSTANCE.putBlock(WondrousWildsBlocks.BIG_POLYPORE, RenderLayer.getCutout());

        BlockRenderLayerMap.INSTANCE.putBlock(WondrousWildsBlocks.PURPLE_VIOLET, RenderLayer.getCutout());
        BlockRenderLayerMap.INSTANCE.putBlock(WondrousWildsBlocks.PINK_VIOLET, RenderLayer.getCutout());
        BlockRenderLayerMap.INSTANCE.putBlock(WondrousWildsBlocks.RED_VIOLET, RenderLayer.getCutout());
        BlockRenderLayerMap.INSTANCE.putBlock(WondrousWildsBlocks.WHITE_VIOLET, RenderLayer.getCutout());
        BlockRenderLayerMap.INSTANCE.putBlock(WondrousWildsBlocks.POTTED_PURPLE_VIOLET, RenderLayer.getCutout());
        BlockRenderLayerMap.INSTANCE.putBlock(WondrousWildsBlocks.POTTED_PINK_VIOLET, RenderLayer.getCutout());
        BlockRenderLayerMap.INSTANCE.putBlock(WondrousWildsBlocks.POTTED_RED_VIOLET, RenderLayer.getCutout());
        BlockRenderLayerMap.INSTANCE.putBlock(WondrousWildsBlocks.POTTED_WHITE_VIOLET, RenderLayer.getCutout());

        BlockRenderLayerMap.INSTANCE.putBlock(WondrousWildsBlocks.YELLOW_BIRCH_LEAVES, RenderLayer.getCutoutMipped());
        BlockRenderLayerMap.INSTANCE.putBlock(WondrousWildsBlocks.ORANGE_BIRCH_LEAVES, RenderLayer.getCutoutMipped());
        BlockRenderLayerMap.INSTANCE.putBlock(WondrousWildsBlocks.RED_BIRCH_LEAVES, RenderLayer.getCutoutMipped());
*/
        WondrousWildsColorProviders.register();

        WondrousWildsNetwork.registerS2CPackets();
    }
}
