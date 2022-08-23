package com.ineffa.wondrouswilds.registry;

import com.ineffa.wondrouswilds.WondrousWilds;
import com.ineffa.wondrouswilds.blocks.*;
import com.ineffa.wondrouswilds.blocks.entity.TreeHollowBlockEntity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraftforge.common.ToolAction;
import net.minecraftforge.common.ToolActions;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import static com.ineffa.wondrouswilds.WondrousWilds.WONDROUS_WILDS_ITEM_GROUP;

public class WondrousWildsBlocks {

    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, WondrousWilds.MOD_ID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, WondrousWilds.MOD_ID);

    public static final RegistryObject<Block> SMALL_POLYPORE = registerBlock("small_polypore", () -> new SmallPolyporeBlock(BlockBehaviour.Properties.of(Material.PLANT, MaterialColor.COLOR_BROWN).sound(SoundType.GRASS).noOcclusion().instabreak().noCollission()), WONDROUS_WILDS_ITEM_GROUP);
    public static final RegistryObject<Block> BIG_POLYPORE = registerBlock("big_polypore", () -> new BigPolyporeBlock(BlockBehaviour.Properties.of(Material.PLANT, MaterialColor.COLOR_BROWN).sound(SoundType.GRASS).noOcclusion().instabreak()), WONDROUS_WILDS_ITEM_GROUP);

    public static final RegistryObject<Block> PURPLE_VIOLET = registerBlock("purple_violet", () -> new VioletBlock(BlockBehaviour.Properties.of(Material.PLANT).noCollission().instabreak().sound(SoundType.GRASS)), WONDROUS_WILDS_ITEM_GROUP);
    public static final RegistryObject<Block> PINK_VIOLET = registerBlock("pink_violet", () -> new VioletBlock(BlockBehaviour.Properties.of(Material.PLANT).noCollission().instabreak().sound(SoundType.GRASS)), WONDROUS_WILDS_ITEM_GROUP);
    public static final RegistryObject<Block> RED_VIOLET = registerBlock("red_violet", () -> new VioletBlock(BlockBehaviour.Properties.of(Material.PLANT).noCollission().instabreak().sound(SoundType.GRASS)), WONDROUS_WILDS_ITEM_GROUP);
    public static final RegistryObject<Block> WHITE_VIOLET = registerBlock("white_violet", () -> new VioletBlock(BlockBehaviour.Properties.of(Material.PLANT).noCollission().instabreak().sound(SoundType.GRASS)), WONDROUS_WILDS_ITEM_GROUP);
    public static final RegistryObject<Block> POTTED_PURPLE_VIOLET = registerBlock("potted_purple_violet", () -> new FlowerPotBlock(PURPLE_VIOLET.get(), BlockBehaviour.Properties.of(Material.DECORATION).instabreak().noOcclusion()), WONDROUS_WILDS_ITEM_GROUP);
    public static final RegistryObject<Block> POTTED_PINK_VIOLET = registerBlock("potted_pink_violet", () -> new FlowerPotBlock(PINK_VIOLET.get(), BlockBehaviour.Properties.of(Material.DECORATION).instabreak().noOcclusion()), WONDROUS_WILDS_ITEM_GROUP);
    public static final RegistryObject<Block> POTTED_RED_VIOLET = registerBlock("potted_red_violet", () -> new FlowerPotBlock(RED_VIOLET.get(), BlockBehaviour.Properties.of(Material.DECORATION).instabreak().noOcclusion()), WONDROUS_WILDS_ITEM_GROUP);
    public static final RegistryObject<Block> POTTED_WHITE_VIOLET = registerBlock("potted_white_violet", () -> new FlowerPotBlock(WHITE_VIOLET.get(), BlockBehaviour.Properties.of(Material.DECORATION).instabreak().noOcclusion()), WONDROUS_WILDS_ITEM_GROUP);

    public static final RegistryObject<Block> DEAD_BIRCH_LOG = registerBlock("dead_birch_log", () -> new RotatedPillarBlock(BlockBehaviour.Properties.copy(Blocks.BIRCH_LOG)), WONDROUS_WILDS_ITEM_GROUP);

    public static final RegistryObject<Block> HOLLOW_OAK_LOG = registerBlock("hollow_oak_log", () -> new HollowLogBlock(BlockBehaviour.Properties.copy(Blocks.OAK_LOG).noOcclusion()), WONDROUS_WILDS_ITEM_GROUP);
    public static final RegistryObject<Block> HOLLOW_SPRUCE_LOG = registerBlock("hollow_spruce_log", () -> new HollowLogBlock(BlockBehaviour.Properties.copy(Blocks.SPRUCE_LOG).noOcclusion()), WONDROUS_WILDS_ITEM_GROUP);
    public static final RegistryObject<Block> HOLLOW_BIRCH_LOG = registerBlock("hollow_birch_log", () -> new HollowLogBlock(BlockBehaviour.Properties.copy(Blocks.BIRCH_LOG).noOcclusion()), WONDROUS_WILDS_ITEM_GROUP);
    public static final RegistryObject<Block> HOLLOW_JUNGLE_LOG = registerBlock("hollow_jungle_log", () -> new HollowLogBlock(BlockBehaviour.Properties.copy(Blocks.JUNGLE_LOG).noOcclusion()), WONDROUS_WILDS_ITEM_GROUP);
    public static final RegistryObject<Block> HOLLOW_ACACIA_LOG = registerBlock("hollow_acacia_log", () -> new HollowLogBlock(BlockBehaviour.Properties.copy(Blocks.ACACIA_LOG).noOcclusion()), WONDROUS_WILDS_ITEM_GROUP);
    public static final RegistryObject<Block> HOLLOW_DARK_OAK_LOG = registerBlock("hollow_dark_oak_log", () -> new HollowLogBlock(BlockBehaviour.Properties.copy(Blocks.DARK_OAK_LOG).noOcclusion()), WONDROUS_WILDS_ITEM_GROUP);
    public static final RegistryObject<Block> HOLLOW_MANGROVE_LOG = registerBlock("hollow_mangrove_log", () -> new HollowLogBlock(BlockBehaviour.Properties.copy(Blocks.MANGROVE_LOG).noOcclusion()), WONDROUS_WILDS_ITEM_GROUP);
    public static final RegistryObject<Block> HOLLOW_CRIMSON_STEM = registerBlock("hollow_crimson_stem", () -> new HollowLogBlock(BlockBehaviour.Properties.copy(Blocks.CRIMSON_STEM).noOcclusion()), WONDROUS_WILDS_ITEM_GROUP);
    public static final RegistryObject<Block> HOLLOW_WARPED_STEM = registerBlock("hollow_warped_stem", () -> new HollowLogBlock(BlockBehaviour.Properties.copy(Blocks.WARPED_STEM).noOcclusion()), WONDROUS_WILDS_ITEM_GROUP);
    public static final RegistryObject<Block> HOLLOW_DEAD_BIRCH_LOG = registerBlock("hollow_dead_birch_log", () -> new HollowLogBlock(BlockBehaviour.Properties.copy(DEAD_BIRCH_LOG.get()).noOcclusion()), WONDROUS_WILDS_ITEM_GROUP);
    public static final RegistryObject<Block> HOLLOW_STRIPPED_OAK_LOG = registerBlock("hollow_stripped_oak_log", () -> new HollowLogBlock(BlockBehaviour.Properties.copy(Blocks.STRIPPED_OAK_LOG).noOcclusion()), WONDROUS_WILDS_ITEM_GROUP);
    public static final RegistryObject<Block> HOLLOW_STRIPPED_SPRUCE_LOG = registerBlock("hollow_stripped_spruce_log", () -> new HollowLogBlock(BlockBehaviour.Properties.copy(Blocks.STRIPPED_SPRUCE_LOG).noOcclusion()), WONDROUS_WILDS_ITEM_GROUP);
    public static final RegistryObject<Block> HOLLOW_STRIPPED_BIRCH_LOG = registerBlock("hollow_stripped_birch_log", () -> new HollowLogBlock(BlockBehaviour.Properties.copy(Blocks.STRIPPED_BIRCH_LOG).noOcclusion()), WONDROUS_WILDS_ITEM_GROUP);
    public static final RegistryObject<Block> HOLLOW_STRIPPED_JUNGLE_LOG = registerBlock("hollow_stripped_jungle_log", () -> new HollowLogBlock(BlockBehaviour.Properties.copy(Blocks.STRIPPED_JUNGLE_LOG).noOcclusion()), WONDROUS_WILDS_ITEM_GROUP);
    public static final RegistryObject<Block> HOLLOW_STRIPPED_ACACIA_LOG = registerBlock("hollow_stripped_acacia_log", () -> new HollowLogBlock(BlockBehaviour.Properties.copy(Blocks.STRIPPED_ACACIA_LOG).noOcclusion()), WONDROUS_WILDS_ITEM_GROUP);
    public static final RegistryObject<Block> HOLLOW_STRIPPED_DARK_OAK_LOG = registerBlock("hollow_stripped_dark_oak_log", () -> new HollowLogBlock(BlockBehaviour.Properties.copy(Blocks.STRIPPED_DARK_OAK_LOG).noOcclusion()), WONDROUS_WILDS_ITEM_GROUP);
    public static final RegistryObject<Block> HOLLOW_STRIPPED_MANGROVE_LOG = registerBlock("hollow_stripped_mangrove_log", () -> new HollowLogBlock(BlockBehaviour.Properties.copy(Blocks.STRIPPED_MANGROVE_LOG).noOcclusion()), WONDROUS_WILDS_ITEM_GROUP);
    public static final RegistryObject<Block> HOLLOW_STRIPPED_CRIMSON_STEM = registerBlock("hollow_stripped_crimson_stem", () -> new HollowLogBlock(BlockBehaviour.Properties.copy(Blocks.STRIPPED_CRIMSON_STEM).noOcclusion()), WONDROUS_WILDS_ITEM_GROUP);
    public static final RegistryObject<Block> HOLLOW_STRIPPED_WARPED_STEM = registerBlock("hollow_stripped_warped_stem", () -> new HollowLogBlock(BlockBehaviour.Properties.copy(Blocks.STRIPPED_WARPED_STEM).noOcclusion()), WONDROUS_WILDS_ITEM_GROUP);

    public static final RegistryObject<Block> BIRCH_TREE_HOLLOW = registerBlock("birch_tree_hollow", () -> new TreeHollowBlock(BlockBehaviour.Properties.of(Material.WOOD, MaterialColor.TERRACOTTA_YELLOW).strength(2.0f).sound(SoundType.WOOD)), WONDROUS_WILDS_ITEM_GROUP);

    public static final RegistryObject<Block> YELLOW_BIRCH_LEAVES = registerBlock("yellow_birch_leaves", () -> new LeavesBlock(BlockBehaviour.Properties.copy(Blocks.BIRCH_LEAVES)), WONDROUS_WILDS_ITEM_GROUP);
    public static final RegistryObject<Block> ORANGE_BIRCH_LEAVES = registerBlock("orange_birch_leaves", () -> new LeavesBlock(BlockBehaviour.Properties.copy(Blocks.BIRCH_LEAVES)), WONDROUS_WILDS_ITEM_GROUP);
    public static final RegistryObject<Block> RED_BIRCH_LEAVES = registerBlock("red_birch_leaves", () -> new LeavesBlock(BlockBehaviour.Properties.copy(Blocks.BIRCH_LEAVES)), WONDROUS_WILDS_ITEM_GROUP);

    public static final class BlockEntities {
        public static final RegistryObject<BlockEntityType<TreeHollowBlockEntity>> TREE_HOLLOW = BLOCK_ENTITIES.register("tree_hollow", () -> BlockEntityType.Builder.of(TreeHollowBlockEntity::new, BIRCH_TREE_HOLLOW.get()).build(null));
    }

    private static RegistryObject<Block> registerBlock(String name, Supplier<? extends Block> block) {
        return BLOCKS.register(name, block);
    }

    private static <B extends Block> RegistryObject<B> registerBlock(String name, Supplier<? extends B> block, CreativeModeTab tab) {
        RegistryObject<B> blocks = BLOCKS.register(name, block);
        WondrousWildsItems.ITEMS.register(name, () -> new BlockItem(blocks.get(), new Item.Properties().tab(tab)));
        return blocks;
    }


    public static Map<Supplier<? extends Block>, Supplier<? extends Block>> Strippables = new HashMap<>();
    public static void initialize() {
        Strippables.put(DEAD_BIRCH_LOG, () -> Blocks.STRIPPED_BIRCH_LOG);

        Strippables.put(HOLLOW_OAK_LOG, HOLLOW_STRIPPED_OAK_LOG);
        Strippables.put(HOLLOW_SPRUCE_LOG, HOLLOW_STRIPPED_SPRUCE_LOG);
        Strippables.put(HOLLOW_BIRCH_LOG, HOLLOW_STRIPPED_BIRCH_LOG);
        Strippables.put(HOLLOW_JUNGLE_LOG, HOLLOW_STRIPPED_JUNGLE_LOG);
        Strippables.put(HOLLOW_ACACIA_LOG, HOLLOW_STRIPPED_ACACIA_LOG);
        Strippables.put(HOLLOW_DARK_OAK_LOG, HOLLOW_STRIPPED_DARK_OAK_LOG);
        Strippables.put(HOLLOW_MANGROVE_LOG, HOLLOW_STRIPPED_MANGROVE_LOG);
        Strippables.put(HOLLOW_CRIMSON_STEM, HOLLOW_STRIPPED_CRIMSON_STEM);
        Strippables.put(HOLLOW_WARPED_STEM, HOLLOW_STRIPPED_WARPED_STEM);
    }

    @SubscribeEvent
    public static void blockToolInteractions(BlockEvent.BlockToolModificationEvent event) {
        ToolAction action = event.getToolAction();
        BlockState state = event.getState();
        if (!event.isSimulated()) {
            if (action == ToolActions.AXE_STRIP) {
                for (Map.Entry<Supplier<? extends Block>, Supplier<? extends Block>> blocks : Strippables.entrySet()) {
                    if (state.is(blocks.getKey().get())) {
                        event.setFinalState(blocks.getValue().get().defaultBlockState());
                    }
                }
            }
        }
    }
}
