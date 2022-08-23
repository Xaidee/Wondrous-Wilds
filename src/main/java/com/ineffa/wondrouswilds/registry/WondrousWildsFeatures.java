package com.ineffa.wondrouswilds.registry;

import com.google.common.collect.ImmutableList;
import com.ineffa.wondrouswilds.WondrousWilds;
import com.ineffa.wondrouswilds.mixin.TreeConfiguredFeaturesInvoker;
import com.ineffa.wondrouswilds.world.features.FallenLogFeature;
import com.ineffa.wondrouswilds.world.features.VioletPatchFeature;
import com.ineffa.wondrouswilds.world.features.configs.FallenLogFeatureConfig;
import com.ineffa.wondrouswilds.world.features.configs.VioletPatchFeatureConfig;
import com.ineffa.wondrouswilds.world.features.trees.decorators.CobwebTreeDecorator;
import com.ineffa.wondrouswilds.world.features.trees.decorators.HangingBeeNestTreeDecorator;
import com.ineffa.wondrouswilds.world.features.trees.decorators.PolyporeTreeDecorator;
import com.ineffa.wondrouswilds.world.features.trees.decorators.TreeHollowTreeDecorator;
import com.ineffa.wondrouswilds.world.features.trees.foliage.FancyBirchFoliagePlacer;
import com.ineffa.wondrouswilds.world.features.trees.trunks.StraightBranchingTrunkPlacer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.data.worldgen.features.FeatureUtils;
import net.minecraft.data.worldgen.features.MiscOverworldFeatures;
import net.minecraft.data.worldgen.features.VegetationFeatures;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.data.worldgen.placement.TreePlacements;
import net.minecraft.data.worldgen.placement.VegetationPlacements;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.valueproviders.ClampedInt;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.WeightedPlacedFeature;
import net.minecraft.world.level.levelgen.feature.configurations.RandomFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.RandomPatchConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.SimpleBlockConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.featuresize.TwoLayersFeatureSize;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacerType;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecorator;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecoratorType;
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacerType;
import net.minecraft.world.level.levelgen.placement.*;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.List;

public class WondrousWildsFeatures {

    public static final DeferredRegister<Feature<?>> FEATURES = DeferredRegister.create(ForgeRegistries.FEATURES, WondrousWilds.MOD_ID);
    public static final DeferredRegister<ConfiguredFeature<?, ?>> CONFIGURED_FEATURES = DeferredRegister.create(Registry.CONFIGURED_FEATURE_REGISTRY, WondrousWilds.MOD_ID);
    public static final DeferredRegister<PlacedFeature> PLACED_FEATURES = DeferredRegister.create(Registry.PLACED_FEATURE_REGISTRY, WondrousWilds.MOD_ID);

    public static final class Trees {

        public static final class TrunkPlacers {
            public static final DeferredRegister<TrunkPlacerType<?>> TRUNK_PLACERS = DeferredRegister.create(Registry.TRUNK_PLACER_TYPE_REGISTRY, WondrousWilds.MOD_ID);

            public static final RegistryObject<TrunkPlacerType<StraightBranchingTrunkPlacer>> STRAIGHT_BRANCHING_TRUNK = TRUNK_PLACERS.register("straight_branching_trunk_placer", () -> new TrunkPlacerType<>(StraightBranchingTrunkPlacer.CODEC));
        }

        public static final class FoliagePlacers {
            public static final DeferredRegister<FoliagePlacerType<?>> FOLIAGE_PLACERS = DeferredRegister.create(Registry.FOLIAGE_PLACER_TYPE_REGISTRY, WondrousWilds.MOD_ID);

            public static final RegistryObject<FoliagePlacerType<FancyBirchFoliagePlacer>> FANCY_BIRCH = FOLIAGE_PLACERS.register("fancy_birch_foliage_placer", () -> new FoliagePlacerType<>(FancyBirchFoliagePlacer.CODEC));
        }

        public static final class Decorators {
            public static final DeferredRegister<TreeDecoratorType<?>> TREE_DECORATORS = DeferredRegister.create(Registry.TREE_DECORATOR_TYPE_REGISTRY, WondrousWilds.MOD_ID);

