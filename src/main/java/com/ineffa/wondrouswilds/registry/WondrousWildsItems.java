package com.ineffa.wondrouswilds.registry;

import com.ineffa.wondrouswilds.WondrousWilds;
import com.ineffa.wondrouswilds.items.LovifierItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class WondrousWildsItems {

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, WondrousWilds.MOD_ID);

    public static final RegistryObject<Item> FIREFLY_SPAWN_EGG = ITEMS.register("firefly_spawn_egg", () -> new ForgeSpawnEggItem(WondrousWildsEntities.FIREFLY, 2563094, 14876540, new Item.Properties().tab(WondrousWilds.WONDROUS_WILDS_ITEM_GROUP)));
    public static final RegistryObject<Item> WOODPECKER_SPAWN_EGG = ITEMS.register("woodpecker_spawn_egg", () -> new ForgeSpawnEggItem(WondrousWildsEntities.WOODPECKER, 2761271, 16740713, new Item.Properties().tab(WondrousWilds.WONDROUS_WILDS_ITEM_GROUP)));

    public static final RegistryObject<Item> LOVIFIER = ITEMS.register("lovifier", () -> new LovifierItem(new Item.Properties().tab(WondrousWilds.WONDROUS_WILDS_ITEM_GROUP).stacksTo(1)));
}
