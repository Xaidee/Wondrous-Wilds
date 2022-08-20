package com.ineffa.wondrouswilds.registry;

import com.ineffa.wondrouswilds.WondrousWilds;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;

public class WondrousWildsTags {

    public static class Blocks {
        public static final TagKey<Block> VIOLETS = createBlockTag("violets");
        public static final TagKey<Block> WOODPECKERS_INTERACT_WITH = createBlockTag("woodpeckers_interact_with");

        public static final TagKey<Block> FIREFLIES_SPAWNABLE_ON = createBlockTag("fireflies_spawnable_on");
        public static final TagKey<Block> FIREFLIES_HIDE_IN = createBlockTag("fireflies_hide_in");
    }

    public static class Items {
        public static final TagKey<Item> VIOLETS = createItemTag("violets");
    }

    public static class Biomes {
        public static final TagKey<Biome> SPAWNS_FIREFLIES_ON_SURFACE = createBiomeTag("spawns_fireflies_on_surface");
        public static final TagKey<Biome> SPAWNS_FIREFLIES_ON_SURFACE_ONLY_IN_RAIN = createBiomeTag("spawns_fireflies_on_surface_only_in_rain");
        public static final TagKey<Biome> SPAWNS_FIREFLIES_UNDERGROUND = createBiomeTag("spawns_fireflies_underground");
    }

    private static TagKey<Block> createBlockTag(String name) {
        return BlockTags.create(new ResourceLocation(WondrousWilds.MOD_ID, name));
    }

    private static TagKey<Item> createItemTag(String name) {
        return ItemTags.create(new ResourceLocation(WondrousWilds.MOD_ID, name));
    }

    private static TagKey<Biome> createBiomeTag(String name) {
        return BiomeTags.create(new ResourceLocation(WondrousWilds.MOD_ID, name));
    }
}