            public static final RegistryObject<TreeDecoratorType<TreeHollowTreeDecorator>> TREE_HOLLOW_TYPE = TREE_DECORATORS.register("tree_hollow", () -> new TreeDecoratorType<>(TreeHollowTreeDecorator.CODEC));
            public static final RegistryObject<TreeDecoratorType<HangingBeeNestTreeDecorator>> HANGING_BEE_NEST_TYPE = TREE_DECORATORS.register("hanging_bee_nest", () -> new TreeDecoratorType<>(HangingBeeNestTreeDecorator.CODEC));
            public static final RegistryObject<TreeDecoratorType<PolyporeTreeDecorator>> POLYPORE_TYPE = TREE_DECORATORS.register("polypores", () -> new TreeDecoratorType<>(PolyporeTreeDecorator.CODEC));
            public static final RegistryObject<TreeDecoratorType<CobwebTreeDecorator>> COBWEB_TYPE = TREE_DECORATORS.register("cobwebs", () -> new TreeDecoratorType<>(CobwebTreeDecorator.CODEC));

            public static final TreeDecorator TREE_HOLLOW = TreeHollowTreeDecorator.INSTANCE;
            public static final TreeDecorator HANGING_BEE_NEST = HangingBeeNestTreeDecorator.INSTANCE;
            public static final TreeDecorator POLYPORES = PolyporeTreeDecorator.INSTANCE;
            public static final TreeDecorator COBWEBS = CobwebTreeDecorator.INSTANCE;
        }

        public static final class Configs {
            private static TreeConfiguration.TreeConfigurationBuilder fancyBirchConfig() {
                return new TreeConfiguration.TreeConfigurationBuilder(
                        BlockStateProvider.simple(Blocks.BIRCH_LOG),
                        new StraightBranchingTrunkPlacer(10, 10, 0, 0, 3, 1, 1),
                        BlockStateProvider.simple(Blocks.BIRCH_LEAVES), new FancyBirchFoliagePlacer(ConstantInt.of(3), ConstantInt.of(0)),
                        new TwoLayersFeatureSize(1, 0, 2)
                ).decorators(ImmutableList.of(Decorators.POLYPORES, Decorators.COBWEBS)).ignoreVines();
            }

            private static TreeConfiguration.TreeConfigurationBuilder fancyBirchWithBeesConfig() {
                return new TreeConfiguration.TreeConfigurationBuilder(
                        BlockStateProvider.simple(Blocks.BIRCH_LOG),
                        new StraightBranchingTrunkPlacer(10, 10, 0, 1, 3, 1, 1),
                        BlockStateProvider.simple(Blocks.BIRCH_LEAVES), new FancyBirchFoliagePlacer(ConstantInt.of(3), ConstantInt.of(0)),
                        new TwoLayersFeatureSize(1, 0, 2)
                ).decorators(ImmutableList.of(Decorators.HANGING_BEE_NEST, Decorators.POLYPORES, Decorators.COBWEBS)).ignoreVines();
            }

            private static TreeConfiguration.TreeConfigurationBuilder yellowFancyBirchConfig() {
                return new TreeConfiguration.TreeConfigurationBuilder(
                        BlockStateProvider.simple(Blocks.BIRCH_LOG),
                        new StraightBranchingTrunkPlacer(10, 10, 0, 0, 3, 1, 1),
                        BlockStateProvider.simple(WondrousWildsBlocks.YELLOW_BIRCH_LEAVES.get()), new FancyBirchFoliagePlacer(ConstantInt.of(3), ConstantInt.of(0)),
                        new TwoLayersFeatureSize(1, 0, 2)
                ).decorators(ImmutableList.of(Decorators.POLYPORES, Decorators.COBWEBS)).ignoreVines();
            }

