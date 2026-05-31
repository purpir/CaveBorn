package ru.purpir.event;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import ru.purpir.item.ModItems;

public class VacuumiteMagnetHandler {
    private static final double RADIUS = 8.0;
    private static final double MAX_PULL_SPEED = 0.35;

    public static void register() {
        ServerTickEvents.END_WORLD_TICK.register(VacuumiteMagnetHandler::onWorldTick);
    }

    private static void onWorldTick(ServerWorld world) {
        if (world.getTime() % 2 != 0) {
            return;
        }

        for (PlayerEntity player : world.getPlayers()) {
            if (!hasMagnet(player)) {
                continue;
            }

            pullNearbyItems(world, player);
        }
    }

    private static boolean hasMagnet(PlayerEntity player) {
        for (int slot = 0; slot < player.getInventory().size(); slot++) {
            ItemStack stack = player.getInventory().getStack(slot);
            if (stack.isOf(ModItems.VACUUMITE_MAGNET)) {
                return true;
            }
        }
        return false;
    }

    private static void pullNearbyItems(ServerWorld world, PlayerEntity player) {
        Box searchBox = player.getBoundingBox().expand(RADIUS);

        for (ItemEntity item : world.getEntitiesByClass(ItemEntity.class, searchBox, ItemEntity::isAlive)) {
            Vec3d target = new Vec3d(player.getX(), player.getY() + 0.75, player.getZ());
            Vec3d itemPos = new Vec3d(item.getX(), item.getY(), item.getZ());
            Vec3d offset = target.subtract(itemPos);
            double distance = offset.length();

            if (distance < 0.6 || distance > RADIUS) {
                continue;
            }

            double scaledSpeed = 0.08 + (1.0 - distance / RADIUS) * 0.25;
            double speed = Math.min(MAX_PULL_SPEED, scaledSpeed);
            Vec3d pullVelocity = offset.normalize().multiply(speed);
            item.setVelocity(item.getVelocity().multiply(0.55).add(pullVelocity));
            item.velocityDirty = true;
        }
    }
}
