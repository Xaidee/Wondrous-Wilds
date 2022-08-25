package com.ineffa.wondrouswilds.entities;

import com.ineffa.wondrouswilds.blocks.TreeHollowBlock;
import com.ineffa.wondrouswilds.blocks.entity.InhabitableNestBlockEntity;
import com.ineffa.wondrouswilds.entities.ai.*;
import com.ineffa.wondrouswilds.networking.packets.s2c.WoodpeckerDrillPacket;
import com.ineffa.wondrouswilds.registry.WondrousWildsEntities;
import com.ineffa.wondrouswilds.registry.WondrousWildsItems;
import com.ineffa.wondrouswilds.registry.WondrousWildsSounds;
import com.ineffa.wondrouswilds.registry.WondrousWildsTags;
import com.ineffa.wondrouswilds.util.WondrousWildsUtils;
import com.ineffa.wondrouswilds.util.fakeplayer.WoodpeckerFakePlayer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.PillarBlock;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.control.BodyControl;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.Angerable;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.particle.DefaultParticleType;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.tag.BlockTags;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TimeHelper;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.intprovider.UniformIntProvider;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;

import java.util.Objects;
import java.util.UUID;
import java.util.function.Predicate;

import static com.ineffa.wondrouswilds.util.WondrousWildsUtils.HORIZONTAL_DIRECTIONS;
import static com.ineffa.wondrouswilds.util.WondrousWildsUtils.TREE_HOLLOW_MAP;

public class WoodpeckerEntity extends FlyingAndWalkingAnimalEntity implements BlockNester, Angerable, IAnimatable {

    public static final String CLING_POS_KEY = "ClingPos";
    public static final String NEST_POS_KEY = "NestPos";
    public static final String PLAY_SESSIONS_BEFORE_TAME_KEY = "PlaySessionsBeforeTame";
    public static final String TAME_KEY = "Tame";

    public static final int PECKS_NEEDED_FOR_NEST = 200;

    private int playSessionsBeforeTame;

    private final Predicate<WoodpeckerEntity> AVOID_WOODPECKER_PREDICATE = otherWoodpecker -> this.getAttacker() == otherWoodpecker;

    private static final TrackedData<BlockPos> CLING_POS = DataTracker.registerData(WoodpeckerEntity.class, TrackedDataHandlerRegistry.BLOCK_POS);
    private static final TrackedData<Integer> PECK_CHAIN_LENGTH = DataTracker.registerData(WoodpeckerEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private static final TrackedData<Integer> PECK_CHAIN_TICKS = DataTracker.registerData(WoodpeckerEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private static final TrackedData<Integer> DRUMMING_TICKS = DataTracker.registerData(WoodpeckerEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private static final TrackedData<Integer> ANGER_TICKS = DataTracker.registerData(WoodpeckerEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private static final TrackedData<Byte> CHIRP_DELAY = DataTracker.registerData(WoodpeckerEntity.class, TrackedDataHandlerRegistry.BYTE);
    private static final TrackedData<Boolean> TAME = DataTracker.registerData(WoodpeckerEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    private static final UniformIntProvider ANGER_TIME_RANGE = TimeHelper.betweenSeconds(10, 15);
    @Nullable
    private UUID angryAt;

    private Direction clingSide;

    private int consecutivePecks;

    @Environment(value = EnvType.SERVER)
    private int cannotEnterNestTicks;
    @Nullable
    @Environment(value = EnvType.SERVER)
    private BlockPos nestPos;

    @Environment(value = EnvType.SERVER)
    private byte chirpCount;
    @Environment(value = EnvType.SERVER)
    private byte nextChirpCount;
    @Environment(value = EnvType.SERVER)
    private byte nextChirpSpeed;
    @Environment(value = EnvType.SERVER)
    private float nextChirpPitch;

    @Environment(value = EnvType.CLIENT)
    public float flapSpeed;
    @Environment(value = EnvType.CLIENT)
    public float prevFlapAngle;
    @Environment(value = EnvType.CLIENT)
    public float flapAngle;

    public WoodpeckerEntity(EntityType<? extends WoodpeckerEntity> entityType, World world) {
        super(entityType, world);

        this.ignoreCameraFrustum = true;
    }

    public static DefaultAttributeContainer.Builder createWoodpeckerAttributes() {
        return AnimalEntity.createMobAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 8.0D)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 3.0D)
                .add(EntityAttributes.GENERIC_FLYING_SPEED, 0.25D)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.25D);
    }

    @Override
    protected void initDataTracker() {
        super.initDataTracker();

        this.dataTracker.startTracking(CLING_POS, BlockPos.ORIGIN);
        this.dataTracker.startTracking(PECK_CHAIN_LENGTH, 0);
        this.dataTracker.startTracking(PECK_CHAIN_TICKS, 0);
        this.dataTracker.startTracking(DRUMMING_TICKS, 0);
        this.dataTracker.startTracking(ANGER_TICKS, 0);
        this.dataTracker.startTracking(CHIRP_DELAY, (byte) 0);
        this.dataTracker.startTracking(TAME, false);
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);

        nbt.put(CLING_POS_KEY, NbtHelper.fromBlockPos(this.getClingPos()));

        if (this.hasNestPos()) nbt.put(NEST_POS_KEY, NbtHelper.fromBlockPos(Objects.requireNonNull(this.getNestPos())));

        nbt.putBoolean(TAME_KEY, this.isTame());

        nbt.putInt(PLAY_SESSIONS_BEFORE_TAME_KEY, this.getPlaySessionsBeforeTame());
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);

        BlockPos clingPos = NbtHelper.toBlockPos(nbt.getCompound(CLING_POS_KEY));
        if (!this.isClinging() && !WondrousWildsUtils.isPosAtWorldOrigin(clingPos)) this.tryClingingTo(clingPos);

        if (nbt.contains(NEST_POS_KEY)) this.setNestPos(NbtHelper.toBlockPos(nbt.getCompound(NEST_POS_KEY)));

        this.setTame(nbt.getBoolean(TAME_KEY));

        this.setPlaySessionsBeforeTame(nbt.getInt(PLAY_SESSIONS_BEFORE_TAME_KEY));
    }