            private static TreeConfiguration.TreeConfigurationBuilder yellowFancyBirchWithBeesConfig() {
                return new TreeConfiguration.TreeConfigurationBuilder(
                        BlockStateProvider.simple(Blocks.BIRCH_LOG),
                        new StraightBranchingTrunkPlacer(10, 10, 0, 1, 3, 1, 1),
                        BlockStateProvider.simple(WondrousWildsBlocks.YELLOW_BIRCH_LEAVES.get()), new FancyBirchFoliagePlacer(ConstantInt.of(3), ConstantInt.of(0)),
                        new TwoLayersFeatureSize(1, 0, 2)
                ).decorators(ImmutableList.of(Decorators.HANGING_BEE_NEST, Decorators.POLYPORES, Decorators.COBWEBS)).ignoreVines();
            }

            private static TreeConfiguration.TreeConfigurationBuilder orangeFancyBirchConfig() {
                return new TreeConfiguration.TreeConfigurationBuilder(
                        BlockStateProvider.simple(Blocks.BIRCH_LOG),
                        new StraightBranchingTrunkPlacer(10, 10, 0, 0, 3, 1, 1),
                        BlockStateProvider.simple(WondrousWildsBlocks.ORANGE_BIRCH_LEAVES.get()), new FancyBirchFoliagePlacer(ConstantInt.of(3), ConstantInt.of(0)),
                        new TwoLayersFeatureSize(1, 0, 2)
                ).decorators(ImmutableList.of(Decorators.POLYPORES, Decorators.COBWEBS)).ignoreVines();
            }

            private static TreeConfiguration.TreeConfigurationBuilder orangeFancyBirchWithBeesConfig() {
                return new TreeConfiguration.TreeConfigurationBuilder(
                        BlockStateProvider.simple(Blocks.BIRCH_LOG),
                        new StraightBranchingTrunkPlacer(10, 10, 0, 1, 3, 1, 1),
                        BlockStateProvider.simple(WondrousWildsBlocks.ORANGE_BIRCH_LEAVES.get()), new FancyBirchFoliagePlacer(ConstantInt.of(3), ConstantInt.of(0)),
                        new TwoLayersFeatureSize(1, 0, 2)
                ).decorators(ImmutableList.of(Decorators.HANGING_BEE_NEST, Decorators.POLYPORES, Decorators.COBWEBS)).ignoreVines();
            }

            private static TreeConfiguration.TreeConfigurationBuilder redFancyBirchConfig() {
                return new TreeConfiguration.TreeConfigurationBuilder(
                        BlockStateProvider.simple(Blocks.BIRCH_LOG),
                        new StraightBranchingTrunkPlacer(10, 10, 0, 0, 3, 1, 1),
                        BlockStateProvider.simple(WondrousWildsBlocks.RED_BIRCH_LEAVES.get()), new FancyBirchFoliagePlacer(ConstantInt.of(3), ConstantInt.of(0)),
                        new TwoLayersFeatureSize(1, 0, 2)
                ).decorators(ImmutableList.of(Decorators.POLYPORES, Decorators.COBWEBS)).ignoreVines();
            }

            private static TreeConfiguration.TreeConfigurationBuilder redFancyBirchWithBeesConfig() {
                return new TreeConfiguration.TreeConfigurationBuilder(
                        BlockStateProvider.simple(Blocks.BIRCH_LOG),
                        new StraightBranchingTrunkPlacer(10, 10, 0, 1, 3, 1, 1),
                        BlockStateProvider.simple(WondrousWildsBlocks.RED_BIRCH_LEAVES.get()), new FancyBirchFoliagePlacer(ConstantInt.of(3), ConstantInt.of(0)),
                        new TwoLayersFeatureSize(1, 0, 2)
                ).decorators(ImmutableList.of(Decorators.HANGING_BEE_NEST, Decorators.POLYPORES, Decorators.COBWEBS)).ignoreVines();
            }
        }

