package com.ineffa.wondrouswilds.registry;

import com.ineffa.wondrouswilds.WondrousWilds;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;

public class WondrousWildsSounds {

    public static final SoundEvent WOODPECKER_CHIRP = createSoundEvent("entity.woodpecker.chirp");
    public static final SoundEvent WOODPECKER_DRUM = createSoundEvent("entity.woodpecker.drum");

    private static SoundEvent createSoundEvent(String name) {
        ResourceLocation id = new ResourceLocation(WondrousWilds.MOD_ID, name);
        return Registry.register(Registry.SOUND_EVENT, id, new SoundEvent(id));
    }

    public static void initialize() {}
}
