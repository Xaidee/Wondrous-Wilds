package com.ineffa.wondrouswilds.networking.packets.s2c;

import com.ineffa.wondrouswilds.networking.WondrousWildsNetwork;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.filters.ForgeConnectionNetworkFilter;

public final class WoodpeckerDrillPacket {

    public static final ResourceLocation ID = WondrousWildsNetwork.createChannelId("woodpecker_drill");

    @OnlyIn(Dist.CLIENT)
    public static void receive(Minecraft client, ForgeConnectionNetworkFilter handler, FriendlyByteBuf buf, PacketDistributor.PacketTarget responseSender) {
        BlockPos drillPos = buf.readBlockPos();
        Direction drillSide = buf.readEnum(Direction.class);

        client.execute(() -> {
            for (int i = 0; i < 10; ++i) client.particleEngine.crack(drillPos, drillSide);
        });
    }
}