    @Override
    public EntityData initialize(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason, @Nullable EntityData entityData, @Nullable NbtCompound entityNbt) {
        if (this.getPlaySessionsBeforeTame() <= 0 && !this.isTame()) this.setPlaySessionsBeforeTame(this.getRandom().nextBetween(5, 15));

        this.initEquipment(world.getRandom(), difficulty);

        return super.initialize(world, difficulty, spawnReason, entityData, entityNbt);
    }

    @Override
    protected void initEquipment(Random random, LocalDifficulty localDifficulty) {
        if (random.nextInt(4) != 0) return;

        // ITEM CHANCES:
        // 1% Wooden Axe
        // 4% Music Disc
        // 8% Glass Bottle
        // 12% Honeycomb
        // 18% Bone Meal
        // 24% Flower
        // 33% Seeds
        Item itemToHold;
        int i = 1 + random.nextInt(100);
        if (i <= 1) itemToHold = Items.WOODEN_AXE;
        else if (i <= 5) itemToHold = Items.MUSIC_DISC_OTHERSIDE;
        else if (i <= 13) itemToHold = Items.GLASS_BOTTLE;
        else if (i <= 25) itemToHold = Items.HONEYCOMB;
        else if (i <= 43) itemToHold = Items.BONE_MEAL;
        else if (i <= 67) itemToHold = switch (random.nextInt(5)) {
            default -> Items.LILY_OF_THE_VALLEY;
            case 1 -> WondrousWildsItems.PURPLE_VIOLET;
            case 2 -> WondrousWildsItems.PINK_VIOLET;
            case 3 -> WondrousWildsItems.RED_VIOLET;
            case 4 -> WondrousWildsItems.WHITE_VIOLET;
        };
        else itemToHold = switch (random.nextInt(4)) {
            default -> Items.WHEAT_SEEDS;
            case 1 -> Items.BEETROOT_SEEDS;
            case 2 -> Items.PUMPKIN_SEEDS;
            case 3 -> Items.MELON_SEEDS;
        };

        this.equipStack(EquipmentSlot.MAINHAND, new ItemStack(itemToHold));
    }

    @Override
    public void setFlying(boolean flying) {
        super.setFlying(flying);

        if (flying && this.isClinging()) this.stopClinging();
    }

