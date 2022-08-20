package com.ineffa.wondrouswilds.blocks.entity;

import com.google.common.collect.Lists;
import com.ineffa.wondrouswilds.blocks.TreeHollowBlock;
import com.ineffa.wondrouswilds.entities.FlyingAndWalkingAnimalEntity;
import com.ineffa.wondrouswilds.entities.TreeHollowNester;
import com.ineffa.wondrouswilds.registry.WondrousWildsBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static com.ineffa.wondrouswilds.registry.WondrousWildsEntities.DEFAULT_NESTER_CAPACITY_WEIGHTS;

public class TreeHollowBlockEntity extends BlockEntity {

    public static final String INHABITANTS_KEY = "Inhabitants";
    public static final String ENTITY_DATA_KEY = "EntityData";
    public static final String CAPACITY_WEIGHT_KEY = "CapacityWeight";
    public static final String TICKS_IN_NEST_KEY = "TicksInNest";
    public static final String MIN_OCCUPATION_TICKS_KEY = "MinOccupationTicks";

    private static final List<String> IRRELEVANT_INHABITANT_NBT_KEYS = Arrays.asList("CannotEnterNestTicks", "Air", "DeathTime", "FallDistance", "FallFlying", "Fire", "HurtByTimestamp", "HurtTime", "Motion", "OnGround", "PortalCooldown", "Pos", "Rotation", "Passengers", "Leash", "UUID");

    private final List<Inhabitant> inhabitants = Lists.newArrayList();

    public TreeHollowBlockEntity(BlockPos pos, BlockState state) {
        super(WondrousWildsBlocks.BlockEntities.TREE_HOLLOW.get(), pos, state);
    }

    @Override
    protected void saveAdditional(CompoundTag nbt) {
        super.saveAdditional(nbt);

        nbt.put(INHABITANTS_KEY, this.getInhabitantsNbt());
    }

    @Override
    public void load(CompoundTag nbt) {
        super.load(nbt);

        this.inhabitants.clear();

        ListTag nbtList = nbt.getList(INHABITANTS_KEY, ListTag.TAG_COMPOUND);
        for (int i = 0; i < nbtList.size(); ++i) {
            CompoundTag nbtCompound = nbtList.getCompound(i);

            Inhabitant inhabitant = new Inhabitant(false, nbtCompound.getCompound(ENTITY_DATA_KEY), nbtCompound.getInt(CAPACITY_WEIGHT_KEY), nbtCompound.getInt(MIN_OCCUPATION_TICKS_KEY), nbtCompound.getInt(TICKS_IN_NEST_KEY));
            this.inhabitants.add(inhabitant);
        }
    }

    private static void removeIrrelevantNbt(CompoundTag nbtCompound) {
        for (String key : IRRELEVANT_INHABITANT_NBT_KEYS) nbtCompound.remove(key);
    }

    public ListTag getInhabitantsNbt() {
        ListTag nbtList = new ListTag();
        for (Inhabitant inhabitant : this.inhabitants) {
            CompoundTag copy = inhabitant.entityData.copy();
            CompoundTag nbt = new CompoundTag();

            nbt.put(ENTITY_DATA_KEY, copy);
            nbt.putInt(CAPACITY_WEIGHT_KEY, inhabitant.capacityWeight);
            nbt.putInt(MIN_OCCUPATION_TICKS_KEY, inhabitant.minOccupationTicks);
            nbt.putInt(TICKS_IN_NEST_KEY, inhabitant.ticksInNest);

            nbtList.add(nbt);
        }
        return nbtList;
    }

    public int getInhabitantCount() {
        return this.inhabitants.size();
    }

    public boolean hasInhabitants() {
        return !this.inhabitants.isEmpty();
    }

    public int getMaxCapacity() {
        return 100;
    }

    public int getUsedCapacity() {
        int usedCapacity = 0;

        for (Inhabitant inhabitant : this.inhabitants) usedCapacity += inhabitant.capacityWeight;

        return usedCapacity;
    }

    public int getRemainingCapacity() {
        return this.getMaxCapacity() - this.getUsedCapacity();
    }

    public boolean isCapacityFilled() {
        return this.getUsedCapacity() >= this.getMaxCapacity();
    }

    public boolean entityMatchesInhabitants(Mob entity) {
        String nesterId = EntityType.getKey(entity.getType()).toString();

        for (Inhabitant inhabitant : this.inhabitants) {
            String inhabitantId = inhabitant.entityData.getString(Entity.ID_TAG);

            if (!Objects.equals(inhabitantId, nesterId)) return false;
        }

        return true;
    }

