package com.ineffa.wondrouswilds.networking;

import com.ineffa.wondrouswilds.WondrousWilds;
import com.ineffa.wondrouswilds.networking.packets.s2c.WoodpeckerDrillPacket;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.util.Identifier;

public final class WondrousWildsNetwork {

    public static Identifier createChannelId(String name) {
        return new Identifier(WondrousWilds.MOD_ID, name);
    }

    @Environment(value = EnvType.CLIENT)
    public static void registerS2CPackets() {
        ClientPlayNetworking.registerGlobalReceiver(WoodpeckerDrillPacket.ID, WoodpeckerDrillPacket::receive);
    }

    //public static void registerC2SPackets() {}
}