    public BlockPos getClingPos() {
        return this.dataTracker.get(CLING_POS);
    }

    public void setClingPos(BlockPos pos) {
        this.dataTracker.set(CLING_POS, pos);
    }

    public boolean isClinging() {
        BlockPos clingPos = this.dataTracker.get(CLING_POS);
        return clingPos != null && !WondrousWildsUtils.isPosAtWorldOrigin(clingPos);
    }

    public boolean tryClingingTo(BlockPos clingPos) {
        Direction clingSide = Direction.fromHorizontal(this.getRandom().nextInt(4));
        double closestSideDistance = 100.0D;
        for (Direction side : HORIZONTAL_DIRECTIONS) {
            BlockPos offsetPos = clingPos.offset(side);
            if (!this.getWorld().isAir(offsetPos) || !this.getWorld().getBlockState(clingPos).isSideSolidFullSquare(this.getWorld(), clingPos, side)) continue;

            double distanceFromSide = this.getBlockPos().getSquaredDistance(offsetPos);
            if (distanceFromSide < closestSideDistance) {
                clingSide = side;
                closestSideDistance = distanceFromSide;
            }
        }
        if (closestSideDistance == 100.0D) return false;

        this.setClingPos(clingPos);
        this.clingSide = clingSide;

        BlockPos pos = clingPos.offset(clingSide);
        this.setPosition(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D);

        this.setFlying(false);

        return true;
    }

    public void stopClinging() {
        this.setClingPos(BlockPos.ORIGIN);

        if (this.isPecking()) this.stopPecking(true);
        else this.resetConsecutivePecks();
    }

    public boolean hasValidClingPos() {
        return this.canClingToPos(this.getClingPos(), false, this.clingSide) && this.getWorld().isAir(this.getBlockPos());
    }

    public boolean canClingToPos(BlockPos pos, boolean checkForSpace, @Nullable Direction... sidesToCheck) {
        Direction[] directionsToCheck = sidesToCheck != null ? sidesToCheck : HORIZONTAL_DIRECTIONS;

        if (checkForSpace) {
            boolean hasOpenSpace = false;
            for (Direction direction : directionsToCheck) {
                if (this.getWorld().isAir(pos.offset(direction))) {
                    hasOpenSpace = true;
                    break;
                }
            }
            if (!hasOpenSpace) return false;
        }

        BlockState state = this.getWorld().getBlockState(pos);

        boolean hasSolidSide = false;
        for (Direction direction : directionsToCheck) {
            if (state.isSideSolidFullSquare(this.getWorld(), pos, direction)) {
                hasSolidSide = true;
                break;
            }
        }
        if (!hasSolidSide) return false;

        return (state.getBlock() instanceof PillarBlock && state.isIn(BlockTags.OVERWORLD_NATURAL_LOGS) && state.get(PillarBlock.AXIS).isVertical()) || state.isIn(WondrousWildsTags.BlockTags.WOODPECKERS_INTERACT_WITH);
    }

    public boolean canMakeNestInPos(BlockPos pos) {
        Block block = this.getWorld().getBlockState(pos).getBlock();
        return TREE_HOLLOW_MAP.containsKey(block);
    }

    public boolean canInteractWithPos(BlockPos pos) {
        return this.getWorld().getBlockState(pos).isIn(WondrousWildsTags.BlockTags.WOODPECKERS_INTERACT_WITH);
    }

    public boolean isMakingNest() {
        return this.canMakeNestInPos(this.getClingPos()) && (this.getConsecutivePecks() > 0 || this.isPecking());
    }

    public boolean isPecking() {
        return this.getCurrentPeckChainLength() > 0;
    }

    public int getCurrentPeckChainLength() {
        return this.dataTracker.get(PECK_CHAIN_LENGTH);
    }

    public void setPeckChainLength(int length) {
        this.dataTracker.set(PECK_CHAIN_LENGTH, length);
    }

    public int getPeckChainTicks() {
        return this.dataTracker.get(PECK_CHAIN_TICKS);
    }

    public void setPeckChainTicks(int ticks) {
        this.dataTracker.set(PECK_CHAIN_TICKS, ticks);
    }

    public int calculateTicksForPeckChain(int chainLength) {
        return 10 + (10 * chainLength);
    }

