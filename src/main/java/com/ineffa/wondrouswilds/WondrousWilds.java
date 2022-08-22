package com.ineffa.wondrouswilds;

import com.ineffa.wondrouswilds.registry.*;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.worldgen.placement.VegetationPlacements;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.common.data.JsonCodecProvider;
import net.minecraftforge.common.world.BiomeModifier;
import net.minecraftforge.common.world.ForgeBiomeModifiers;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.bernie.example.GeckoLibMod;
import software.bernie.geckolib3.GeckoLib;

import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Mod(WondrousWilds.MOD_ID)
public class WondrousWilds {
	public static final String MOD_ID = "wondrouswilds";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static final CreativeModeTab WONDROUS_WILDS_ITEM_GROUP = new CreativeModeTab("wondrous_wilds") {
		@Override
		public @NotNull ItemStack makeIcon() {
			return new ItemStack(WondrousWildsItems.LOVIFIER.get());
		}
	};

	public WondrousWilds() {
		LOGGER.info("Wondrous Wilds initializing!");

		GeckoLibMod.DISABLE_IN_DEV = true;
		GeckoLib.initialize();

		IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();

		bus.addListener(this::gatherData);

		DeferredRegister<?>[] registers = {
				WondrousWildsBlocks.BLOCKS,
				WondrousWildsBlocks.BLOCK_ENTITIES,
				WondrousWildsItems.ITEMS,
				WondrousWildsEntities.ENTITIES,
				WondrousWildsFeatures.FEATURES,
				WondrousWildsFeatures.CONFIGURED_FEATURES,
				WondrousWildsFeatures.PLACED_FEATURES,
				WondrousWildsFeatures.Trees.TrunkPlacers.TRUNK_PLACERS,
				WondrousWildsFeatures.Trees.FoliagePlacers.FOLIAGE_PLACERS,
				WondrousWildsFeatures.Trees.Decorators.TREE_DECORATORS,
		};

		for (DeferredRegister<?> register : registers) {
			register.register(bus);
		}

		WondrousWildsSounds.initialize();

		WondrousWildsEntities.initialize();
		WondrousWildsBlocks.initialize();

		WondrousWildsFeatures.initialize();
	}

	public void gatherData(GatherDataEvent event) {
		DataGenerator generator = event.getGenerator();
		ExistingFileHelper helper = event.getExistingFileHelper();

		if(event.includeClient()) {

		}
		if(event.includeServer()) {
			generator.addProvider(true, BiomeModifierProvider.create(generator, helper));
		}
	}

	static class BiomeModifierProvider {