        public static final RegistryObject<ConfiguredFeature<?, ?>> FANCY_BIRCH_CONFIGURED = CONFIGURED_FEATURES.register("fancy_birch", () -> new ConfiguredFeature<>(Feature.TREE, Configs.fancyBirchConfig().build()));
        public static final RegistryObject<PlacedFeature> FANCY_BIRCH_PLACED = PLACED_FEATURES.register("fancy_birch", () -> new PlacedFeature(FANCY_BIRCH_CONFIGURED.getHolder().get(), List.of(PlacementUtils.filteredByBlockSurvival(Blocks.BIRCH_SAPLING))));
        public static final RegistryObject<ConfiguredFeature<?, ?>> FANCY_BIRCH_WITH_WOODPECKERS_CONFIGURED = CONFIGURED_FEATURES.register("fancy_birch_with_woodpeckers", () -> new ConfiguredFeature<>(Feature.TREE, Configs.fancyBirchConfig().decorators(List.of(Decorators.TREE_HOLLOW)).build()));
        public static final RegistryObject<PlacedFeature> FANCY_BIRCH_WITH_WOODPECKERS_PLACED = PLACED_FEATURES.register("fancy_birch_with_woodpeckers", () -> new PlacedFeature(FANCY_BIRCH_WITH_WOODPECKERS_CONFIGURED.getHolder().get(), List.of(PlacementUtils.filteredByBlockSurvival(Blocks.BIRCH_SAPLING))));
        public static final RegistryObject<ConfiguredFeature<?, ?>> FANCY_BIRCH_WITH_BEES_CONFIGURED = CONFIGURED_FEATURES.register("fancy_birch_with_bees", () -> new ConfiguredFeature<>(Feature.TREE, Configs.fancyBirchWithBeesConfig().build()));
        public static final RegistryObject<PlacedFeature> FANCY_BIRCH_WITH_BEES_PLACED = PLACED_FEATURES.register("fancy_birch_with_bees", () -> new PlacedFeature(FANCY_BIRCH_WITH_BEES_CONFIGURED.getHolder().get(), List.of(PlacementUtils.filteredByBlockSurvival(Blocks.BIRCH_SAPLING))));

        public static final RegistryObject<ConfiguredFeature<?, ?>> YELLOW_FANCY_BIRCH_CONFIGURED = CONFIGURED_FEATURES.register("yellow_fancy_birch", () -> new ConfiguredFeature<>(Feature.TREE, Configs.yellowFancyBirchConfig().build()));
        public static final RegistryObject<PlacedFeature> YELLOW_FANCY_BIRCH_PLACED = PLACED_FEATURES.register("yellow_fancy_birch", () -> new PlacedFeature(YELLOW_FANCY_BIRCH_CONFIGURED.getHolder().get(), List.of(PlacementUtils.filteredByBlockSurvival(Blocks.BIRCH_SAPLING))));
        public static final RegistryObject<ConfiguredFeature<?, ?>> YELLOW_FANCY_BIRCH_WITH_WOODPECKERS_CONFIGURED = CONFIGURED_FEATURES.register("yellow_fancy_birch_with_woodpeckers", () -> new ConfiguredFeature<>(Feature.TREE, Configs.yellowFancyBirchConfig().decorators(List.of(Decorators.TREE_HOLLOW)).build()));
        public static final RegistryObject<PlacedFeature> YELLOW_FANCY_BIRCH_WITH_WOODPECKERS_PLACED = PLACED_FEATURES.register("yellow_fancy_birch_with_woodpeckers", () -> new PlacedFeature(YELLOW_FANCY_BIRCH_WITH_WOODPECKERS_CONFIGURED.getHolder().get(), List.of(PlacementUtils.filteredByBlockSurvival(Blocks.BIRCH_SAPLING))));
        public static final RegistryObject<ConfiguredFeature<?, ?>> YELLOW_FANCY_BIRCH_WITH_BEES_CONFIGURED = CONFIGURED_FEATURES.register("yellow_fancy_birch_with_bees", () -> new ConfiguredFeature<>(Feature.TREE, Configs.yellowFancyBirchWithBeesConfig().build()));
        public static final RegistryObject<PlacedFeature> YELLOW_FANCY_BIRCH_WITH_BEES_PLACED = PLACED_FEATURES.register("yellow_fancy_birch_with_bees", () -> new PlacedFeature(YELLOW_FANCY_BIRCH_WITH_BEES_CONFIGURED.getHolder().get(), List.of(PlacementUtils.filteredByBlockSurvival(Blocks.BIRCH_SAPLING))));