    public void startPeckChain(int length) {
        this.setPeckChainLength(length);
        this.setPeckChainTicks(this.calculateTicksForPeckChain(length));
    }

    public void stopPecking(boolean resetConsecutive) {
        this.setPeckChainLength(0);
        this.setPeckChainTicks(0);

        if (resetConsecutive) this.resetConsecutivePecks();
    }

    public int getConsecutivePecks() {
        return this.consecutivePecks;
    }

    public void setConsecutivePecks(int pecks) {
        this.consecutivePecks = pecks;
    }

    public void resetConsecutivePecks() {
        this.setConsecutivePecks(0);
    }

    public double getPeckReach() {
        return 1.0D;
    }

    public byte getChirpDelay() {
        return this.dataTracker.get(CHIRP_DELAY);
    }

    public void setChirpDelay(byte speed) {
        this.dataTracker.set(CHIRP_DELAY, speed);
    }

    public void startChirping(byte count, byte speed) {
        this.chirpCount = (byte) 0;
        this.nextChirpCount = count;

        this.nextChirpSpeed = speed;
        this.setChirpDelay(speed);

        this.nextChirpPitch = this.getSoundPitch();
    }

    public void stopChirping() {
        this.chirpCount = (byte) 0;
        this.nextChirpCount = (byte) 0;

        this.nextChirpSpeed = (byte) 0;
        this.setChirpDelay((byte) 0);
    }

    public int getPlaySessionsBeforeTame() {
        return this.playSessionsBeforeTame;
    }

    public void setPlaySessionsBeforeTame(int sessions) {
        this.playSessionsBeforeTame = sessions;
    }

    public void progressTame() {
        if (this.getPlaySessionsBeforeTame() <= 1) this.finishTame();
        else {
            this.setPlaySessionsBeforeTame(this.getPlaySessionsBeforeTame() - 1);
            this.showTameParticles(false);
        }

        this.resetConsecutivePecks();
    }

    public void finishTame() {
        this.setTame(true);
        this.showTameParticles(true);
    }

    public boolean isTame() {
        return this.dataTracker.get(TAME);
    }

    public void setTame(boolean tame) {
        this.dataTracker.set(TAME, tame);
    }

    public int getDrummingTicks() {
        return this.dataTracker.get(DRUMMING_TICKS);
    }

    public void setDrummingTicks(int ticks) {
        this.dataTracker.set(DRUMMING_TICKS, ticks);
    }

    public boolean isDrumming() {
        return this.getDrummingTicks() > 0;
    }

    public void startDrumming() {
        this.setDrummingTicks(55);
    }

    @Override
    public int getNestCapacityWeight() {
        return this.isBaby() ? 15 : WondrousWildsEntities.DEFAULT_NESTER_CAPACITY_WEIGHTS.get(this.getType());
    }

    @Nullable
    @Override
    public BlockPos getNestPos() {
        return this.nestPos;
    }

    @Override
    public void setNestPos(@Nullable BlockPos pos) {
        this.nestPos = pos;
    }

    @Override
    public int getMinTicksInNest() {
        return 200;
    }

    @Override
    public int getMinTicksOutOfNest() {
        return 400;
    }

    @Override
    public int getCannotInhabitNestTicks() {
        if (this.getWorld().isClient()) return 0;

        return this.cannotEnterNestTicks;
    }

    @Override
    public void setCannotInhabitNestTicks(int ticks) {
        if (this.getWorld().isClient()) return;

        this.cannotEnterNestTicks = ticks;
    }

    @Override
    public boolean shouldReturnToNest() {
        if (this.getCannotInhabitNestTicks() > 0 || !this.hasNestPos()) return false;

        return this.getWorld().isNight() || this.getWorld().isRaining();
    }

    @Override
    public boolean defendsNest() {
        return true;
    }

    @Override
    public int getWanderRadiusFromNest() {
        return 64;
    }

    @Override
    public int getAngerTime() {
        return this.dataTracker.get(ANGER_TICKS);
    }

    @Override
    public void setAngerTime(int ticks) {
        this.dataTracker.set(ANGER_TICKS, ticks);
    }

    @Nullable
    @Override
    public UUID getAngryAt() {
        return this.angryAt;
    }

    @Override
    public void setAngryAt(@Nullable UUID angryAt) {
        this.angryAt = angryAt;
    }

