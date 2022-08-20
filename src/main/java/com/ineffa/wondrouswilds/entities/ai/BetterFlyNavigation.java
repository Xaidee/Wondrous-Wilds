package com.ineffa.wondrouswilds.entities.ai;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.Path;

public class BetterFlyNavigation extends FlyingPathNavigation {

    public BetterFlyNavigation(Mob mobEntity, Level world) {
        super(mobEntity, world);
    }

    @Override
    public boolean moveTo(double x, double y, double z, double speed) {
        return this.moveTo(this.createPath(x, y, z, 0), speed);
    }

    @Override
    public boolean moveTo(Entity entity, double speed) {
        Path path = this.createPath(entity, 0);
        return path != null && this.moveTo(path, speed);
    }
}
