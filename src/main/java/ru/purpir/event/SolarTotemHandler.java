package ru.purpir.event;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import ru.purpir.enchantment.SolarInfusionSystem;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

public class SolarTotemHandler {
    private static final int IMMORTAL_TICKS = 40;
    private static final int FIRE_HIT_TICKS = 100;
    private static final float BONUS_DAMAGE = 2.0F;
    private static final Map<UUID, Long> FIRE_HIT_UNTIL = new HashMap<>();
    private static final Map<UUID, Long> IMMORTAL_UNTIL = new HashMap<>();

    public static void register() {
    }

    public static boolean hasSolarTotem(LivingEntity entity) {
        return isSolarTotem(entity.getStackInHand(Hand.MAIN_HAND)) || isSolarTotem(entity.getStackInHand(Hand.OFF_HAND));
    }

    public static void onSolarTotemUsed(LivingEntity entity) {
        if (!(entity instanceof PlayerEntity player) || !(entity.getEntityWorld() instanceof ServerWorld world)) {
            return;
        }

        long now = world.getTime();
        player.setHealth(player.getMaxHealth());
        IMMORTAL_UNTIL.put(player.getUuid(), now + IMMORTAL_TICKS);
        FIRE_HIT_UNTIL.put(player.getUuid(), now + FIRE_HIT_TICKS);

        world.spawnParticles(ParticleTypes.FLAME, player.getX(), player.getY() + 1.0, player.getZ(), 28, 0.55, 0.55, 0.55, 0.04);
        world.spawnParticles(ParticleTypes.END_ROD, player.getX(), player.getY() + 1.0, player.getZ(), 20, 0.45, 0.5, 0.45, 0.05);
        world.playSound(null, player.getBlockPos(), SoundEvents.BLOCK_BEACON_POWER_SELECT, SoundCategory.PLAYERS, 0.9f, 1.7f);
    }

    public static float getAttackBonus(PlayerEntity player, ServerWorld world) {
        cleanup(world);
        float bonus = 0.0F;

        if (isSolarTotem(player.getStackInHand(Hand.OFF_HAND))) {
            bonus += BONUS_DAMAGE;
        }

        if (FIRE_HIT_UNTIL.getOrDefault(player.getUuid(), 0L) > world.getTime()) {
            bonus += BONUS_DAMAGE;
        }

        return bonus;
    }

    public static boolean isImmortal(PlayerEntity player, ServerWorld world) {
        cleanup(world);
        return IMMORTAL_UNTIL.getOrDefault(player.getUuid(), 0L) > world.getTime();
    }

    public static void applyFireHit(PlayerEntity player, ServerWorld world, LivingEntity target) {
        cleanup(world);
        if (FIRE_HIT_UNTIL.getOrDefault(player.getUuid(), 0L) <= world.getTime()) {
            return;
        }

        target.setOnFireFor(5.0F);
        world.spawnParticles(ParticleTypes.FLAME, target.getX(), target.getBodyY(0.5), target.getZ(), 8, 0.25, 0.25, 0.25, 0.02);
    }

    private static boolean isSolarTotem(ItemStack stack) {
        return stack.isOf(Items.TOTEM_OF_UNDYING) && SolarInfusionSystem.isInfused(stack);
    }

    private static void cleanup(ServerWorld world) {
        long now = world.getTime();
        Iterator<Map.Entry<UUID, Long>> iterator = FIRE_HIT_UNTIL.entrySet().iterator();
        while (iterator.hasNext()) {
            if (iterator.next().getValue() <= now) {
                iterator.remove();
            }
        }

        Iterator<Map.Entry<UUID, Long>> immortalIterator = IMMORTAL_UNTIL.entrySet().iterator();
        while (immortalIterator.hasNext()) {
            if (immortalIterator.next().getValue() <= now) {
                immortalIterator.remove();
            }
        }
    }
}
