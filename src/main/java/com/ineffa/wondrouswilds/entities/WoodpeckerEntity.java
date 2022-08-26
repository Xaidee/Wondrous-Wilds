package com.ineffa.wondrouswilds.entities;

import com.ineffa.wondrouswilds.blocks.TreeHollowBlock;
import com.ineffa.wondrouswilds.blocks.entity.TreeHollowBlockEntity;
import com.ineffa.wondrouswilds.entities.ai.*;
import com.ineffa.wondrouswilds.registry.*;
import com.ineffa.wondrouswilds.util.WondrousWildsUtils;
import com.ineffa.wondrouswilds.util.fakeplayer.WoodpeckerFakePlayer;
import io.netty.buffer.Unpooled;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.TimeUtil;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.BodyRotationControl;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
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

public class WoodpeckerEntity extends FlyingAndWalkingAnimalEntity implements BlockNester, NeutralMob, IAnimatable {

    public static final String CLING_POS_KEY = "ClingPos";
    public static final String NEST_POS_KEY = "NestPos";
    public static final String PLAY_SESSIONS_BEFORE_TAME_KEY = "PlaySessionsBeforeTame";
    public static final String TAME_KEY = "Tame";

    public static final int PECKS_NEEDED_FOR_NEST = 200;

    private int playSessionsBeforeTame;

    private final Predicate<WoodpeckerEntity> AVOID_WOODPECKER_PREDICATE = otherWoodpecker -> this.getLastHurtByMob() == otherWoodpecker;