    @Override
    public void chooseRandomAngerTime() {
        this.setAngerTime(ANGER_TIME_RANGE.get(this.getRandom()));
    }

    public boolean canWander() {
        return !this.isClinging() && !this.hasAttackTarget() && this.getAttacker() == null;
    }

    public boolean hasAttackTarget() {
        return this.getTarget() != null;
    }

    @Override
    protected void initGoals() {
        this.goalSelector.add(0, new SwimGoal(this));
        this.goalSelector.add(1, new EscapeDangerGoal(this, 1.5D));
        this.goalSelector.add(2, new FleeEntityGoal<>(this, WoodpeckerEntity.class, 24.0F, 1.0D, 1.5D, entity -> AVOID_WOODPECKER_PREDICATE.test((WoodpeckerEntity) entity)));
        this.goalSelector.add(3, new WoodpeckerAttackGoal(this, 1.0D, true));
        this.goalSelector.add(4, new FleeEntityGoal<>(this, PlayerEntity.class, 16.0F, 1.0D, 1.5D, entity -> !this.isTame()));
        this.goalSelector.add(5, new FindOrReturnToBlockNestGoal(this, 1.0D, 24, 24));
        this.goalSelector.add(6, new WoodpeckerPlayWithBlockGoal(this, 1.0D, 24, 24));
        this.goalSelector.add(7, new WoodpeckerClingToLogGoal(this, 1.0D, 24, 24));
        this.goalSelector.add(8, new WoodpeckerWanderLandGoal(this, 1.0D));
        this.goalSelector.add(8, new WoodpeckerWanderFlyingGoal(this));
        this.goalSelector.add(9, new LookAtEntityGoal(this, PlayerEntity.class, 16.0F));
        this.goalSelector.add(9, new LookAtEntityGoal(this, MobEntity.class, 16.0F));
        this.goalSelector.add(10, new LookAroundGoal(this));
    }

    @Nullable
    @Override
    public PassiveEntity createChild(ServerWorld world, PassiveEntity entity) {
        return null;
    }

