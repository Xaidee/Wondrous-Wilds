package com.ineffa.wondrouswilds.registry;

import com.ineffa.wondrouswilds.WondrousWilds;
import com.ineffa.wondrouswilds.entities.FireflyEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;


public class WondrousWildsEntities {

    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, WondrousWilds.MOD_ID);

    public static final RegistryObject<EntityType<FireflyEntity>> FIREFLY = ENTITIES.register("firefly", () -> EntityType.Builder.of(
            FireflyEntity::new,
            MobCategory.WATER_AMBIENT)
                    .sized(0.1875F, 0.25F)
                    .build("firefly")
            /*FabricEntityTypeBuilder.createMob()
            .entityFactory(FireflyEntity::new)
            .defaultAttributes(FireflyEntity::createFireflyAttributes)
            .dimensions(EntityDimensions.fixed(0.1875F, 0.25F))
            .spawnGroup(SpawnGroup.WATER_AMBIENT)
            .spawnRestriction(SpawnRestriction.Location.ON_GROUND, Heightmap.Type.MOTION_BLOCKING, FireflyEntity::canFireflySpawn)
            .build()*/
    );

    /*public static final EntityType<WoodpeckerEntity> WOODPECKER = Registry.register(Registry.ENTITY_TYPE, new Identifier(WondrousWilds.MOD_ID, "woodpecker"), FabricEntityTypeBuilder.createMob()
            .entityFactory(WoodpeckerEntity::new)
            .defaultAttributes(WoodpeckerEntity::createWoodpeckerAttributes)
            .dimensions(EntityDimensions.fixed(0.3125F, 0.5F))
            .spawnGroup(SpawnGroup.CREATURE)
            .spawnRestriction(SpawnRestriction.Location.ON_GROUND, Heightmap.Type.MOTION_BLOCKING, AnimalEntity::isValidNaturalSpawn)
            .build()
    );*/

    public static void initialize() {
        //ForgeBiomeModifiers.AddSpawnsBiomeModifier.singleSpawn(ForgeRegistries.BIOMES.get).addSpawn(context -> context.hasTag(ConventionalBiomeTags.IN_OVERWORLD), SpawnGroup.WATER_AMBIENT, FIREFLY, 100, 3, 6);
    }

    /*public static final Map<EntityType<?>, Integer> DEFAULT_NESTER_CAPACITY_WEIGHTS = new ImmutableMap.Builder<EntityType<?>, Integer>()
            .put(WOODPECKER, 55)
            .build();*/
}