    private static final EntityDataAccessor<BlockPos> CLING_POS = SynchedEntityData.defineId(WoodpeckerEntity.class, EntityDataSerializers.BLOCK_POS);
    private static final EntityDataAccessor<Integer> PECK_CHAIN_LENGTH = SynchedEntityData.defineId(WoodpeckerEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> PECK_CHAIN_TICKS = SynchedEntityData.defineId(WoodpeckerEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DRUMMING_TICKS = SynchedEntityData.defineId(WoodpeckerEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> ANGER_TICKS = SynchedEntityData.defineId(WoodpeckerEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Byte> CHIRP_DELAY = SynchedEntityData.defineId(WoodpeckerEntity.class, EntityDataSerializers.BYTE);
    private static final EntityDataAccessor<Boolean> TAME = SynchedEntityData.defineId(WoodpeckerEntity.class, EntityDataSerializers.BOOLEAN);
    private static final UniformInt ANGER_TIME_RANGE = TimeUtil.rangeOfSeconds(10, 15);
    @Nullable
    private UUID angryAt;

    private Direction clingSide;

    private int consecutivePecks;

    @OnlyIn(Dist.DEDICATED_SERVER)
    private int cannotEnterNestTicks;
    @Nullable
    @OnlyIn(Dist.DEDICATED_SERVER)
    private BlockPos nestPos;

    @OnlyIn(Dist.DEDICATED_SERVER)
    private byte chirpCount;
    @OnlyIn(Dist.DEDICATED_SERVER)
    private byte nextChirpCount;
    @OnlyIn(Dist.DEDICATED_SERVER)
    private byte nextChirpSpeed;
    @OnlyIn(Dist.DEDICATED_SERVER)
    private float nextChirpPitch;

    @OnlyIn(Dist.CLIENT)
    public float flapSpeed;
    @OnlyIn(Dist.CLIENT)
    public float prevFlapAngle;
    @OnlyIn(Dist.CLIENT)
    public float flapAngle;

    public WoodpeckerEntity(EntityType<? extends WoodpeckerEntity> entityType, Level world) {
        super(entityType, world);

        //this.ignoreCameraFrustum = true;
    }

    public static AttributeSupplier.Builder createWoodpeckerAttributes() {
        return Animal.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 8.0D)
                .add(Attributes.ATTACK_DAMAGE, 3.0D)
                .add(Attributes.FLYING_SPEED, 0.25D)
                .add(Attributes.MOVEMENT_SPEED, 0.25D);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();

        this.entityData.define(CLING_POS, BlockPos.ZERO);
        this.entityData.define(PECK_CHAIN_LENGTH, 0);
        this.entityData.define(PECK_CHAIN_TICKS, 0);
        this.entityData.define(DRUMMING_TICKS, 0);
        this.entityData.define(ANGER_TICKS, 0);
        this.entityData.define(CHIRP_DELAY, (byte) 0);
        this.entityData.define(TAME, false);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag nbt) {
        super.addAdditionalSaveData(nbt);

        nbt.put(CLING_POS_KEY, NbtUtils.writeBlockPos(this.getClingPos()));

        if (this.hasNestPos()) nbt.put(NEST_POS_KEY, NbtUtils.writeBlockPos(Objects.requireNonNull(this.getNestPos())));

        nbt.putBoolean(TAME_KEY, this.isTame());

        nbt.putInt(PLAY_SESSIONS_BEFORE_TAME_KEY, this.getPlaySessionsBeforeTame());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag nbt) {
        super.readAdditionalSaveData(nbt);

        BlockPos clingPos = NbtUtils.readBlockPos(nbt.getCompound(CLING_POS_KEY));
        if (!this.isClinging() && !clingPos.equals(BlockPos.ZERO)) this.tryClingingTo(clingPos);

        if (nbt.contains(NEST_POS_KEY)) this.setNestPos(NbtUtils.readBlockPos(nbt.getCompound(NEST_POS_KEY)));

        this.setTame(nbt.getBoolean(TAME_KEY));

        this.setPlaySessionsBeforeTame(nbt.getInt(PLAY_SESSIONS_BEFORE_TAME_KEY));
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor world, DifficultyInstance difficulty, MobSpawnType spawnReason, @Nullable SpawnGroupData entityData, @Nullable CompoundTag entityNbt) {
        if (this.getPlaySessionsBeforeTame() <= 0 && !this.isTame()) this.setPlaySessionsBeforeTame(this.getRandom().nextIntBetweenInclusive(5, 15));

        this.populateDefaultEquipmentSlots(world.getRandom(), difficulty);

        return super.finalizeSpawn(world, difficulty, spawnReason, entityData, entityNbt);
    }

    @Override
    protected void populateDefaultEquipmentSlots(RandomSource random, DifficultyInstance localDifficulty) {
        if (random.nextInt(4) != 0) return;

        // ITEM CHANCES:
        // 1% Wooden Axe
        // 4% Music Disc
        // 8% Glass Bottle
        // 12% Honeycomb
        // 18% Bone Meal
        // 24% Flower
        // 33% Seeds
        ItemLike itemToHold;
        int i = 1 + random.nextInt(100);
        if (i <= 1) itemToHold = Items.WOODEN_AXE;
        else if (i <= 5) itemToHold = Items.MUSIC_DISC_OTHERSIDE;
        else if (i <= 13) itemToHold = Items.GLASS_BOTTLE;
        else if (i <= 25) itemToHold = Items.HONEYCOMB;
        else if (i <= 43) itemToHold = Items.BONE_MEAL;
        else if (i <= 67) itemToHold = switch (random.nextInt(5)) {
            default -> Items.LILY_OF_THE_VALLEY;
            case 1 -> WondrousWildsBlocks.PURPLE_VIOLET.get();
            case 2 -> WondrousWildsBlocks.PINK_VIOLET.get();
            case 3 -> WondrousWildsBlocks.RED_VIOLET.get();
            case 4 -> WondrousWildsBlocks.WHITE_VIOLET.get();
        };
        else itemToHold = switch (random.nextInt(4)) {
            default -> Items.WHEAT_SEEDS;
            case 1 -> Items.BEETROOT_SEEDS;
            case 2 -> Items.PUMPKIN_SEEDS;
            case 3 -> Items.MELON_SEEDS;
        };

        this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(itemToHold));
    }

    @Override
    public void setFlying(boolean flying) {
        super.setFlying(flying);

        if (flying && this.isClinging()) this.stopClinging();
    }

    public BlockPos getClingPos() {
        return this.entityData.get(CLING_POS);
    }

    public void setClingPos(BlockPos pos) {
        this.entityData.set(CLING_POS, pos);
    }

    public boolean isClinging() {
        BlockPos clingPos = this.entityData.get(CLING_POS);
        return clingPos != null && !WondrousWildsUtils.isPosAtWorldOrigin(clingPos);
    }

    public boolean tryClingingTo(BlockPos clingPos) {
        Direction clingSide = Direction.from2DDataValue(this.getRandom().nextInt(4));
        double closestSideDistance = 100.0D;
        for (Direction side : HORIZONTAL_DIRECTIONS) {
            BlockPos offsetPos = clingPos.relative(side);
            if (!this.getLevel().isEmptyBlock(offsetPos) || !this.getLevel().getBlockState(clingPos).isFaceSturdy(this.getLevel(), clingPos, side)) continue;

            double distanceFromSide = this.getOnPos().distSqr(offsetPos);
            if (distanceFromSide < closestSideDistance) {
                clingSide = side;
                closestSideDistance = distanceFromSide;
            }
        }
        if (closestSideDistance == 100.0D) return false;

        this.setClingPos(clingPos);
        this.clingSide = clingSide;

        BlockPos pos = clingPos.relative(clingSide);
        this.setPos(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D);

        this.setFlying(false);

        return true;
    }

    public void stopClinging() {
        this.setClingPos(BlockPos.ZERO);

        if (this.isPecking()) this.stopPecking(true);
        else this.resetConsecutivePecks();
    }

    public boolean hasValidClingPos() {
        return this.canClingToPos(this.getClingPos(), false, this.clingSide) && this.getLevel().isEmptyBlock(this.getOnPos());
    }

    public boolean canClingToPos(BlockPos pos, boolean checkForSpace, @Nullable Direction... sidesToCheck) {
        Direction[] directionsToCheck = sidesToCheck != null ? sidesToCheck : HORIZONTAL_DIRECTIONS;

        if (checkForSpace) {
            boolean hasOpenSpace = false;
            for (Direction direction : directionsToCheck) {
                if (this.getLevel().isEmptyBlock(pos.relative(direction))) {
                    hasOpenSpace = true;
                    break;
                }
            }
            if (!hasOpenSpace) return false;
        }

        BlockState state = this.getLevel().getBlockState(pos);

        boolean hasSolidSide = false;
        for (Direction direction : directionsToCheck) {
            if (state.isFaceSturdy(this.getLevel(), pos, direction)) {
                hasSolidSide = true;
                break;
            }
        }
        if (!hasSolidSide) return false;

        return (state.getBlock() instanceof RotatedPillarBlock && state.is(BlockTags.OVERWORLD_NATURAL_LOGS) && state.getValue(RotatedPillarBlock.AXIS).isVertical()) || state.is(WondrousWildsTags.Blocks.WOODPECKERS_INTERACT_WITH);
    }

    public boolean canMakeNestInPos(BlockPos pos) {
        Block block = this.getLevel().getBlockState(pos).getBlock();
        return TREE_HOLLOW_MAP.containsKey(block);
    }

    public boolean canInteractWithPos(BlockPos pos) {
        return this.getLevel().getBlockState(pos).is(WondrousWildsTags.Blocks.WOODPECKERS_INTERACT_WITH);
    }

    public boolean isMakingNest() {
        return this.canMakeNestInPos(this.getClingPos()) && (this.getConsecutivePecks() > 0 || this.isPecking());
    }

    public boolean isPecking() {
        return this.getCurrentPeckChainLength() > 0;
    }

    public int getCurrentPeckChainLength() {
        return this.entityData.get(PECK_CHAIN_LENGTH);
    }

    public void setPeckChainLength(int length) {
        this.entityData.set(PECK_CHAIN_LENGTH, length);
    }

    public int getPeckChainTicks() {
        return this.entityData.get(PECK_CHAIN_TICKS);
    }

    public void setPeckChainTicks(int ticks) {
        this.entityData.set(PECK_CHAIN_TICKS, ticks);
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
        return this.entityData.get(CHIRP_DELAY);
    }

    public void setChirpDelay(byte speed) {
        this.entityData.set(CHIRP_DELAY, speed);
    }

    public void startChirping(byte count, byte speed) {
        this.chirpCount = (byte) 0;
        this.nextChirpCount = count;

        this.nextChirpSpeed = speed;
        this.setChirpDelay(speed);

        this.nextChirpPitch = this.getVoicePitch();
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
        return this.entityData.get(TAME);
    }

    public void setTame(boolean tame) {
        this.entityData.set(TAME, tame);
    }

    public int getDrummingTicks() {
        return this.entityData.get(DRUMMING_TICKS);
    }

    public void setDrummingTicks(int ticks) {
        this.entityData.set(DRUMMING_TICKS, ticks);
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
        if (this.getLevel().isClientSide) return 0;

        return this.cannotEnterNestTicks;
    }

    @Override
    public void setCannotInhabitNestTicks(int ticks) {
        if (this.getLevel().isClientSide) return;

        this.cannotEnterNestTicks = ticks;
    }

    @Override
    public boolean shouldReturnToNest() {
        if (this.getCannotInhabitNestTicks() > 0 || !this.hasNestPos()) return false;

        return this.getLevel().isNight() || this.getLevel().isRaining();
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
    public int getRemainingPersistentAngerTime() {
        return this.entityData.get(ANGER_TICKS);
    }

    @Override
    public void setRemainingPersistentAngerTime(int ticks) {
        this.entityData.set(ANGER_TICKS, ticks);
    }

    @Nullable
    @Override
    public UUID getPersistentAngerTarget() {
        return this.angryAt;
    }

    @Override
    public void setPersistentAngerTarget(@Nullable UUID angryAt) {
        this.angryAt = angryAt;
    }

    @Override
    public void startPersistentAngerTimer() {
        this.setRemainingPersistentAngerTime(ANGER_TIME_RANGE.sample(this.getRandom()));
    }

    public boolean canWander() {
        return !this.isClinging() && !this.hasAttackTarget() && this.getLastHurtByMob() == null;
    }

    public boolean hasAttackTarget() {
        return this.getTarget() != null;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new PanicGoal(this, 1.5D));
        this.goalSelector.addGoal(2, new AvoidEntityGoal<>(this, WoodpeckerEntity.class, 24.0F, 1.0D, 1.5D, entity -> AVOID_WOODPECKER_PREDICATE.test((WoodpeckerEntity) entity)));
        this.goalSelector.addGoal(3, new AvoidEntityGoal<>(this, Player.class, 16.0F, 1.0D, 1.5D, entity -> !this.isTame()));
        this.goalSelector.addGoal(4, new WoodpeckerAttackGoal(this, 1.0D, true));
        this.goalSelector.addGoal(5, new FindOrReturnToBlockNestGoal(this, 1.0D, 24, 24));
        this.goalSelector.addGoal(6, new WoodpeckerPlayWithBlockGoal(this, 1.0D, 24, 24));
        this.goalSelector.addGoal(7, new WoodpeckerClingToLogGoal(this, 1.0D, 24, 24));
        this.goalSelector.addGoal(8, new WoodpeckerWanderLandGoal(this, 1.0D));
        this.goalSelector.addGoal(8, new WoodpeckerWanderFlyingGoal(this));
        this.goalSelector.addGoal(9, new LookAtPlayerGoal(this, Player.class, 16.0F));
        this.goalSelector.addGoal(9, new LookAtPlayerGoal(this, Mob.class, 16.0F));
        this.goalSelector.addGoal(10, new RandomLookAroundGoal(this));
    }

    @Nullable
    @Override
    public AgeableMob getBreedOffspring(ServerLevel world, AgeableMob entity) {
        return null;
    }

    @Override
    public void tick() {
        super.tick();

        if (this.getLevel().isClientSide) {
            this.flapSpeed = Mth.clamp(1.0F - (this.swingTime * 0.5F), 0.0F, 1.0F);
            this.prevFlapAngle = this.flapAngle;
            this.flapAngle += this.flapSpeed;
        }
        else {
            if (this.hasNestPos() && !(this.getLevel().getBlockEntity(this.getNestPos()) instanceof TreeHollowBlockEntity)) this.clearNestPos();

            if (this.isPecking()) {
                if (this.getPeckChainTicks() <= 0) this.stopPecking(false);

                else {
                    if (this.getPeckChainTicks() % 10 == 0 && this.getPeckChainTicks() != this.calculateTicksForPeckChain(this.getCurrentPeckChainLength())) {
                        SoundEvent peckSound = null;

                        if (this.hasAttackTarget() && this.isAngry()) {
                            LivingEntity attackTarget = this.getTarget();
                            double distanceFromTarget = this.distanceToSqr(attackTarget.getX(), attackTarget.getY(), attackTarget.getZ());

                            if (distanceFromTarget <= this.getPeckReach()) this.canAttack(attackTarget);
                        }
                        else if (this.isClinging() && this.canMakeNestInPos(this.getClingPos()) && this.hasValidClingPos()) {
                            BlockState peckState = this.getLevel().getBlockState(this.getClingPos());

                            this.setConsecutivePecks(this.getConsecutivePecks() + 1);
                            if (this.getConsecutivePecks() >= PECKS_NEEDED_FOR_NEST) {
                                this.stopPecking(true);

                                Block clingBlock = peckState.getBlock();
                                this.getLevel().setBlockAndUpdate(this.getClingPos(), TREE_HOLLOW_MAP.get(clingBlock).defaultBlockState().setValue(TreeHollowBlock.FACING, this.clingSide));
                            }

                            FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
                            buf.writeBlockPos(this.getClingPos());
                            buf.writeEnum(this.clingSide);
                            //for (ServerPlayer receiver : PlayerList.tracking(this)) send(WoodpeckerDrillPacket.ID, buf);

                            peckSound = peckState.getSoundType().getHitSound();
                        }
                        else {
                            BlockHitResult hitResult = (BlockHitResult) this.pick(this.getPeckReach(), 0.0F, false);
                            BlockState peckState = this.getLevel().getBlockState(hitResult.getBlockPos());

                            if (peckState.is(WondrousWildsTags.Blocks.WOODPECKERS_INTERACT_WITH)) {
                                WoodpeckerFakePlayer fakePlayer = new WoodpeckerFakePlayer(this);
                                InteractionHand hand = InteractionHand.MAIN_HAND;

                                boolean successfulInteraction;

                                if (!(successfulInteraction = (peckState.use(this.getLevel(), fakePlayer, hand, hitResult)).consumesAction())) {
                                    ItemStack heldItem = fakePlayer.getItemInHand(hand);
                                    if (!heldItem.isEmpty()) {
                                        successfulInteraction = heldItem.useOn(new UseOnContext(fakePlayer, hand, hitResult)).consumesAction();
                                    }
                                }

                                if (successfulInteraction) this.setConsecutivePecks(this.getConsecutivePecks() + 1);
                            }

                            peckSound = peckState.getSoundType().getHitSound();
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
                    this.setYRot(this.clingSide.getOpposite().get2DDataValue() * 90.0F);
                    this.setYBodyRot(this.getYRot());

                    if (this.isPecking() || this.isDrumming()) this.setYHeadRot(this.getYRot());
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
                                this.playSound(WondrousWildsSounds.WOODPECKER_CHIRP.get(), this.getSoundVolume(), this.nextChirpPitch);
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
                if (this.getDrummingTicks() == 45) this.playSound(WondrousWildsSounds.WOODPECKER_DRUM.get(), 4.0F, 1.0F);

                this.setDrummingTicks(this.getDrummingTicks() - 1);
            }

            this.updatePersistentAnger((ServerLevel) this.getLevel(), false);
        }
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack playerHeldStack = player.getItemInHand(hand);
        ItemStack woodpeckerHeldStack = this.getItemInHand(InteractionHand.MAIN_HAND);

        if (this.isTame()) {
            if (hand == InteractionHand.MAIN_HAND && playerHeldStack.isEmpty() && !woodpeckerHeldStack.isEmpty()) {
                ItemStack stackToTransfer = woodpeckerHeldStack.copy();

                if (player.isCrouching()) {
                    stackToTransfer.setCount(1);
                    woodpeckerHeldStack.shrink(1);
                }
                else this.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);

                if (!player.addItem(stackToTransfer)) this.spawnAtLocation(stackToTransfer);

                return InteractionResult.SUCCESS;
            }

            transferToWoodpecker: if (!playerHeldStack.isEmpty()) {
                int woodpeckerSpaceRemaining = woodpeckerHeldStack.getMaxStackSize() - woodpeckerHeldStack.getCount();
                if (woodpeckerSpaceRemaining <= 0) break transferToWoodpecker;

                boolean isNewItemType = playerHeldStack.getItem() != woodpeckerHeldStack.getItem();
                if (isNewItemType && !woodpeckerHeldStack.isEmpty()) break transferToWoodpecker;

                if (!player.isCrouching()) {
                    if (!isNewItemType) {
                        int amountToTransfer = Math.min(playerHeldStack.getCount(), woodpeckerSpaceRemaining);

                        if (!player.getAbilities().invulnerable) playerHeldStack.shrink(amountToTransfer);
                        woodpeckerHeldStack.shrink(amountToTransfer);

                    }
                    else {
                        if (!player.getAbilities().invulnerable) player.setItemInHand(hand, ItemStack.EMPTY);
                        this.setItemInHand(InteractionHand.MAIN_HAND, playerHeldStack.copy());
                    }
                    return InteractionResult.SUCCESS;
                }

                ItemStack playerHeldStackCopy = playerHeldStack.copy();

                if (!player.getAbilities().invulnerable) playerHeldStack.shrink(1);

                if (isNewItemType) {
                    playerHeldStackCopy.setCount(1);
                    this.setItemInHand(InteractionHand.MAIN_HAND, playerHeldStackCopy);
                }
                else woodpeckerHeldStack.shrink(1);

                return InteractionResult.SUCCESS;
            }
        }

        else if (playerHeldStack.getItem() == WondrousWildsItems.LOVIFIER.get()) {
            if (!this.getLevel().isClientSide) this.finishTame();
            return InteractionResult.SUCCESS;
        }

        return super.mobInteract(player, hand);
    }

    @Override
    public void setItemSlot(EquipmentSlot slot, ItemStack stack) {
        super.setItemSlot(slot, stack);

        this.setGuaranteedDrop(slot);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (source.getEntity() instanceof WoodpeckerEntity) amount = 0.0F;

        if (super.hurt(source, amount)) {
            this.resetConsecutivePecks();

            if (!this.isFlying() && !this.isDrumming()) {
                this.setFlying(true);
            }

            return true;
        }
        else return false;
    }

    @Override
    public void travel(Vec3 movementInput) {
        if (!this.isFlying()) {
            super.travel(movementInput);
            return;
        }

        if (this.hasRestriction() || this.isWithinRestriction()) {
            if (this.isInWater()) {
                this.moveRelative(0.02F, movementInput);
                this.move(MoverType.SELF, this.getDeltaMovement());
                this.setDeltaMovement(this.getDeltaMovement().scale(0.8D));
            }
            else if (this.isInLava()) {
                this.moveRelative(0.02F, movementInput);
                this.move(MoverType.SELF, this.getDeltaMovement());
                this.setDeltaMovement(this.getDeltaMovement().scale(0.5D));
            }
            else {
                this.moveRelative(this.getSpeed(), movementInput);
                this.move(MoverType.SELF, this.getDeltaMovement());
                this.setDeltaMovement(this.getDeltaMovement().scale(0.91D));
            }
        }

        this.calculateEntityAnimation(this, false);
    }

    @Override
    public boolean hasRestriction() {
        return super.hasRestriction() && !this.isClinging();
    }

    @Override
    public boolean causeFallDamage(float fallDistance, float damageMultiplier, DamageSource damageSource) {
        return false;
    }

    @Override
    public int getNoActionTime() {
        return 0;
    }

    @Override
    protected float getStandingEyeHeight(Pose pose, EntityDimensions dimensions) {
        return dimensions.height * 0.95F;
    }

    @Override
    protected BodyRotationControl createBodyControl() {
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
        if (this.isFlying() && this.animationPosition >= 0.9F)
            event.getController().setAnimation(new AnimationBuilder().addAnimation("flap"));

        else if (this.isDrumming() && this.isClinging())
            event.getController().setAnimation(new AnimationBuilder().addAnimation("drum"));

        else
            event.getController().setAnimation(new AnimationBuilder().addAnimation("empty"));

        return PlayState.CONTINUE;
    }

    private void showTameParticles(boolean positive) {
        SimpleParticleType particleEffect = ParticleTypes.SMOKE;
        if (positive) particleEffect = ParticleTypes.HEART;

        if (!this.getLevel().isClientSide && this.getLevel() instanceof ServerLevel serverWorld) {
            serverWorld.sendParticles(particleEffect, this.getX(), this.getEyeY(), this.getZ(), 10, 0.25D, 0.25D, 0.25D, 0.0D);
        }

        else for (int i = 0; i < 7; ++i) {
            double d = this.getRandom().nextGaussian() * 0.02D;
            double e = this.getRandom().nextGaussian() * 0.02D;
            double f = this.getRandom().nextGaussian() * 0.02D;

            this.getLevel().addParticle(particleEffect, this.getX(1.0D), this.getRandomY() + 0.5D, this.getZ(1.0D), d, e, f);
        }
    }
}
