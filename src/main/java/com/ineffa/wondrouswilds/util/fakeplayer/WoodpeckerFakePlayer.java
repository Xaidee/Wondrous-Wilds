package com.ineffa.wondrouswilds.util.fakeplayer;

import com.ineffa.wondrouswilds.entities.WoodpeckerEntity;
import com.mojang.authlib.GameProfile;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class WoodpeckerFakePlayer extends FakeServerPlayerEntity {

    private final WoodpeckerEntity woodpecker;

    public WoodpeckerFakePlayer(WoodpeckerEntity woodpecker) {
        super((ServerLevel) woodpecker.getLevel(), new GameProfile(UUID.fromString("62ca5b38-99b2-4cea-93a1-b32935725151"), woodpecker.getName().getString()));

        this.copyPosition(woodpecker);

        for (EquipmentSlot slot : EquipmentSlot.values()) {
            ItemStack itemStack = woodpecker.getItemBySlot(slot);
            if (itemStack.isEmpty()) continue;
            this.setItemSlot(slot, itemStack.copy());
        }

        this.setInvulnerable(woodpecker.isInvulnerable());

        this.woodpecker = woodpecker;
    }

    @Override
    public boolean canEat(boolean ignoreHunger) {
        return true;
    }

    @Override
    public void setItemInHand(InteractionHand hand, ItemStack stack) {
        if (hand == InteractionHand.MAIN_HAND) this.woodpecker.equipStack(EquipmentSlot.MAINHAND, stack);

        else if (hand == InteractionHand.OFF_HAND) this.woodpecker.spawnAtLocation(stack);

        else super.setItemInHand(hand, stack);
    }

    @Override
    public ItemStack getItemInHand(InteractionHand hand) {
        return this.woodpecker.getItemInHand(hand);
    }

    @Nullable
    @Override
    public ItemEntity spawnAtLocation(ItemStack stack) {
        return this.woodpecker.spawnAtLocation(stack);
    }

    @Nullable
    @Override
    public ItemEntity spawnAtLocation(ItemStack stack, float yOffset) {
        return this.woodpecker.spawnAtLocation(stack, yOffset);
    }

    @Override
    public boolean canTakeItem(ItemStack stack) {
        if (this.woodpecker.getMainHandItem().isEmpty()) {
            this.setItemInHand(InteractionHand.MAIN_HAND, stack);
            return true;
        }

        return false;
    }

    @Override
    public boolean isCreative() {
        return false;
    }

    @Override
    public boolean isSpectator() {
        return false;
    }
}
