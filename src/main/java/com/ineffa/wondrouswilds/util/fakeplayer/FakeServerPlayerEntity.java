package com.ineffa.wondrouswilds.util.fakeplayer;

import com.mojang.authlib.GameProfile;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.ServerStatsCounter;
import net.minecraft.stats.Stat;

public class FakeServerPlayerEntity extends ServerPlayer {

    private final FakePlayerAdvancementTracker fakeAdvancementTracker;
    private final FakeServerStatHandler fakeServerStatHandler;

    public FakeServerPlayerEntity(ServerLevel world, GameProfile fakeProfile) {
        super(world.getServer(), world, fakeProfile, null);

        this.fakeAdvancementTracker = new FakePlayerAdvancementTracker(this);
        this.fakeServerStatHandler = new FakeServerStatHandler(world.getServer());
    }

    @Override
    public PlayerAdvancements getAdvancements() {
        return this.fakeAdvancementTracker;
    }

    @Override
    public ServerStatsCounter getStats() {
        return this.fakeServerStatHandler;
    }

    @Override
    public void awardStat(ResourceLocation stat, int amount) {}

    @Override
    public void awardStat(Stat<?> stat, int amount) {}

    @Override
    public void resetStat(Stat<?> stat) {}

    @Override
    public void tick() {}
}