        public static final RegistryObject<ConfiguredFeature<?, ?>> ORANGE_FANCY_BIRCH_CONFIGURED = CONFIGURED_FEATURES.register("orange_fancy_birch", () -> new ConfiguredFeature<>(Feature.TREE, Configs.orangeFancyBirchConfig().build()));
        public static final RegistryObject<PlacedFeature> ORANGE_FANCY_BIRCH_PLACED = PLACED_FEATURES.register("orange_fancy_birch", () -> new PlacedFeature(ORANGE_FANCY_BIRCH_CONFIGURED.getHolder().get(), List.of(PlacementUtils.filteredByBlockSurvival(Blocks.BIRCH_SAPLING))));
        public static final RegistryObject<ConfiguredFeature<?, ?>> ORANGE_FANCY_BIRCH_WITH_WOODPECKERS_CONFIGURED = CONFIGURED_FEATURES.register("orange_fancy_birch_with_woodpeckers", () -> new ConfiguredFeature<>(Feature.TREE, Configs.orangeFancyBirchConfig().decorators(List.of(Decorators.TREE_HOLLOW)).build()));
        public static final RegistryObject<PlacedFeature> ORANGE_FANCY_BIRCH_WITH_WOODPECKERS_PLACED = PLACED_FEATURES.register("orange_fancy_birch_with_woodpeckers", () -> new PlacedFeature(ORANGE_FANCY_BIRCH_WITH_WOODPECKERS_CONFIGURED.getHolder().get(), List.of(PlacementUtils.filteredByBlockSurvival(Blocks.BIRCH_SAPLING))));
        public static final RegistryObject<ConfiguredFeature<?, ?>> ORANGE_FANCY_BIRCH_WITH_BEES_CONFIGURED = CONFIGURED_FEATURES.register("orange_fancy_birch_with_bees", () -> new ConfiguredFeature<>(Feature.TREE, Configs.orangeFancyBirchWithBeesConfig().build()));
        public static final RegistryObject<PlacedFeature> ORANGE_FANCY_BIRCH_WITH_BEES_PLACED = PLACED_FEATURES.register("orange_fancy_birch_with_bees", () -> new PlacedFeature(ORANGE_FANCY_BIRCH_WITH_BEES_CONFIGURED.getHolder().get(), List.of(PlacementUtils.filteredByBlockSurvival(Blocks.BIRCH_SAPLING))));

        public static final RegistryObject<ConfiguredFeature<?, ?>> RED_FANCY_BIRCH_CONFIGURED = CONFIGURED_FEATURES.register("red_fancy_birch", () -> new ConfiguredFeature<>(Feature.TREE, Configs.redFancyBirchConfig().build()));
        public static final RegistryObject<PlacedFeature> RED_FANCY_BIRCH_PLACED = PLACED_FEATURES.register("red_fancy_birch", () -> new PlacedFeature(RED_FANCY_BIRCH_CONFIGURED.getHolder().get(), List.of(PlacementUtils.filteredByBlockSurvival(Blocks.BIRCH_SAPLING))));
        public static final RegistryObject<ConfiguredFeature<?, ?>> RED_FANCY_BIRCH_WITH_WOODPECKERS_CONFIGURED = CONFIGURED_FEATURES.register("red_fancy_birch_with_woodpeckers", () -> new ConfiguredFeature<>(Feature.TREE, Configs.redFancyBirchConfig().decorators(List.of(Decorators.TREE_HOLLOW)).build()));
        public static final RegistryObject<PlacedFeature> RED_FANCY_BIRCH_WITH_WOODPECKERS_PLACED = PLACED_FEATURES.register("red_fancy_birch_with_woodpeckers", () -> new PlacedFeature(RED_FANCY_BIRCH_WITH_WOODPECKERS_CONFIGURED.getHolder().get(), List.of(PlacementUtils.filteredByBlockSurvival(Blocks.BIRCH_SAPLING))));
        public static final RegistryObject<ConfiguredFeature<?, ?>> RED_FANCY_BIRCH_WITH_BEES_CONFIGURED = CONFIGURED_FEATURES.register("red_fancy_birch_with_bees", () -> new ConfiguredFeature<>(Feature.TREE, Configs.redFancyBirchWithBeesConfig().build()));
        public static final RegistryObject<PlacedFeature> RED_FANCY_BIRCH_WITH_BEES_PLACED = PLACED_FEATURES.register("red_fancy_birch_with_bees", () -> new PlacedFeature(RED_FANCY_BIRCH_WITH_BEES_CONFIGURED.getHolder().get(), List.of(PlacementUtils.filteredByBlockSurvival(Blocks.BIRCH_SAPLING))));

