package com.ineffa.wondrouswilds.util.fakeplayer;

import com.mojang.datafixers.DataFixer;
import net.minecraft.client.Minecraft;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.ServerStatsCounter;
import net.minecraft.stats.Stat;
import net.minecraft.stats.StatType;
import net.minecraft.world.entity.player.Player;

public class FakeServerStatHandler extends ServerStatsCounter {

    public FakeServerStatHandler(MinecraftServer server) {
        super(server, Minecraft.getInstance().gameDirectory);
    }

    @Override
    public void save() {}

    @Override
    public void increment(Player player, Stat<?> stat, int value) {}

    @Override
    public void setValue(Player player, Stat<?> stat, int value) {}

    @Override
    public <T> int getValue(StatType<T> type, T stat) {
        return 0;
    }

    @Override
    public int getValue(Stat<?> stat) {
        return 0;
    }

    @Override
    public void parseLocal(DataFixer dataFixer, String json) {}

    /*@Override
    protected String asString() {
        return super.asString();
    }*/

    @Override
    public void markAllDirty() {}

    @Override
    public void sendStats(ServerPlayer player) {}
}
