package com.ineffa.wondrouswilds.registry;

import com.google.common.collect.ImmutableMap;
import com.ineffa.wondrouswilds.WondrousWilds;
import com.ineffa.wondrouswilds.entities.FireflyEntity;
import com.ineffa.wondrouswilds.entities.WoodpeckerEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.Map;

@Mod.EventBusSubscriber(modid = WondrousWilds.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class WondrousWildsEntities {

    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, WondrousWilds.MOD_ID);

    public static final RegistryObject<EntityType<FireflyEntity>> FIREFLY = ENTITIES.register("firefly", () -> EntityType.Builder.of(
            FireflyEntity::new,
            MobCategory.WATER_AMBIENT)
                    .sized(0.1875F, 0.25F)
                    .build("firefly")
    );
    public static final RegistryObject<EntityType<WoodpeckerEntity>> WOODPECKER = ENTITIES.register("woodpecker", () -> EntityType.Builder.of(
            WoodpeckerEntity::new,
            MobCategory.CREATURE)
                    .sized(0.3125F, 0.5F)
                    .build("woodpecker")
    );

    public static void initialize() {
        //ForgeBiomeModifiers.AddSpawnsBiomeModifier.singleSpawn(ForgeRegistries.BIOMES.get).addSpawn(context -> context.hasTag(ConventionalBiomeTags.IN_OVERWORLD), SpawnGroup.WATER_AMBIENT, FIREFLY, 100, 3, 6);
    }

    public static void spawnPlacements() {
        SpawnPlacements.register(FIREFLY.get(), SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING, FireflyEntity::canFireflySpawn);
        SpawnPlacements.register(WOODPECKER.get(), SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING, Animal::checkAnimalSpawnRules);
    }

    public static final Map<EntityType<?>, Integer> DEFAULT_NESTER_CAPACITY_WEIGHTS = new ImmutableMap.Builder<EntityType<?>, Integer>()
            .put(WOODPECKER.get(), 55)
            .build();

    @SubscribeEvent
    public static void entityAttributes(EntityAttributeCreationEvent event) {
        event.put(FIREFLY.get(), FireflyEntity.createFireflyAttributes().build());
        event.put(WOODPECKER.get(), WoodpeckerEntity.createWoodpeckerAttributes().build());
    }
}
