package ru.purpir.event;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.tag.EntityTypeTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import ru.purpir.component.CooldownComponent;
import ru.purpir.component.ModComponents;
import ru.purpir.enchantment.SolarInfusionSystem;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SolarShieldHandler {
    private static final long COOLDOWN_TICKS = 200;
    private static final int MIN_CHARGE_TICKS = 10;
    private static final int MAX_CHARGE_TICKS = 40;
    private static final double BASE_RANGE = 4.0;
    private static final double MAX_RANGE = 7.0;
    private static final double CONE_DOT = 0.35;

    private static final Map<UUID, ShieldCharge> CHARGING_PLAYERS = new HashMap<>();

    public static void register() {
        ServerTickEvents.END_WORLD_TICK.register(SolarShieldHandler::onWorldTick);
    }

    private static void onWorldTick(ServerWorld world) {
        long currentTime = world.getTime();

        for (PlayerEntity player : world.getPlayers()) {
            UUID uuid = player.getUuid();
            ItemStack shield = getActiveSolarShield(player);

            if (!shield.isEmpty()) {
                if (isOnCooldown(shield, currentTime)) {
                    CHARGING_PLAYERS.remove(uuid);
                    continue;
                }

                ShieldCharge charge = CHARGING_PLAYERS.get(uuid);
                if (charge == null) {
                    CHARGING_PLAYERS.put(uuid, new ShieldCharge(shield, currentTime, 1));
                } else {
                    charge.ticks = Math.min(MAX_CHARGE_TICKS, charge.ticks + 1);
                    charge.stack = shield;
                }
                continue;
            }

            ShieldCharge charge = CHARGING_PLAYERS.remove(uuid);
            if (charge != null && charge.ticks >= MIN_CHARGE_TICKS && !isOnCooldown(charge.stack, currentTime)) {
                releaseSolarShield(world, player, charge.stack, charge.ticks, currentTime);
            }
        }
    }

    private static ItemStack getActiveSolarShield(PlayerEntity player) {
        if (!player.isBlocking()) {
            return ItemStack.EMPTY;
        }

        ItemStack activeStack = player.getActiveItem();
        if (activeStack.isOf(Items.SHIELD) && SolarInfusionSystem.isInfused(activeStack)) {
            return activeStack;
        }

        return ItemStack.EMPTY;
    }

    private static boolean isOnCooldown(ItemStack stack, long currentTime) {
        CooldownComponent cooldown = stack.getOrDefault(ModComponents.ABILITY_COOLDOWN, CooldownComponent.DEFAULT);
        return cooldown.isOnCooldown(currentTime, COOLDOWN_TICKS);
    }

    private static void releaseSolarShield(ServerWorld world, PlayerEntity player, ItemStack shield, int chargeTicks, long currentTime) {
        double charge = Math.min(1.0, chargeTicks / (double) MAX_CHARGE_TICKS);
        double range = BASE_RANGE + (MAX_RANGE - BASE_RANGE) * charge;
        float damage = (float) (2.0 + 3.0 * charge);
        double knockbackStrength = 0.65 + 0.7 * charge;

        Vec3d forward = player.getRotationVector().normalize();
        Vec3d origin = new Vec3d(player.getX(), player.getY() + 1.0, player.getZ());
        Box box = player.getBoundingBox().expand(range);
        int hits = 0;

        for (LivingEntity target : world.getEntitiesByClass(LivingEntity.class, box, LivingEntity::isAlive)) {
            if (target == player) {
                continue;
            }

            Vec3d toTarget = new Vec3d(target.getX() - player.getX(), target.getBodyY(0.5) - origin.y, target.getZ() - player.getZ());
            double distance = toTarget.length();
            if (distance > range || distance < 0.01) {
                continue;
            }

            Vec3d direction = toTarget.normalize();
            if (direction.dotProduct(forward) < CONE_DOT) {
                continue;
            }

            float finalDamage = target.getType().isIn(EntityTypeTags.UNDEAD) ? damage + 2.0f : damage;
            target.damage(world, player.getDamageSources().playerAttack(player), finalDamage);
            if (target.getType().isIn(EntityTypeTags.UNDEAD)) {
                target.setOnFireFor(4);
            }
            target.addVelocity(direction.x * knockbackStrength, 0.25 + charge * 0.2, direction.z * knockbackStrength);
            target.velocityDirty = true;
            hits++;
        }

        shield.set(ModComponents.ABILITY_COOLDOWN, new CooldownComponent(currentTime));
        shield.damage(6 + (int) (charge * 6), player, player.getPreferredEquipmentSlot(shield));

        world.spawnParticles(ParticleTypes.END_ROD, player.getX(), player.getY() + 1.0, player.getZ(), 24 + (int) (charge * 24), 0.8, 0.45, 0.8, 0.08);
        world.playSound(null, player.getBlockPos(), SoundEvents.ITEM_SHIELD_BLOCK.value(), SoundCategory.PLAYERS, 1.0f, 1.4f);
        world.playSound(null, player.getBlockPos(), SoundEvents.BLOCK_BEACON_POWER_SELECT, SoundCategory.PLAYERS, 0.8f, 1.8f);

        if (hits > 0) {
            player.sendMessage(Text.translatable("ability.caveborn.solar_shield", hits).formatted(Formatting.GOLD), true);
        }

    }

    private static class ShieldCharge {
        private ItemStack stack;
        private final long startedAt;
        private int ticks;

        private ShieldCharge(ItemStack stack, long startedAt, int ticks) {
            this.stack = stack;
            this.startedAt = startedAt;
            this.ticks = ticks;
        }
    }
}