    public boolean canFitNester(TreeHollowNester nester) {
        return nester.getNestCapacityWeight() <= this.getRemainingCapacity();
    }

    public boolean tryAddingInhabitant(TreeHollowNester nester) {
        if (!(nester instanceof Mob nesterEntity)) return false;

        if ((this.isCapacityFilled() || !this.canFitNester(nester)) || !this.entityMatchesInhabitants(nesterEntity)) {
            if (nesterEntity instanceof Animal animal && animal.isBaby() && this.entityMatchesInhabitants(animal)) return false;

            this.alertInhabitants(nesterEntity, InhabitantReleaseState.ALERT);
            return false;
        }

        nesterEntity.stopRiding();
        nesterEntity.ejectPassengers();

        this.addInhabitant(nester);

        Level world = this.getLevel();
        if (world != null) {
            BlockPos pos = this.getBlockPos();

            world.playSound(null, pos.getX(), pos.getY(), pos.getZ(), SoundEvents.BEEHIVE_ENTER, SoundSource.BLOCKS, 1.0F, 1.0F);
            world.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(nesterEntity, this.getBlockState()));
        }

        nesterEntity.discard();

        super.setChanged();

        return true;
    }

    private void addInhabitant(TreeHollowNester inhabitant) {
        if (!(inhabitant instanceof Mob entity)) return;

        CompoundTag entityData = new CompoundTag();
        entity.save(entityData);

        this.inhabitants.add(new Inhabitant(false, entityData, inhabitant.getNestCapacityWeight(), 0, inhabitant.getMinTicksInNest()));
    }

    public void addFreshInhabitant(EntityType<?> entityType) {
        if (!DEFAULT_NESTER_CAPACITY_WEIGHTS.containsKey(entityType)) return;

        CompoundTag entityData = new CompoundTag();
        entityData.putString("id", ForgeRegistries.ENTITY_TYPES.getKey(entityType).toString());
        entityData.put("NestPos", NbtUtils.writeBlockPos(this.getBlockPos()));

        this.inhabitants.add(new Inhabitant(true, entityData, DEFAULT_NESTER_CAPACITY_WEIGHTS.get(entityType), 0, 600));
    }

    public static void serverTick(Level world, BlockPos pos, BlockState state, TreeHollowBlockEntity treeHollow) {

        tickInhabitants(world, pos, state, treeHollow.inhabitants);

        /*if (treeHollow.hasInhabitants() && world.getRandom().nextDouble() < 0.005D) {
            double d = (double) pos.getX() + 0.5D;
            double e = pos.getY();
            double f = (double) pos.getZ() + 0.5D;
            world.playSound(null, d, e, f, SoundEvents.BLOCK_BEEHIVE_WORK, SoundCategory.BLOCKS, 1.0F, 1.0F);
        }*/
    }

    private static void tickInhabitants(Level world, BlockPos pos, BlockState state, List<Inhabitant> inhabitants) {
        boolean released = false;

        Iterator<Inhabitant> iterator = inhabitants.iterator();
        while (iterator.hasNext()) {
            Inhabitant inhabitant = iterator.next();
            if (inhabitant.ticksInNest > inhabitant.minOccupationTicks) {
                if (tryReleasingInhabitant(world, pos, state, InhabitantReleaseState.RELEASE, inhabitant, null)) {
                    released = true;
                    iterator.remove();
                }
            }
            ++inhabitant.ticksInNest;
        }

        if (released) setChanged(world, pos, state);
    }

    public void alertInhabitants(@Nullable LivingEntity attacker, InhabitantReleaseState releaseState) {
        this.alertInhabitants(attacker, Objects.requireNonNull(this.getLevel()).getBlockState(this.getBlockPos()), releaseState);
    }

    public void alertInhabitants(@Nullable LivingEntity attacker, BlockState state, InhabitantReleaseState releaseState) {
        List<TreeHollowNester> inhabitants = this.tryReleasingInhabitants(state, releaseState);

        if (attacker != null) {
            for (TreeHollowNester inhabitant : inhabitants) {
                if (!inhabitant.defendsNest() || !(inhabitant instanceof Mob inhabitantEntity)) continue;

                if (attacker.getOnPos().distSqr(inhabitantEntity.getOnPos()) > inhabitantEntity.getAttributeValue(Attributes.FOLLOW_RANGE)) continue;

                inhabitantEntity.setTarget(attacker);

                //inhabitant.setCannotEnterNestTicks(inhabitant.getMinTicksOutOfNest());
            }
        }
    }

    private List<TreeHollowNester> tryReleasingInhabitants(BlockState state, InhabitantReleaseState releaseState) {
        ArrayList<TreeHollowNester> releasedInhabitants = new ArrayList<>();

        this.inhabitants.removeIf(inhabitant -> tryReleasingInhabitant(Objects.requireNonNull(this.getLevel()), this.worldPosition, state, releaseState, inhabitant, releasedInhabitants));

        if (!releasedInhabitants.isEmpty()) super.setChanged();

        return releasedInhabitants;
    }

    private static boolean tryReleasingInhabitant(Level world, BlockPos treeHollowPos, BlockState state, InhabitantReleaseState releaseState, Inhabitant inhabitant, @Nullable List<TreeHollowNester> inhabitantGetter) {
        if ((world.isNight() || world.isRaining()) && releaseState == InhabitantReleaseState.RELEASE) return false;

        CompoundTag nbtCompound = inhabitant.entityData.copy();
        removeIrrelevantNbt(nbtCompound);
        nbtCompound.put("NestPos", NbtUtils.writeBlockPos(treeHollowPos));

        Direction frontDirection = state.getValue(TreeHollowBlock.FACING);
        BlockPos frontPos = treeHollowPos.relative(frontDirection);

        boolean hasSpaceToRelease = world.getBlockState(frontPos).getCollisionShape(world, frontPos).isEmpty();

        if (!hasSpaceToRelease && releaseState != InhabitantReleaseState.EMERGENCY) return false;

        Mob inhabitantEntity = (Mob) EntityType.loadEntityRecursive(nbtCompound, world, e -> e);
        if (inhabitantEntity != null) {
            double d0 = !hasSpaceToRelease ? 0.0D : 0.55D + (double) (inhabitantEntity.getBbWidth() / 2.0F);
            double d1 = (double) treeHollowPos.getX() + 0.5D + d0 * (double) frontDirection.getStepX();
            double d2 = (double) treeHollowPos.getY() + 0.5D - (double) (inhabitantEntity.getBbHeight() / 2.0F);
            double d3 = (double) treeHollowPos.getZ() + 0.5D + d0 * (double) frontDirection.getStepX();
            inhabitantEntity.absMoveTo(d1, d2, d3, inhabitantEntity.getYRot(), inhabitantEntity.getXRot());

            if (inhabitantEntity instanceof Animal) ageInhabitant(inhabitant.ticksInNest, (Animal) inhabitantEntity);

            if (inhabitant.isFresh) {
                if (world instanceof ServerLevelAccessor serverWorldAccess) {
                    inhabitantEntity.finalizeSpawn(serverWorldAccess, serverWorldAccess.getCurrentDifficultyAt(inhabitantEntity.getOnPos()), MobSpawnType.CHUNK_GENERATION, null, nbtCompound);
                }
                if (inhabitantEntity instanceof FlyingAndWalkingAnimalEntity flyingEntity) flyingEntity.setFlying(true);
            }

            if (inhabitantGetter != null) inhabitantGetter.add((TreeHollowNester) inhabitantEntity);

            world.playSound(null, treeHollowPos, SoundEvents.BEEHIVE_EXIT, SoundSource.BLOCKS, 1.0F, 1.0F);
            world.gameEvent(GameEvent.BLOCK_CHANGE, treeHollowPos, GameEvent.Context.of(inhabitantEntity, world.getBlockState(treeHollowPos)));

            return world.addFreshEntity(inhabitantEntity);
        }
        return false;
    }

    private static void ageInhabitant(int ticks, Animal inhabitant) {
        int i = inhabitant.getAge();
        if (i < 0) inhabitant.setAge(Math.min(0, i + ticks));
        else if (i > 0) inhabitant.setAge(Math.max(0, i - ticks));

        inhabitant.setInLoveTime(Math.max(0, inhabitant.getInLoveTime() - ticks));
    }

    private static class Inhabitant {
        final boolean isFresh;
        final CompoundTag entityData;
        final int capacityWeight;
        final int minOccupationTicks;
        int ticksInNest;

        private Inhabitant(boolean isFresh, CompoundTag entityData, int capacityWeight, int ticksInNest, int minOccupationTicks) {

            removeIrrelevantNbt(entityData);

            this.isFresh = isFresh;
            this.entityData = entityData;
            this.capacityWeight = capacityWeight;
            this.ticksInNest = ticksInNest;
            this.minOccupationTicks = minOccupationTicks;
        }
    }

    public enum InhabitantReleaseState {
        RELEASE,
        ALERT,
        EMERGENCY
    }
}
