package ru.purpir.event;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import ru.purpir.enchantment.SolarInfusionSystem;
import ru.purpir.item.ModItems;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class VacuumiteMagnetHandler {
    private static final double RADIUS = 8.0;
    private static final double MAX_PULL_SPEED = 0.35;
    private static final double SOLAR_RADIUS = 16.0;
    private static final double SOLAR_MAX_PULL_SPEED = MAX_PULL_SPEED * 2.0;
    private static final long SECOND_HIT_DELAY_TICKS = 10;

    private static final List<DelayedMagnetHit> DELAYED_HITS = new ArrayList<>();

    public static void register() {
        ServerTickEvents.END_WORLD_TICK.register(VacuumiteMagnetHandler::onWorldTick);
        AttackEntityCallback.EVENT.register(VacuumiteMagnetHandler::onAttackEntity);
    }

    private static void onWorldTick(ServerWorld world) {
        repairMagnets(world);
        processDelayedHits(world);

        if (world.getTime() % 2 != 0) {
            return;
        }

        for (PlayerEntity player : world.getPlayers()) {
            MagnetPullStats stats = getPullStats(player);
            if (stats == null) {
                continue;
            }

            pullNearbyItems(world, player, stats);
        }
    }

    private static MagnetPullStats getPullStats(PlayerEntity player) {
        boolean hasMagnet = false;
        boolean hasSolarMagnet = false;

        for (int slot = 0; slot < player.getInventory().size(); slot++) {
            ItemStack stack = player.getInventory().getStack(slot);
            if (stack.isOf(ModItems.VACUUMITE_MAGNET)) {
                hasMagnet = true;
                if (SolarInfusionSystem.isInfused(stack)) {
                    hasSolarMagnet = true;
                }
            }
        }

        if (!hasMagnet) {
            return null;
        }

        return hasSolarMagnet
            ? new MagnetPullStats(SOLAR_RADIUS, SOLAR_MAX_PULL_SPEED)
            : new MagnetPullStats(RADIUS, MAX_PULL_SPEED);
    }

    private static void pullNearbyItems(ServerWorld world, PlayerEntity player, MagnetPullStats stats) {
        Box searchBox = player.getBoundingBox().expand(stats.radius());

        for (ItemEntity item : world.getEntitiesByClass(ItemEntity.class, searchBox, ItemEntity::isAlive)) {
            Vec3d target = new Vec3d(player.getX(), player.getY() + 0.75, player.getZ());
            Vec3d itemPos = new Vec3d(item.getX(), item.getY(), item.getZ());
            Vec3d offset = target.subtract(itemPos);
            double distance = offset.length();

            if (distance < 0.6 || distance > stats.radius()) {
                continue;
            }

            double scaledSpeed = 0.08 + (1.0 - distance / stats.radius()) * 0.25;
            double speed = Math.min(stats.maxSpeed(), scaledSpeed * stats.speedMultiplier());
            Vec3d pullVelocity = offset.normalize().multiply(speed);
            item.setVelocity(item.getVelocity().multiply(0.55).add(pullVelocity));
            item.velocityDirty = true;
        }
    }

    private static ActionResult onAttackEntity(PlayerEntity player, World world, net.minecraft.util.Hand hand,
                                               net.minecraft.entity.Entity entity, net.minecraft.util.hit.EntityHitResult hitResult) {
        ItemStack stack = player.getStackInHand(hand);
        if (!stack.isOf(ModItems.VACUUMITE_MAGNET)) {
            return ActionResult.PASS;
        }

        if (!SolarInfusionSystem.isInfused(stack)) {
            return ActionResult.PASS;
        }

        if (stack.getDamage() > 0) {
            return ActionResult.FAIL;
        }

        if (world.isClient()) {
            return ActionResult.SUCCESS;
        }

        if (!(world instanceof ServerWorld serverWorld) || !(entity instanceof LivingEntity target)) {
            return ActionResult.FAIL;
        }

        Vec3d knockback = getKnockbackVector(player, target);
        applyMagnetHit(serverWorld, player, target, knockback, 5.0f);
        stack.setDamage(stack.getMaxDamage());
        DELAYED_HITS.add(new DelayedMagnetHit(serverWorld, player, target, knockback, world.getTime() + SECOND_HIT_DELAY_TICKS));

        return ActionResult.SUCCESS;
    }

    private static void repairMagnets(ServerWorld world) {
        for (PlayerEntity player : world.getPlayers()) {
            for (int slot = 0; slot < player.getInventory().size(); slot++) {
                ItemStack stack = player.getInventory().getStack(slot);
                if (stack.isOf(ModItems.VACUUMITE_MAGNET) && stack.getDamage() > 0) {
                    stack.setDamage(stack.getDamage() - 1);
                }
            }
        }
    }

    private static void processDelayedHits(ServerWorld world) {
        Iterator<DelayedMagnetHit> iterator = DELAYED_HITS.iterator();
        while (iterator.hasNext()) {
            DelayedMagnetHit hit = iterator.next();
            if (hit.world() != world || world.getTime() < hit.triggerTime()) {
                continue;
            }

            if (hit.player().isAlive() && hit.target().isAlive()) {
                applyMagnetHit(world, hit.player(), hit.target(), hit.knockback(), 2.0f);
            }
            iterator.remove();
        }
    }

    private static void applyMagnetHit(ServerWorld world, PlayerEntity player, LivingEntity target, Vec3d knockback, float damage) {
        target.damage(world, player.getDamageSources().playerAttack(player), damage);
        target.addVelocity(knockback.x, 0.25, knockback.z);
        target.velocityDirty = true;
    }

    private static Vec3d getKnockbackVector(PlayerEntity player, LivingEntity target) {
        Vec3d offset = new Vec3d(target.getX() - player.getX(), 0.0, target.getZ() - player.getZ());
        if (offset.lengthSquared() < 0.0001) {
            offset = player.getRotationVector().multiply(1.0, 0.0, 1.0);
        }
        return offset.normalize().multiply(1.15);
    }

    private record MagnetPullStats(double radius, double maxSpeed) {
        double speedMultiplier() {
            return maxSpeed / MAX_PULL_SPEED;
        }
    }

    private record DelayedMagnetHit(ServerWorld world, PlayerEntity player, LivingEntity target, Vec3d knockback, long triggerTime) {
    }
}