        public static final RegistryObject<ConfiguredFeature<?, ?>> TALL_BIRCH_CONFIGURED = CONFIGURED_FEATURES.register("tall_birch", () -> new ConfiguredFeature<>(Feature.TREE, TreeConfiguredFeaturesInvoker.tallBirchConfig().build()));
        public static final RegistryObject<PlacedFeature> TALL_BIRCH_PLACED = PLACED_FEATURES.register("tall_birch", () -> new PlacedFeature(TALL_BIRCH_CONFIGURED.getHolder().get(), List.of(PlacementUtils.filteredByBlockSurvival(Blocks.BIRCH_SAPLING))));

        public static final RegistryObject<ConfiguredFeature<?, ?>> BIRCH_FOREST_TREES_CONFIGURED = CONFIGURED_FEATURES.register("birch_forest_trees", () -> new ConfiguredFeature<>(Feature.RANDOM_SELECTOR, new RandomFeatureConfiguration(List.of(
                new WeightedPlacedFeature(FANCY_BIRCH_PLACED.getHolder().get(), 0.85F),
                new WeightedPlacedFeature(FANCY_BIRCH_WITH_WOODPECKERS_PLACED.getHolder().get(), 0.1F),
                new WeightedPlacedFeature(FANCY_BIRCH_WITH_BEES_PLACED.getHolder().get(), 0.075F)
        ), TreePlacements.BIRCH_CHECKED)));
        public static final RegistryObject<PlacedFeature> BIRCH_FOREST_TREES_PLACED = PLACED_FEATURES.register("birch_forest_trees", () -> new PlacedFeature(BIRCH_FOREST_TREES_CONFIGURED.getHolder().get(), List.of(PlacementUtils.countExtra(5, 0.1F, 1), PlacementUtils.filteredByBlockSurvival(Blocks.BIRCH_SAPLING))));

        public static final RegistryObject<ConfiguredFeature<?, ?>> OLD_GROWTH_BIRCH_FOREST_TREES_CONFIGURED = CONFIGURED_FEATURES.register("old_growth_birch_forest_trees", () -> new ConfiguredFeature<>(Feature.RANDOM_SELECTOR, new RandomFeatureConfiguration(List.of(
                new WeightedPlacedFeature(FANCY_BIRCH_PLACED.getHolder().get(), 0.5F),
                new WeightedPlacedFeature(YELLOW_FANCY_BIRCH_PLACED.getHolder().get(), 0.5F),
                new WeightedPlacedFeature(ORANGE_FANCY_BIRCH_PLACED.getHolder().get(), 0.5F),
                new WeightedPlacedFeature(RED_FANCY_BIRCH_PLACED.getHolder().get(), 0.5F),
                new WeightedPlacedFeature(FANCY_BIRCH_WITH_WOODPECKERS_PLACED.getHolder().get(), 0.04F),
                new WeightedPlacedFeature(YELLOW_FANCY_BIRCH_WITH_WOODPECKERS_PLACED.getHolder().get(), 0.04F),
                new WeightedPlacedFeature(ORANGE_FANCY_BIRCH_WITH_WOODPECKERS_PLACED.getHolder().get(), 0.04F),
                new WeightedPlacedFeature(RED_FANCY_BIRCH_WITH_WOODPECKERS_PLACED.getHolder().get(), 0.04F),
                new WeightedPlacedFeature(FANCY_BIRCH_WITH_BEES_PLACED.getHolder().get(), 0.03F),
                new WeightedPlacedFeature(YELLOW_FANCY_BIRCH_WITH_BEES_PLACED.getHolder().get(), 0.03F),
                new WeightedPlacedFeature(ORANGE_FANCY_BIRCH_WITH_BEES_PLACED.getHolder().get(), 0.03F),
                new WeightedPlacedFeature(RED_FANCY_BIRCH_WITH_BEES_PLACED.getHolder().get(), 0.03F)
        ), TALL_BIRCH_PLACED.getHolder().get())));
        public static final RegistryObject<PlacedFeature> OLD_GROWTH_BIRCH_FOREST_TREES_PLACED = PLACED_FEATURES.register("old_growth_birch_forest_trees", () -> new PlacedFeature(OLD_GROWTH_BIRCH_FOREST_TREES_CONFIGURED.getHolder().get(), List.of(PlacementUtils.countExtra(10, 0.1F, 1), PlacementUtils.filteredByBlockSurvival(Blocks.BIRCH_SAPLING))));
    }

