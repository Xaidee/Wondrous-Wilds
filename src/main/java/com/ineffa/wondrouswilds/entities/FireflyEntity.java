package com.ineffa.wondrouswilds.entities;

import com.ineffa.wondrouswilds.entities.ai.FireflyHideGoal;
import com.ineffa.wondrouswilds.entities.ai.FireflyLandOnEntityGoal;
import com.ineffa.wondrouswilds.entities.ai.FireflyWanderFlyingGoal;
import com.ineffa.wondrouswilds.entities.ai.FireflyWanderLandGoal;
import com.ineffa.wondrouswilds.registry.WondrousWildsTags;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundSetPassengersPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.animal.SnowGolem;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Objects;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class FireflyEntity extends FlyingAndWalkingAnimalEntity implements IAnimatable {

    private static final EntityDataAccessor<Integer> LAND_ON_ENTITY_COOLDOWN = SynchedEntityData.defineId(FireflyEntity.class, EntityDataSerializers.INT);

    public FireflyEntity(EntityType<? extends FireflyEntity> entityType, Level world) {
        super(entityType, world);

        this.setPathfindingMalus(BlockPathTypes.DANGER_FIRE, -1.0f);
        this.setPathfindingMalus(BlockPathTypes.WATER, -1.0f);
        this.setPathfindingMalus(BlockPathTypes.WATER_BORDER, 16.0f);
        this.setPathfindingMalus(BlockPathTypes.COCOA, -1.0f);
        this.setPathfindingMalus(BlockPathTypes.FENCE, -1.0f);

        //this.ignoreCameraFrustum = true;
    }

    public static boolean canFireflySpawn(EntityType<FireflyEntity> entityType, LevelAccessor world, MobSpawnType spawnReason, BlockPos spawnAttemptPos, RandomSource random) {
        if (!world.getBlockState(spawnAttemptPos.below()).is(WondrousWildsTags.BlockTags.FIREFLIES_SPAWNABLE_ON) || !FireflyEntity.checkAnimalSpawnRules(entityType, world, spawnReason, spawnAttemptPos, random)) return false;

        Holder<Biome> biome = world.getBiome(spawnAttemptPos);
        int skylightLevel = world.getBrightness(LightLayer.SKY, spawnAttemptPos);

        // Spawn immediately if the spawn position is underground and the biome allows underground spawning
        if (skylightLevel <= 0 && biome.is(WondrousWildsTags.BiomeTags.SPAWNS_FIREFLIES_UNDERGROUND)) return true;

        ServerLevel serverWorld = Objects.requireNonNull(world.getServer()).overworld();

        // Otherwise, cancel if it is not raining and the biome requires it
        if (biome.is(WondrousWildsTags.BiomeTags.SPAWNS_FIREFLIES_ON_SURFACE_ONLY_IN_RAIN)) {
            if (!serverWorld.isRaining()) return false;
        }
        // Otherwise, cancel if the biome does not allow surface spawning at all
        else if (!biome.is(WondrousWildsTags.BiomeTags.SPAWNS_FIREFLIES_ON_SURFACE)) return false;

        // Finally, spawn if basic surface spawning conditions are met
        return serverWorld.isNight() && skylightLevel >= 6 && world.getBrightness(LightLayer.BLOCK, spawnAttemptPos) <= 0;
    }

    // Removes the light level restriction set by AnimalEntity
    @Override
    public float getWalkTargetValue(BlockPos pos, LevelReader world) {
        return 0.0F;
    }

    public static AttributeSupplier.Builder createFireflyAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 6.0D)
                .add(Attributes.FLYING_SPEED, 0.3D)
                .add(Attributes.MOVEMENT_SPEED, 0.1D);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();

        this.entityData.define(LAND_ON_ENTITY_COOLDOWN, 0);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag nbt) {
        super.readAdditionalSaveData(nbt);

        this.setLandOnEntityCooldown(nbt.getInt("LandOnEntityCooldown"));
    }

    @Override
    public void addAdditionalSaveData(CompoundTag nbt) {
        super.addAdditionalSaveData(nbt);

        nbt.putInt("LandOnEntityCooldown", this.getLandOnEntityCooldown());
    }

    public int getLandOnEntityCooldown() {
        return this.entityData.get(LAND_ON_ENTITY_COOLDOWN);
    }

    public void setLandOnEntityCooldown(int ticks) {
        this.entityData.set(LAND_ON_ENTITY_COOLDOWN, ticks);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new PanicGoal(this, 2.0D));
        this.goalSelector.addGoal(2, new FireflyHideGoal(this, 1.0D, 16, 16));
        this.goalSelector.addGoal(3, new FireflyLandOnEntityGoal(this, LivingEntity.class));
        this.goalSelector.addGoal(4, new FireflyWanderLandGoal(this, 1.0D));
        this.goalSelector.addGoal(4, new FireflyWanderFlyingGoal(this));
    }

    @Override
    public void tick() {
        super.tick();

        if (this.isPassenger()) {
            if (!this.getLevel().isClientSide && (this.getRandom().nextInt(600) == 0 || this.shouldHide())) this.stopRiding();

            if (this.getVehicle() != null) this.setYHeadRot(this.getVehicle().getYHeadRot());
        }
        else if (this.getLandOnEntityCooldown() > 0) this.setLandOnEntityCooldown(this.getLandOnEntityCooldown() - 1);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (super.hurt(source, amount)) {
            if (this.isPassenger()) this.stopRiding();

            else if (!this.isAiFlying()) this.setFlying(true);

            return true;
        }
        else return false;
    }

    @Override
    protected float getStandingEyeHeight(Pose pose, EntityDimensions dimensions) {
        return dimensions.height * 0.7F;
    }

    @Override
    public MobType getMobType() {
        return MobType.ARTHROPOD;
    }

    @Override
    public boolean removeWhenFarAway(double distanceSquared) {
        return true;
    }

    public boolean canWander() {
        return !this.isPassenger();
    }

    public boolean canSearchForEntityToLandOn() {
        return this.getLandOnEntityCooldown() <= 0 && !this.isPassenger() && !this.shouldHide();
    }

    public boolean shouldHide() {
        return this.getLevel().isDay() && this.getLevel().getBrightness(LightLayer.SKY, this.getOnPos()) >= 6;
    }

    @Override
    public void stopRiding() {
        if (!this.isFlying()) this.setFlying(true);

        Entity vehicle = this.getVehicle();

        super.stopRiding();

        if (!this.getLevel().isClientSide) {
            if (vehicle instanceof Player) {
                ServerLevel serverWorld = (ServerLevel) this.getLevel();
                for (ServerPlayer player : serverWorld.players()) player.connection.send(new ClientboundSetPassengersPacket(vehicle));
            }
        }
    }

    @Override
    public double getMyRidingOffset() {
        double offset = 0.0D;

        if (this.isPassenger()) {
            EntityType<?> vehicleType = Objects.requireNonNull(this.getVehicle()).getType();

            if (vehicleType == EntityType.ZOMBIE_VILLAGER || vehicleType == EntityType.ENDERMAN) offset = 0.6D;
            else if (vehicleType == EntityType.PLAYER || vehicleType == EntityType.ZOMBIE || vehicleType == EntityType.SKELETON || vehicleType == EntityType.VILLAGER || vehicleType == EntityType.WANDERING_TRADER || vehicleType == EntityType.PILLAGER || vehicleType == EntityType.VINDICATOR || vehicleType == EntityType.EVOKER || vehicleType == EntityType.ILLUSIONER) offset = 0.5D;

            else if (vehicleType == EntityType.CREEPER || vehicleType == EntityType.SPIDER || vehicleType == EntityType.COW || vehicleType == EntityType.CHICKEN) offset = 0.3D;
            else if (vehicleType == EntityType.SHEEP || vehicleType == EntityType.PIG) offset = 0.2D;
            else if (vehicleType == EntityType.WITCH) offset = 1.0D;
            else if (vehicleType == EntityType.ALLAY) offset = 0.1D;
            else if (vehicleType == EntityType.IRON_GOLEM) offset = 0.65D;

            else if (vehicleType == EntityType.SNOW_GOLEM) {
                offset = 0.45D;

                if (!((SnowGolem) this.getVehicle()).hasPumpkin()) offset -= 0.175D;
            }

            else if (vehicleType == EntityType.ARMOR_STAND) {
                offset = 0.4D;

                if (!((ArmorStand) this.getVehicle()).getItemBySlot(EquipmentSlot.HEAD).isEmpty()) offset += 0.1D;
            }

            if (this.getVehicle().isCrouching()) offset -= 0.15D;
        }

        return offset;
    }

    @Override
    public boolean causeFallDamage(float fallDistance, float damageMultiplier, DamageSource damageSource) {
        return false;
    }

    @Nullable
    @Override
    public AgeableMob getBreedOffspring(ServerLevel world, AgeableMob entity) {
        return null;
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {}

    @Override
    protected SoundEvent getAmbientSound() {
        return null;
    }

    @Nullable
    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return null;
    }

    @Nullable
    @Override
    protected SoundEvent getDeathSound() {
        return null;
    }

    private final AnimationFactory factory = new AnimationFactory(this);

    @Override
    public AnimationFactory getFactory() {
        return this.factory;
    }

    @Override
    public void registerControllers(AnimationData animationData) {
        AnimationController<FireflyEntity> molangAnimationController = new AnimationController<>(this, "molangAnimationController", 2, this::molangAnimationPredicate);

        AnimationController<FireflyEntity> antennaMolangController = new AnimationController<>(this, "antennaMolangController", 2, this::antennaMolangPredicate);

        animationData.addAnimationController(molangAnimationController);
        animationData.addAnimationController(antennaMolangController);
    }

    private <E extends IAnimatable> PlayState molangAnimationPredicate(AnimationEvent<E> event) {
        if (this.isFlying()) event.getController().setAnimation(new AnimationBuilder().addAnimation("flyingMolang"));

        else event.getController().setAnimation(new AnimationBuilder().addAnimation("groundedMolang"));

        return PlayState.CONTINUE;
    }

    private <E extends IAnimatable> PlayState antennaMolangPredicate(AnimationEvent<E> event) {
        event.getController().setAnimation(new AnimationBuilder().addAnimation("antennaeMolang"));

        return PlayState.CONTINUE;
    }
}