    @Override
    public void tick() {
        super.tick();

        if (this.getWorld().isClient()) {
            this.flapSpeed = MathHelper.clamp(1.0F - (this.limbDistance * 0.5F), 0.0F, 1.0F);
            this.prevFlapAngle = this.flapAngle;
            this.flapAngle += this.flapSpeed;
        }
        else {
            if (this.hasNestPos() && !(this.getWorld().getBlockEntity(this.getNestPos()) instanceof InhabitableNestBlockEntity)) this.clearNestPos();

            if (this.isPecking()) {
                if (this.getPeckChainTicks() <= 0) this.stopPecking(false);

                else {
                    if (this.getPeckChainTicks() % 10 == 0 && this.getPeckChainTicks() != this.calculateTicksForPeckChain(this.getCurrentPeckChainLength())) {
                        SoundEvent peckSound = null;

                        if (this.isAttacking() && this.hasAttackTarget()) {
                            LivingEntity attackTarget = this.getTarget();
                            double distanceFromTarget = this.squaredDistanceTo(attackTarget.getX(), attackTarget.getY(), attackTarget.getZ());

                            if (distanceFromTarget <= this.getPeckReach()) this.tryAttack(attackTarget);
                        }
                        else if (this.isClinging() && this.canMakeNestInPos(this.getClingPos()) && this.hasValidClingPos()) {
                            BlockState peckState = this.getWorld().getBlockState(this.getClingPos());

                            this.setConsecutivePecks(this.getConsecutivePecks() + 1);
                            if (this.getConsecutivePecks() >= PECKS_NEEDED_FOR_NEST) {
                                this.stopPecking(true);

                                Block clingBlock = peckState.getBlock();
                                this.getWorld().setBlockState(this.getClingPos(), TREE_HOLLOW_MAP.get(clingBlock).getDefaultState().with(TreeHollowBlock.FACING, this.clingSide));
                            }

                            PacketByteBuf buf = PacketByteBufs.create();
                            buf.writeBlockPos(this.getClingPos());
                            buf.writeEnumConstant(this.clingSide);
                            for (ServerPlayerEntity receiver : PlayerLookup.tracking(this)) ServerPlayNetworking.send(receiver, WoodpeckerDrillPacket.ID, buf);

                            peckSound = peckState.getSoundGroup().getHitSound();
                        }
                        else {
                            BlockHitResult hitResult = (BlockHitResult) this.raycast(this.getPeckReach(), 0.0F, false);
                            BlockState peckState = this.getWorld().getBlockState(hitResult.getBlockPos());

                            if (peckState.isIn(WondrousWildsTags.BlockTags.WOODPECKERS_INTERACT_WITH)) {
                                WoodpeckerFakePlayer fakePlayer = new WoodpeckerFakePlayer(this);
                                Hand hand = Hand.MAIN_HAND;

                                boolean successfulInteraction;

                                if (!(successfulInteraction = (peckState.onUse(this.getWorld(), fakePlayer, hand, hitResult)).isAccepted())) {
                                    ItemStack heldItem = fakePlayer.getMainHandStack();
                                    if (!heldItem.isEmpty()) {
                                        successfulInteraction = heldItem.useOnBlock(new ItemUsageContext(fakePlayer, hand, hitResult)).isAccepted();
                                    }
                                }

                                if (successfulInteraction) this.setConsecutivePecks(this.getConsecutivePecks() + 1);
                            }

                            peckSound = peckState.getSoundGroup().getHitSound();
                        }

                        if (peckSound != null) this.playSound(peckSound, 0.75F, 1.5F);
                    }

                    this.setPeckChainTicks(this.getPeckChainTicks() - 1);

                    if (this.getCannotInhabitNestTicks() > 0) this.setCannotInhabitNestTicks(this.getCannotInhabitNestTicks() - 1);
                }
            }

            if (this.isClinging()) {
                boolean shouldInteract = this.canInteractWithPos(this.getClingPos());
                boolean hasValidClingPos = this.hasValidClingPos();

                if (!this.isPecking()) {
                    if (!this.isDrumming()) {
                        boolean canMakeNest = this.canMakeNestInPos(this.getClingPos());
                        if (shouldInteract || (canMakeNest && this.shouldFindNest())) {
                            if (this.getRandom().nextInt(shouldInteract ? 40 : 20) == 0 && hasValidClingPos) {
                                int randomLength = 1 + this.getRandom().nextInt(4);
                                this.startPeckChain(canMakeNest ? Math.min(randomLength, PECKS_NEEDED_FOR_NEST - this.getConsecutivePecks()) : randomLength);
                            }
                        }
                        else if (this.hasNestPos() && this.getRandom().nextInt(400) == 0) this.startDrumming();
                    }
                }

                boolean naturallyUncling = !this.isDrumming() && !this.isMakingNest() && (this.getRandom().nextInt(shouldInteract ? 200 : 800) == 0 || this.shouldReturnToNest());
                if (naturallyUncling || !hasValidClingPos) {
                    if (shouldInteract && naturallyUncling && !this.isTame() && this.getConsecutivePecks() > 0) this.progressTame();
                    this.setFlying(true);
                }
                else {
                    this.setYaw(this.clingSide.getOpposite().getHorizontal() * 90.0F);
                    this.setBodyYaw(this.getYaw());

                    if (this.isPecking() || this.isDrumming()) this.setHeadYaw(this.getYaw());
                }
            }

            if (!this.isDrumming()) {
                if (!this.isPecking()) {
                    if (this.nextChirpSpeed == (byte) 0) {
                        if (this.getRandom().nextInt(120) == 0) this.startChirping((byte) (1 + this.getRandom().nextInt(12)), (byte) (2 + this.getRandom().nextInt(3)));
                    }
                    else {
                        if (this.getChirpDelay() > 0) {
                            if (this.getChirpDelay() == 2) {
                                ++this.chirpCount;
                                this.playSound(WondrousWildsSounds.WOODPECKER_CHIRP, this.getSoundVolume(), this.nextChirpPitch);
                            }

                            this.setChirpDelay((byte) (this.getChirpDelay() - 1));
                        }
                        else {
                            this.setChirpDelay(this.nextChirpSpeed);

                            if (this.chirpCount >= this.nextChirpCount) this.stopChirping();
                        }
                    }
                }
            }
            else {
                if (this.getDrummingTicks() == 45) this.playSound(WondrousWildsSounds.WOODPECKER_DRUM, 4.0F, 1.0F);

                this.setDrummingTicks(this.getDrummingTicks() - 1);
            }

            this.tickAngerLogic((ServerWorld) this.getWorld(), false);
        }
    }

