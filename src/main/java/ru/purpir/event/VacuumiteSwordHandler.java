package ru.purpir.event;

import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import ru.purpir.enchantment.SolarInfusionSystem;
import ru.purpir.item.ModItems;
import ru.purpir.solar.SolarPointBank;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

public class VacuumiteSwordHandler {
    private static final long MARK_DURATION_TICKS = 80;
    private static final long VOID_STRIKE_COOLDOWN_TICKS = 10;
    private static final float COLLAPSE_DAMAGE = 4.0F;
    private static final float ACTIVE_FIRE_SECONDS = 4.0F;
    private static final Map<UUID, VoidMark> MARKS = new HashMap<>();
    private static final Map<UUID, Long> NEXT_VOID_STRIKE = new HashMap<>();

    public static void register() {
        UseItemCallback.EVENT.register(VacuumiteSwordHandler::onUseItem);

        AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (world.isClient() || !(world instanceof ServerWorld serverWorld) || !(entity instanceof LivingEntity target)) {
                return ActionResult.PASS;
            }

            ItemStack stack = player.getStackInHand(hand);
            if (!stack.isOf(ModItems.VACUUMITE_SWORD)) {
                return ActionResult.PASS;
            }

            boolean infused = SolarInfusionSystem.isInfused(stack);
            if (isVoidStrikeOnCooldown(player, serverWorld)) {
                return ActionResult.PASS;
            }

            if (infused && SolarPointBank.isVacuumiteSwordActive(player, serverWorld)) {
                putVoidStrikeOnCooldown(player, serverWorld);
                triggerActiveStrike(serverWorld, player, target);
                return ActionResult.PASS;
            }

            handleHit(serverWorld, player, target, infused);
            return ActionResult.PASS;
        });
    }

    private static ActionResult onUseItem(PlayerEntity player, net.minecraft.world.World world, Hand hand) {
        ItemStack stack = player.getStackInHand(hand);
        if (!stack.isOf(ModItems.VACUUMITE_SWORD) || !SolarInfusionSystem.isInfused(stack)) {
            return ActionResult.PASS;
        }

        if (world.isClient()) {
            return ActionResult.SUCCESS;
        }

        if (player instanceof ServerPlayerEntity serverPlayer && world instanceof ServerWorld serverWorld) {
            boolean success = SolarPointBank.tryActivateVacuumiteSword(serverPlayer, serverWorld);
            return success ? ActionResult.SUCCESS : ActionResult.FAIL;
        }

        return ActionResult.PASS;
    }

    private static void handleHit(ServerWorld world, PlayerEntity player, LivingEntity target, boolean infused) {
        cleanup(world);

        UUID targetId = target.getUuid();
        UUID attackerId = player.getUuid();
        long now = world.getTime();
        VoidMark mark = MARKS.get(targetId);

        if (mark != null && mark.owner.equals(attackerId) && mark.expiresAt > now) {
            MARKS.remove(targetId);
            if (!mark.hadGlowingBefore) {
                target.removeStatusEffect(StatusEffects.GLOWING);
            }
            DamageSource source = player.getDamageSources().playerAttack(player);
            target.damage(world, source, COLLAPSE_DAMAGE);
            if (infused && player instanceof ServerPlayerEntity serverPlayer) {
                SolarPointBank.addVoidWoundPoints(serverPlayer);
            }
            putVoidStrikeOnCooldown(player, world);
            spawnCollapse(world, target);
            world.playSound(null, target.getBlockPos(), SoundEvents.BLOCK_AMETHYST_CLUSTER_BREAK, SoundCategory.PLAYERS, 0.9f, 0.65f);
            world.playSound(null, target.getBlockPos(), SoundEvents.ENTITY_ENDERMAN_TELEPORT, SoundCategory.PLAYERS, 0.45f, 1.7f);
            return;
        }

        boolean hadGlowingBefore = target.hasStatusEffect(StatusEffects.GLOWING);
        MARKS.put(targetId, new VoidMark(attackerId, now + MARK_DURATION_TICKS, hadGlowingBefore));
        putVoidStrikeOnCooldown(player, world);
        target.addStatusEffect(new StatusEffectInstance(StatusEffects.GLOWING, (int) MARK_DURATION_TICKS, 0, false, false, true));
        spawnMark(world, target);
        world.playSound(null, target.getBlockPos(), SoundEvents.BLOCK_AMETHYST_CLUSTER_HIT, SoundCategory.PLAYERS, 0.6f, 0.55f);
    }

    private static void triggerActiveStrike(ServerWorld world, PlayerEntity player, LivingEntity target) {
        DamageSource source = player.getDamageSources().playerAttack(player);
        target.damage(world, source, COLLAPSE_DAMAGE);
        target.setOnFireFor(ACTIVE_FIRE_SECONDS);
        spawnCollapse(world, target);
        world.spawnParticles(ParticleTypes.FLAME, target.getX(), target.getBodyY(0.5), target.getZ(), 8, 0.24, 0.28, 0.24, 0.02);
        world.playSound(null, target.getBlockPos(), SoundEvents.BLOCK_AMETHYST_CLUSTER_BREAK, SoundCategory.PLAYERS, 0.65f, 0.8f);
    }

    private static void spawnMark(ServerWorld world, LivingEntity target) {
        world.spawnParticles(ParticleTypes.REVERSE_PORTAL, target.getX(), target.getBodyY(0.55), target.getZ(), 18, 0.28, 0.35, 0.28, 0.02);
        world.spawnParticles(ParticleTypes.GLOW, target.getX(), target.getBodyY(0.55), target.getZ(), 6, 0.18, 0.22, 0.18, 0.01);
    }

    private static void spawnCollapse(ServerWorld world, LivingEntity target) {
        world.spawnParticles(ParticleTypes.PORTAL, target.getX(), target.getBodyY(0.5), target.getZ(), 32, 0.35, 0.45, 0.35, 0.08);
        world.spawnParticles(ParticleTypes.SONIC_BOOM, target.getX(), target.getBodyY(0.5), target.getZ(), 1, 0.0, 0.0, 0.0, 0.0);
    }

    private static void cleanup(ServerWorld world) {
        long now = world.getTime();
        Iterator<Map.Entry<UUID, VoidMark>> iterator = MARKS.entrySet().iterator();
        while (iterator.hasNext()) {
            if (iterator.next().getValue().expiresAt <= now) {
                iterator.remove();
            }
        }

        Iterator<Map.Entry<UUID, Long>> cooldownIterator = NEXT_VOID_STRIKE.entrySet().iterator();
        while (cooldownIterator.hasNext()) {
            if (cooldownIterator.next().getValue() <= now) {
                cooldownIterator.remove();
            }
        }
    }

    private static boolean isVoidStrikeOnCooldown(PlayerEntity player, ServerWorld world) {
        cleanup(world);
        return NEXT_VOID_STRIKE.getOrDefault(player.getUuid(), 0L) > world.getTime();
    }

    private static void putVoidStrikeOnCooldown(PlayerEntity player, ServerWorld world) {
        NEXT_VOID_STRIKE.put(player.getUuid(), world.getTime() + VOID_STRIKE_COOLDOWN_TICKS);
    }

    private record VoidMark(UUID owner, long expiresAt, boolean hadGlowingBefore) {
    }
}
