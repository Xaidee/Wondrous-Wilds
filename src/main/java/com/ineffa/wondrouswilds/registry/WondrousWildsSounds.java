package com.ineffa.wondrouswilds.registry;

import com.ineffa.wondrouswilds.WondrousWilds;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class WondrousWildsSounds {

    public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, WondrousWilds.MOD_ID);

    public static final RegistryObject<SoundEvent> WOODPECKER_CHIRP = createSoundEvent("entity.woodpecker.chirp");
    public static final RegistryObject<SoundEvent> WOODPECKER_DRUM = createSoundEvent("entity.woodpecker.drum");

    private static RegistryObject<SoundEvent> createSoundEvent(String name) {
        return SOUNDS.register(name, () -> new SoundEvent(new ResourceLocation(WondrousWilds.MOD_ID, name)));
    }
}