    @Override
    public ActionResult interactMob(PlayerEntity player, Hand hand) {
        ItemStack playerHeldStack = player.getStackInHand(hand);
        ItemStack woodpeckerHeldStack = this.getStackInHand(Hand.MAIN_HAND);

        if (this.isTame()) {
            if (hand == Hand.MAIN_HAND && playerHeldStack.isEmpty() && !woodpeckerHeldStack.isEmpty()) {
                ItemStack stackToTransfer = woodpeckerHeldStack.copy();

                if (player.isSneaking()) {
                    stackToTransfer.setCount(1);
                    woodpeckerHeldStack.decrement(1);
                }
                else this.setStackInHand(Hand.MAIN_HAND, ItemStack.EMPTY);

                if (!player.giveItemStack(stackToTransfer)) this.dropStack(stackToTransfer);

                return ActionResult.SUCCESS;
            }

            transferToWoodpecker: if (!playerHeldStack.isEmpty()) {
                int woodpeckerSpaceRemaining = woodpeckerHeldStack.getMaxCount() - woodpeckerHeldStack.getCount();
                if (woodpeckerSpaceRemaining <= 0) break transferToWoodpecker;

                boolean isNewItemType = playerHeldStack.getItem() != woodpeckerHeldStack.getItem();
                if (isNewItemType && !woodpeckerHeldStack.isEmpty()) break transferToWoodpecker;

                if (!player.isSneaking()) {
                    if (!isNewItemType) {
                        int amountToTransfer = Math.min(playerHeldStack.getCount(), woodpeckerSpaceRemaining);

                        if (!player.getAbilities().creativeMode) playerHeldStack.decrement(amountToTransfer);
                        woodpeckerHeldStack.increment(amountToTransfer);

                    }
                    else {
                        if (!player.getAbilities().creativeMode) player.setStackInHand(hand, ItemStack.EMPTY);
                        this.setStackInHand(Hand.MAIN_HAND, playerHeldStack.copy());
                    }
                    return ActionResult.SUCCESS;
                }

                ItemStack playerHeldStackCopy = playerHeldStack.copy();

                if (!player.getAbilities().creativeMode) playerHeldStack.decrement(1);

                if (isNewItemType) {
                    playerHeldStackCopy.setCount(1);
                    this.setStackInHand(Hand.MAIN_HAND, playerHeldStackCopy);
                }
                else woodpeckerHeldStack.increment(1);

                return ActionResult.SUCCESS;
            }
        }

        else if (playerHeldStack.getItem() == WondrousWildsItems.LOVIFIER) {
            if (!this.getWorld().isClient()) this.finishTame();
            return ActionResult.SUCCESS;
        }

        return super.interactMob(player, hand);
    }

    @Override
    public void equipStack(EquipmentSlot slot, ItemStack stack) {
        super.equipStack(slot, stack);

        this.updateDropChances(slot);
    }

    @Override
    public boolean damage(DamageSource source, float amount) {
        if (source.getAttacker() instanceof WoodpeckerEntity) amount = 0.0F;

        if (super.damage(source, amount)) {
            this.resetConsecutivePecks();

            if (!this.isFlying() && !this.isDrumming()) {
                this.setFlying(true);
            }

            return true;
        }
        else return false;
    }

    @Override
    public void travel(Vec3d movementInput) {
        if (!this.isFlying()) {
            super.travel(movementInput);
            return;
        }

        if (this.canMoveVoluntarily() || this.isLogicalSideForUpdatingMovement()) {
            if (this.isTouchingWater()) {
                this.updateVelocity(0.02F, movementInput);
                this.move(MovementType.SELF, this.getVelocity());
                this.setVelocity(this.getVelocity().multiply(0.8D));
            }
            else if (this.isInLava()) {
                this.updateVelocity(0.02F, movementInput);
                this.move(MovementType.SELF, this.getVelocity());
                this.setVelocity(this.getVelocity().multiply(0.5D));
            }
            else {
                this.updateVelocity(this.getMovementSpeed(), movementInput);
                this.move(MovementType.SELF, this.getVelocity());
                this.setVelocity(this.getVelocity().multiply(0.91D));
            }
        }

        this.updateLimbs(this, false);
    }

