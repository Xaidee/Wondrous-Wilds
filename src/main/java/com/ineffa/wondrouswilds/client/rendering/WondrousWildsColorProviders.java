package com.ineffa.wondrouswilds.client.rendering;

import com.ineffa.wondrouswilds.WondrousWilds;
import com.ineffa.wondrouswilds.registry.WondrousWildsBlocks;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.world.item.BlockItem;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = WondrousWilds.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class WondrousWildsColorProviders {

    @SubscribeEvent
    public static void registerBlockColors(RegisterColorHandlersEvent.Block event) {
        event.register((state, world, pos, tintIndex) ->
                getYellowBirchLeavesColor(),
                    WondrousWildsBlocks.YELLOW_BIRCH_LEAVES.get()
                );
        event.register((state, world, pos, tintIndex) ->
                        getOrangeBirchLeavesColor(),
                WondrousWildsBlocks.ORANGE_BIRCH_LEAVES.get()
        );
        event.register((state, world, pos, tintIndex) ->
                        getRedBirchLeavesColor(),
                WondrousWildsBlocks.RED_BIRCH_LEAVES.get()
        );
    }

    @SubscribeEvent
    public static void registerBlockColors(RegisterColorHandlersEvent.Item event) {
        BlockColors bColors = event.getBlockColors();

        event.register((stack, tintIndex) ->
                        bColors.getColor(((BlockItem) stack.getItem()).getBlock().defaultBlockState(), null, null, 0),
                WondrousWildsBlocks.YELLOW_BIRCH_LEAVES.get()
        );
        event.register((stack, tintIndex) ->
                        bColors.getColor(((BlockItem) stack.getItem()).getBlock().defaultBlockState(), null, null, 0),
                WondrousWildsBlocks.ORANGE_BIRCH_LEAVES.get()
        );
        event.register((stack, tintIndex) ->
                        bColors.getColor(((BlockItem) stack.getItem()).getBlock().defaultBlockState(), null, null, 0),
                WondrousWildsBlocks.RED_BIRCH_LEAVES.get()
        );
    }

    public static int getYellowBirchLeavesColor() {
        return 14924323;
    }

    public static int getOrangeBirchLeavesColor() {
        return 15304240;
    }

    public static int getRedBirchLeavesColor() {
        return 16204080;
    }
}
