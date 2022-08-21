package com.ineffa.wondrouswilds.world.features.trees.trunks;

import com.google.common.collect.ImmutableList;
import com.ineffa.wondrouswilds.registry.WondrousWildsFeatures;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer;
import net.minecraft.world.level.levelgen.feature.trunkplacers.StraightTrunkPlacer;
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacer;
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacerType;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

import static com.ineffa.wondrouswilds.util.WondrousWildsUtils.HORIZONTAL_DIRECTIONS;

public class StraightBranchingTrunkPlacer extends TrunkPlacer {

    public static final Codec<StraightBranchingTrunkPlacer> CODEC = RecordCodecBuilder.create(instance -> StraightBranchingTrunkPlacer.trunkPlacerParts(instance).and(instance.group(
            ExtraCodecs.NON_NEGATIVE_INT.fieldOf("min_branches").forGetter(StraightBranchingTrunkPlacer::getMinBranches),
            ExtraCodecs.POSITIVE_INT.fieldOf("max_branches").forGetter(StraightBranchingTrunkPlacer::getMaxBranches),
            ExtraCodecs.POSITIVE_INT.fieldOf("min_branch_length").forGetter(StraightBranchingTrunkPlacer::getMinBranchLength),
            ExtraCodecs.POSITIVE_INT.fieldOf("max_branch_length").forGetter(StraightBranchingTrunkPlacer::getMaxBranchLength)
    )).apply(instance, StraightBranchingTrunkPlacer::new));

    private final int minBranches;
    private final int maxBranches;
    private final int minBranchLength;
    private final int maxBranchLength;

    public int getMinBranches() {
        return this.minBranches;
    }

    public int getMaxBranches() {
        return this.maxBranches;
    }

    public int getMinBranchLength() {
        return this.minBranchLength;
    }

    public int getMaxBranchLength() {
        return this.maxBranchLength;
    }

    public StraightBranchingTrunkPlacer(int baseHeight, int firstRandomHeight, int secondRandomHeight, int minBranches, int maxBranches, int minBranchLength, int maxBranchLength) {
        super(baseHeight, firstRandomHeight, secondRandomHeight);

        this.minBranches = minBranches;
        this.maxBranches = maxBranches;

        this.minBranchLength = minBranchLength;
        this.maxBranchLength = maxBranchLength;
    }

    @Override
    protected TrunkPlacerType<?> type() {
        return WondrousWildsFeatures.Trees.TrunkPlacers.STRAIGHT_BRANCHING_TRUNK.get();
    }

    @Override
    public List<FoliagePlacer.FoliageAttachment> placeTrunk(LevelSimulatedReader world, BiConsumer<BlockPos, BlockState> replacer, RandomSource random, int height, BlockPos startPos, TreeConfiguration config) {
        StraightTrunkPlacer.setDirtAt(world, replacer, random, startPos.below(), config);

        BlockPos topTrunkLog = startPos.above(height);

        List<BlockPos> trunkLogsSuitableForBranch = new ArrayList<>();

        for (int currentHeight = 0; currentHeight < height; ++currentHeight) {
            BlockPos logPos = startPos.above(currentHeight);
            if (this.placeLog(world, replacer, random, logPos, config) && logPos.getY() < topTrunkLog.above().getY() - 7) trunkLogsSuitableForBranch.add(logPos);
        }

        int branches = this.minBranches; while (branches < this.maxBranches) if (random.nextBoolean()) ++branches; else break;

        List<BlockPos> successfulBranchPositions = new ArrayList<>();
        List<BlockPos> trunkLogsWithBranchAttempts = new ArrayList<>();
        while (trunkLogsWithBranchAttempts.size() < branches) {
            if (trunkLogsWithBranchAttempts.size() >= trunkLogsSuitableForBranch.size()) break;

            BlockPos trunkLogWithBranchPos = trunkLogsSuitableForBranch.get(random.nextInt(trunkLogsSuitableForBranch.size()));
            if (trunkLogsWithBranchAttempts.contains(trunkLogWithBranchPos)) continue;

            trunkLogsWithBranchAttempts.add(trunkLogWithBranchPos);

            Direction branchDirection;

            List<Direction> suitableBranchDirections = new ArrayList<>();
            for (Direction checkDirection : HORIZONTAL_DIRECTIONS) {
                BlockPos checkPos = trunkLogWithBranchPos.relative(checkDirection);

                if (successfulBranchPositions.contains(checkPos.below()) || successfulBranchPositions.contains(checkPos.above())) continue;

                suitableBranchDirections.add(checkDirection);
            }
            if (suitableBranchDirections.isEmpty()) continue;
            else branchDirection = suitableBranchDirections.get(random.nextInt(suitableBranchDirections.size()));

            int nextBranchLogDistance = 1;
            boolean isNotMinimumLength = nextBranchLogDistance <= this.minBranchLength;
            boolean isNotMaximumLength = nextBranchLogDistance <= this.maxBranchLength;
            while (isNotMaximumLength || isNotMinimumLength) {
                if (!isNotMinimumLength && random.nextBoolean()) break;

                BlockPos branchPos = trunkLogWithBranchPos.relative(branchDirection, nextBranchLogDistance);
                if (!this.placeLog(world, replacer, random, branchPos, config, state -> state.setValue(RotatedPillarBlock.AXIS, branchDirection.getAxis()))) break;
                else successfulBranchPositions.add(branchPos);

                ++nextBranchLogDistance;
                isNotMinimumLength = nextBranchLogDistance <= this.minBranchLength;
                isNotMaximumLength = nextBranchLogDistance <= this.maxBranchLength;
            }
        }

        return ImmutableList.of(new FoliagePlacer.FoliageAttachment(topTrunkLog, 0, false));
    }
}
