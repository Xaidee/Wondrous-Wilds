package com.ineffa.wondrouswilds.client;

import com.ineffa.wondrouswilds.client.rendering.WondrousWildsColorProviders;
import com.ineffa.wondrouswilds.client.rendering.entity.FireflyRenderer;
import com.ineffa.wondrouswilds.client.rendering.entity.WoodpeckerRenderer;
import com.ineffa.wondrouswilds.networking.WondrousWildsNetwork;
import com.ineffa.wondrouswilds.registry.WondrousWildsBlocks;
import com.ineffa.wondrouswilds.registry.WondrousWildsEntities;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.function.Supplier;

@OnlyIn(Dist.CLIENT)
public class WondrousWildsClient {

    @SubscribeEvent
    public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(WondrousWildsEntities.FIREFLY.get(), FireflyRenderer::new);
        event.registerEntityRenderer(WondrousWildsEntities.WOODPECKER.get(), WoodpeckerRenderer::new);
    }

    // This method is depricated, and will require in the future that rendertypes are instead
    // defined in the json files for the corresponding Block Models. Either manually or through Datagen.
    private static void render(Supplier<? extends Block> block, RenderType render) {
        ItemBlockRenderTypes.setRenderLayer(block.get(), render);
    }

    public static void registerBlockRenderers() {
        RenderType cutout = RenderType.cutout();
        RenderType mipped = RenderType.cutoutMipped();
        RenderType translucent = RenderType.translucent();

        render(WondrousWildsBlocks.SMALL_POLYPORE, cutout);
        render(WondrousWildsBlocks.BIG_POLYPORE, cutout);

        render(WondrousWildsBlocks.PURPLE_VIOLET, cutout);
        render(WondrousWildsBlocks.PINK_VIOLET, cutout);
        render(WondrousWildsBlocks.RED_VIOLET, cutout);
        render(WondrousWildsBlocks.WHITE_VIOLET, cutout);
        render(WondrousWildsBlocks.POTTED_PURPLE_VIOLET, cutout);
        render(WondrousWildsBlocks.POTTED_PINK_VIOLET, cutout);
        render(WondrousWildsBlocks.POTTED_RED_VIOLET, cutout);
        render(WondrousWildsBlocks.POTTED_WHITE_VIOLET, cutout);

        render(WondrousWildsBlocks.YELLOW_BIRCH_LEAVES, mipped);
        render(WondrousWildsBlocks.ORANGE_BIRCH_LEAVES, mipped);
        render(WondrousWildsBlocks.RED_BIRCH_LEAVES, mipped);
    }

    public void onInitializeClient() {
    }
}