    public static final Feature<FallenLogFeatureConfig> FALLEN_LOG = new FallenLogFeature();
    public static final RegistryObject<ConfiguredFeature<?, ?>> FALLEN_BIRCH_LOG_CONFIGURED = CONFIGURED_FEATURES.register("fallen_birch_log", () -> new ConfiguredFeature<>(FALLEN_LOG, fallenBirchLogConfig()));
    public static final RegistryObject<PlacedFeature> FALLEN_BIRCH_LOG_PLACED = PLACED_FEATURES.register("fallen_birch_log", () -> new PlacedFeature(FALLEN_BIRCH_LOG_CONFIGURED.getHolder().get(), List.of(RarityFilter.onAverageOnceEvery(12), PlacementUtils.filteredByBlockSurvival(Blocks.BIRCH_SAPLING))));

    public static final RegistryObject<ConfiguredFeature<?, ?>> LILY_OF_THE_VALLEY_PATCH_CONFIGURED = CONFIGURED_FEATURES.register("lily_of_the_valley_patch", () -> new ConfiguredFeature<>(Feature.FLOWER, new RandomPatchConfiguration(64, 6, 2, FeatureUtils.simplePatchConfiguration(Feature.SIMPLE_BLOCK, new SimpleBlockConfiguration(BlockStateProvider.simple(Blocks.LILY_OF_THE_VALLEY))).feature())));
    public static final RegistryObject<PlacedFeature> LILY_OF_THE_VALLEY_PATCH_PLACED = PLACED_FEATURES.register("lily_of_the_valley_patch", () -> new PlacedFeature(LILY_OF_THE_VALLEY_PATCH_CONFIGURED.getHolder().get(), List.of(RarityFilter.onAverageOnceEvery(10), InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP_TOP_SOLID, BiomeFilter.biome())));

    public static final RegistryObject<PlacedFeature> BIRCH_FOREST_TALL_FLOWERS_PLACED = PLACED_FEATURES.register("birch_forest_tall_flowers", () -> new PlacedFeature(Holder.hackyErase(VegetationFeatures.FOREST_FLOWERS), List.of(RarityFilter.onAverageOnceEvery(3), InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP_TOP_SOLID, CountPlacement.of(ClampedInt.of(UniformInt.of(-3, 1), 0, 1)), BiomeFilter.biome())));