    @Override
    public boolean canMoveVoluntarily() {
        return super.canMoveVoluntarily() && !this.isClinging();
    }

    @Override
    public boolean handleFallDamage(float fallDistance, float damageMultiplier, DamageSource damageSource) {
        return false;
    }

    @Override
    public int getDespawnCounter() {
        return 0;
    }

    @Override
    protected float getActiveEyeHeight(EntityPose pose, EntityDimensions dimensions) {
        return dimensions.height * 0.95F;
    }

    @Override
    protected BodyControl createBodyControl() {
        return new RelaxedBodyControl(this);
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {}

    @Nullable
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
        AnimationController<WoodpeckerEntity> constantController = new AnimationController<>(this, "constantController", 2, this::constantAnimationPredicate);
        AnimationController<WoodpeckerEntity> overlapController = new AnimationController<>(this, "overlapController", 0, this::overlapAnimationPredicate);
        AnimationController<WoodpeckerEntity> animationController = new AnimationController<>(this, "animationController", 2, this::animationPredicate);

        animationData.addAnimationController(constantController);
        animationData.addAnimationController(overlapController);
        animationData.addAnimationController(animationController);
    }

    private <E extends IAnimatable> PlayState constantAnimationPredicate(AnimationEvent<E> event) {
        if (this.isFlying())
            event.getController().setAnimation(new AnimationBuilder().addAnimation("flyingConstant"));

        else if (this.isClinging())
            event.getController().setAnimation(new AnimationBuilder().addAnimation("clingingConstant"));

        else if (this.isOnGround())
            event.getController().setAnimation(new AnimationBuilder().addAnimation("groundedConstant"));

        else
            event.getController().setAnimation(new AnimationBuilder().addAnimation("empty"));

        return PlayState.CONTINUE;
    }

    private <E extends IAnimatable> PlayState overlapAnimationPredicate(AnimationEvent<E> event) {
        if (this.isPecking())
            event.getController().setAnimation(new AnimationBuilder().addAnimation(this.getPeckAnimationToPlay()));

        else if (this.getChirpDelay() > 0 && this.getChirpDelay() <= 2)
            event.getController().setAnimation(new AnimationBuilder().addAnimation("chirpOverlap"));

        else
            event.getController().setAnimation(new AnimationBuilder().addAnimation("empty"));

        return PlayState.CONTINUE;
    }

    private String getPeckAnimationToPlay() {
        return switch (this.getCurrentPeckChainLength()) {
            case 1 -> "peck1Overlap";
            case 2 -> "peck2Overlap";
            case 3 -> "peck3Overlap";
            case 4 -> "peck4Overlap";
            default -> "empty";
        };
    }

    private <E extends IAnimatable> PlayState animationPredicate(AnimationEvent<E> event) {
        if (this.isFlying() && this.limbDistance >= 0.9F)
            event.getController().setAnimation(new AnimationBuilder().addAnimation("flap"));

        else if (this.isDrumming() && this.isClinging())
            event.getController().setAnimation(new AnimationBuilder().addAnimation("drum"));

        else
            event.getController().setAnimation(new AnimationBuilder().addAnimation("empty"));

        return PlayState.CONTINUE;
    }

    private void showTameParticles(boolean positive) {
        DefaultParticleType particleEffect = ParticleTypes.SMOKE;
        if (positive) particleEffect = ParticleTypes.HEART;

        if (!this.getWorld().isClient() && this.getWorld() instanceof ServerWorld serverWorld) {
            serverWorld.spawnParticles(particleEffect, this.getX(), this.getEyeY(), this.getZ(), 10, 0.25D, 0.25D, 0.25D, 0.0D);
        }

        else for (int i = 0; i < 7; ++i) {
            double d = this.getRandom().nextGaussian() * 0.02D;
            double e = this.getRandom().nextGaussian() * 0.02D;
            double f = this.getRandom().nextGaussian() * 0.02D;

            this.getWorld().addParticle(particleEffect, this.getParticleX(1.0D), this.getRandomBodyY() + 0.5D, this.getParticleZ(1.0D), d, e, f);
        }
    }
}
