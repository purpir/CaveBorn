package ru.purpir.eventaltar.util;

import net.minecraft.entity.mob.MobEntity;
import net.minecraft.util.math.BlockPos;

public final class AltarMobAiUtil {
    private AltarMobAiUtil() {
    }

    public static void moveToBlock(MobEntity mob, BlockPos target, double speed) {
        mob.getNavigation().startMovingTo(target.getX() + 0.5, target.getY(), target.getZ() + 0.5, speed);
    }

    public static boolean isNearBlock(MobEntity mob, BlockPos target, double radius) {
        double dx = mob.getX() - (target.getX() + 0.5);
        double dz = mob.getZ() - (target.getZ() + 0.5);
        return dx * dx + dz * dz <= radius * radius && Math.abs(mob.getY() - target.getY()) < 3.0;
    }
}