		public static JsonCodecProvider<BiomeModifier> create(DataGenerator generator, ExistingFileHelper helper) {
			RegistryAccess access = RegistryAccess.builtinCopy();
			Registry<Biome> biomeRegistry = access.registryOrThrow(Registry.BIOME_REGISTRY);
			Registry<PlacedFeature> placedFeatures = access.registryOrThrow(Registry.PLACED_FEATURE_REGISTRY);

			HolderSet<Biome> allBirchForests = HolderSet.direct(List.of(biomeRegistry.getHolderOrThrow(Biomes.BIRCH_FOREST), biomeRegistry.getHolderOrThrow(Biomes.OLD_GROWTH_BIRCH_FOREST)));
			HolderSet<Biome> birchForest = HolderSet.direct(List.of(biomeRegistry.getHolderOrThrow(Biomes.BIRCH_FOREST)));
			HolderSet<Biome> oldGrowthBirchForest = HolderSet.direct(List.of(biomeRegistry.getHolderOrThrow(Biomes.OLD_GROWTH_BIRCH_FOREST)));
			HashMap<ResourceLocation, BiomeModifier> modifiers = new HashMap<>();

			// All Birch Forests
			addModifier(modifiers, "remove_default_vegetation", new ForgeBiomeModifiers.RemoveFeaturesBiomeModifier(allBirchForests, HolderSet.direct(
					VegetationPlacements.PATCH_GRASS_FOREST,
					VegetationPlacements.FOREST_FLOWERS),
					Set.of(GenerationStep.Decoration.VEGETAL_DECORATION)));

			addModifier(modifiers, "add_custom_vegetation", new ForgeBiomeModifiers.AddFeaturesBiomeModifier(allBirchForests, of(placedFeatures,
					WondrousWildsFeatures.BIRCH_FOREST_GRASS_PATCH_PLACED,
					WondrousWildsFeatures.BIRCH_FOREST_TALL_FLOWERS_PLACED,
					WondrousWildsFeatures.FALLEN_BIRCH_LOG_PLACED,
					WondrousWildsFeatures.BIRCH_FOREST_TALL_GRASS_PATCH_PLACED,
					WondrousWildsFeatures.PURPLE_VIOLETS_PLACED,
					WondrousWildsFeatures.PINK_VIOLETS_PLACED,
					WondrousWildsFeatures.RED_VIOLETS_PLACED,
					WondrousWildsFeatures.WHITE_VIOLETS_PLACED,
					WondrousWildsFeatures.LILY_OF_THE_VALLEY_PATCH_PLACED
			), GenerationStep.Decoration.VEGETAL_DECORATION));
			addModifier(modifiers, "add_rocks", new ForgeBiomeModifiers.AddFeaturesBiomeModifier(allBirchForests, of(placedFeatures,
					WondrousWildsFeatures.BIRCH_FOREST_ROCK_PLACED
			), GenerationStep.Decoration.LOCAL_MODIFICATIONS));
			addModifier(modifiers, "add_fox_spawns", new ForgeBiomeModifiers.AddSpawnsBiomeModifier(allBirchForests, List.of(
					new MobSpawnSettings.SpawnerData(EntityType.FOX, 6, 2, 4)
			)));

			// Birch Forest
			addModifier(modifiers, "remove_default_trees", new ForgeBiomeModifiers.RemoveFeaturesBiomeModifier(birchForest, HolderSet.direct(
					VegetationPlacements.TREES_BIRCH),
					Set.of(GenerationStep.Decoration.VEGETAL_DECORATION)));
			addModifier(modifiers, "add_custom_trees", new ForgeBiomeModifiers.AddFeaturesBiomeModifier(birchForest, of(placedFeatures,
					WondrousWildsFeatures.Trees.BIRCH_FOREST_TREES_PLACED
			), GenerationStep.Decoration.VEGETAL_DECORATION));

			// Old Growth Birch Forest
			addModifier(modifiers, "remove_default_tall_trees", new ForgeBiomeModifiers.RemoveFeaturesBiomeModifier(oldGrowthBirchForest, HolderSet.direct(
					VegetationPlacements.BIRCH_TALL),
					Set.of(GenerationStep.Decoration.VEGETAL_DECORATION)));
			addModifier(modifiers, "add_custom_tall_trees", new ForgeBiomeModifiers.AddFeaturesBiomeModifier(oldGrowthBirchForest, of(placedFeatures,
					WondrousWildsFeatures.Trees.OLD_GROWTH_BIRCH_FOREST_TREES_PLACED
			), GenerationStep.Decoration.VEGETAL_DECORATION));

			return JsonCodecProvider.forDatapackRegistry(generator, helper, WondrousWilds.MOD_ID, RegistryOps.create(JsonOps.INSTANCE, access), ForgeRegistries.Keys.BIOME_MODIFIERS, modifiers);
		}

		private static void addModifier(HashMap<ResourceLocation, BiomeModifier> modifiers, String name, BiomeModifier modifier) {
			modifiers.put(new ResourceLocation(WondrousWilds.MOD_ID, name), modifier);
		}

		@SafeVarargs
		@SuppressWarnings("ConstantConditions")
		private static HolderSet<PlacedFeature> of(Registry<PlacedFeature> placedFeatures, RegistryObject<PlacedFeature>... features) {
			return HolderSet.direct(Stream.of(features).map(registryObject -> placedFeatures.getOrCreateHolderOrThrow(registryObject.getKey())).collect(Collectors.toList()));
		}
	}
}
