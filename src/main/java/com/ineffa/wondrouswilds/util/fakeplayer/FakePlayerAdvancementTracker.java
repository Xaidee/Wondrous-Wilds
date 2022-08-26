package com.ineffa.wondrouswilds.util.fakeplayer;

import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.ServerAdvancementManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.LevelResource;

import javax.annotation.Nullable;
import java.io.File;

public class FakePlayerAdvancementTracker extends PlayerAdvancements {

    public FakePlayerAdvancementTracker(ServerPlayer owner) {
        super(null, null, owner.getServer().getAdvancements(), new File(owner.getServer().getWorldPath(LevelResource.PLAYER_ADVANCEMENTS_DIR).toFile(), owner.getUUID() + ".json"), owner);
    }

    @Override
    public void stopListening() {}

    @Override
    public void reload(ServerAdvancementManager advancementLoader) {}

    @Override
    public void save() {}

    @Override
    public boolean award(Advancement advancement, String criterionName) {
        return false;
    }

    @Override
    public boolean revoke(Advancement advancement, String criterionName) {
        return false;
    }

    @Override
    public void flushDirty(ServerPlayer player) {}

    @Override
    public void setSelectedTab(@Nullable Advancement advancement) {}

    @Override
    public AdvancementProgress getOrStartProgress(Advancement advancement) {
        return new AdvancementProgress();
    }
}
