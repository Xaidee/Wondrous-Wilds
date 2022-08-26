package com.ineffa.wondrouswilds.networking;

import com.ineffa.wondrouswilds.WondrousWilds;
import com.ineffa.wondrouswilds.networking.packets.s2c.WoodpeckerDrillPacket;
import io.netty.channel.ChannelId;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public final class WondrousWildsNetwork {

    public static ResourceLocation createChannelId(String name) {
        return new ResourceLocation(WondrousWilds.MOD_ID, name);
    }

    public static SimpleChannel channel;

    @OnlyIn(Dist.CLIENT)
    public static void registerS2CPackets() {
        /*channel = NetworkRegistry.ChannelBuilder.named(WoodpeckerDrillPacket.ID)
                .simpleChannel();*/

    }

    //public static void registerC2SPackets() {}
}