    public static final Feature<VioletPatchFeatureConfig> VIOLET_PATCH = new VioletPatchFeature();
    public static final RegistryObject<ConfiguredFeature<?, ?>> PURPLE_VIOLETS_CONFIGURED = CONFIGURED_FEATURES.register("purple_violets", () -> new ConfiguredFeature<>(VIOLET_PATCH, purpleVioletPatchConfig()));
    public static final RegistryObject<ConfiguredFeature<?, ?>> PINK_VIOLETS_CONFIGURED = CONFIGURED_FEATURES.register("pink_violets", () -> new ConfiguredFeature<>(VIOLET_PATCH, pinkVioletPatchConfig()));
    public static final RegistryObject<ConfiguredFeature<?, ?>> RED_VIOLETS_CONFIGURED = CONFIGURED_FEATURES.register("red_violets", () -> new ConfiguredFeature<>(VIOLET_PATCH, redVioletPatchConfig()));
    public static final RegistryObject<ConfiguredFeature<?, ?>> WHITE_VIOLETS_CONFIGURED = CONFIGURED_FEATURES.register("white_violets", () -> new ConfiguredFeature<>(VIOLET_PATCH, whiteVioletPatchConfig()));
    public static final RegistryObject<PlacedFeature> PURPLE_VIOLETS_PLACED = PLACED_FEATURES.register("purple_violets", () -> new PlacedFeature(PURPLE_VIOLETS_CONFIGURED.getHolder().get(), List.of(RarityFilter.onAverageOnceEvery(8), InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP_TOP_SOLID, BiomeFilter.biome())));
    public static final RegistryObject<PlacedFeature> PINK_VIOLETS_PLACED = PLACED_FEATURES.register("pink_violets", () -> new PlacedFeature(PINK_VIOLETS_CONFIGURED.getHolder().get(), List.of(RarityFilter.onAverageOnceEvery(8), InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP_TOP_SOLID, BiomeFilter.biome())));
    public static final RegistryObject<PlacedFeature> RED_VIOLETS_PLACED = PLACED_FEATURES.register("red_violets", () -> new PlacedFeature(RED_VIOLETS_CONFIGURED.getHolder().get(), List.of(RarityFilter.onAverageOnceEvery(8), InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP_TOP_SOLID, BiomeFilter.biome())));
    public static final RegistryObject<PlacedFeature> WHITE_VIOLETS_PLACED = PLACED_FEATURES.register("white_violets", () -> new PlacedFeature(WHITE_VIOLETS_CONFIGURED.getHolder().get(), List.of(RarityFilter.onAverageOnceEvery(8), InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP_TOP_SOLID, BiomeFilter.biome())));

    public static final RegistryObject<PlacedFeature> BIRCH_FOREST_GRASS_PATCH_PLACED = PLACED_FEATURES.register("birch_forest_grass_patch", () -> new PlacedFeature(Holder.hackyErase(VegetationFeatures.PATCH_GRASS), VegetationPlacements.worldSurfaceSquaredWithCount(6)));
    public static final RegistryObject<PlacedFeature> BIRCH_FOREST_TALL_GRASS_PATCH_PLACED = PLACED_FEATURES.register("birch_forest_tall_grass_patch", () -> new PlacedFeature(Holder.hackyErase(VegetationFeatures.PATCH_TALL_GRASS), List.of(RarityFilter.onAverageOnceEvery(20), InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP_TOP_SOLID, BiomeFilter.biome())));

    public static final RegistryObject<PlacedFeature> BIRCH_FOREST_ROCK_PLACED = PLACED_FEATURES.register("birch_forest_rock", () -> new PlacedFeature(Holder.hackyErase(MiscOverworldFeatures.FOREST_ROCK), List.of(RarityFilter.onAverageOnceEvery(5), InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP_TOP_SOLID, BiomeFilter.biome())));

    private static FallenLogFeatureConfig fallenBirchLogConfig() {
        return new FallenLogFeatureConfig(BlockStateProvider.simple(WondrousWildsBlocks.HOLLOW_DEAD_BIRCH_LOG.get()), 3, 8);
    }

    private static VioletPatchFeatureConfig purpleVioletPatchConfig() {
        return new VioletPatchFeatureConfig(BlockStateProvider.simple(WondrousWildsBlocks.PURPLE_VIOLET.get()));
    }

    private static VioletPatchFeatureConfig pinkVioletPatchConfig() {
        return new VioletPatchFeatureConfig(BlockStateProvider.simple(WondrousWildsBlocks.PINK_VIOLET.get()));
    }

    private static VioletPatchFeatureConfig redVioletPatchConfig() {
        return new VioletPatchFeatureConfig(BlockStateProvider.simple(WondrousWildsBlocks.RED_VIOLET.get()));
    }

    private static VioletPatchFeatureConfig whiteVioletPatchConfig() {
        return new VioletPatchFeatureConfig(BlockStateProvider.simple(WondrousWildsBlocks.WHITE_VIOLET.get()));
    }
}
