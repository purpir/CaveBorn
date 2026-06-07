package ru.purpir.event;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import ru.purpir.enchantment.SolarInfusionSystem;
import ru.purpir.entity.SolarSoulEntity;
import ru.purpir.item.ModItems;
import ru.purpir.network.ModPackets;
import ru.purpir.solar.SolarPointBank;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class SolarBronzeAxeHandler {
    private static final int DOUBLE_JUMP_COST = 10;
    private static final int DOUBLE_JUMP_MIN_AIR_TICKS = 3;
    private static final double DOUBLE_JUMP_UP_VELOCITY = 0.95D;
    private static final int LANDING_DAMAGE_DURABILITY = 50;
    private static final float LANDING_DAMAGE = 8.0F;
    private static final Set<UUID> USED_AIR_JUMP = new HashSet<>();
    private static final Set<UUID> LANDING_SLAM_READY = new HashSet<>();
    private static final Set<UUID> FALL_DAMAGE_PROTECTED = new HashSet<>();
    private static final Map<UUID, Integer> FALL_PROTECTION_GRACE_TICKS = new HashMap<>();
    private static final Map<UUID, Integer> AIR_TICKS = new HashMap<>();

    private SolarBronzeAxeHandler() {
    }

    public static void register() {
        ServerPlayNetworking.registerGlobalReceiver(ModPackets.BronzeAxeDoubleJumpPayload.ID, (payload, context) ->
            context.server().execute(() -> tryDoubleJump(context.player())));
        ServerTickEvents.END_WORLD_TICK.register(SolarBronzeAxeHandler::onWorldTick);
    }

    public static void onEntityKilled(ServerPlayerEntity player, LivingEntity killed) {
        ItemStack weapon = player.getMainHandStack();
        if (!weapon.isOf(ModItems.BRONZE_AXE) || !SolarInfusionSystem.isInfused(weapon)) {
            return;
        }

        ServerWorld world = (ServerWorld) player.getEntityWorld();
        SolarSoulEntity soul = new SolarSoulEntity(world, new Vec3d(killed.getX(), killed.getY() + killed.getHeight() * 0.5, killed.getZ()), player.getUuid());
        soul.setVelocity(
            (world.random.nextDouble() - 0.5) * 0.08,
            0.08 + world.random.nextDouble() * 0.05,
            (world.random.nextDouble() - 0.5) * 0.08
        );
        world.spawnEntity(soul);
    }

    private static void tryDoubleJump(ServerPlayerEntity player) {
        ServerWorld world = (ServerWorld) player.getEntityWorld();
        ItemStack axe = getHeldInfusedBronzeAxe(player);
        if (axe.isEmpty() || player.isOnGround() || USED_AIR_JUMP.contains(player.getUuid()) ||
            AIR_TICKS.getOrDefault(player.getUuid(), 0) < DOUBLE_JUMP_MIN_AIR_TICKS) {
            return;
        }

        if (!SolarPointBank.trySpendPoints(player, DOUBLE_JUMP_COST)) {
            return;
        }

        USED_AIR_JUMP.add(player.getUuid());
        LANDING_SLAM_READY.add(player.getUuid());
        FALL_DAMAGE_PROTECTED.add(player.getUuid());
        Vec3d velocity = player.getVelocity();
        player.setVelocity(velocity.x, DOUBLE_JUMP_UP_VELOCITY, velocity.z);
        player.velocityDirty = true;
        player.networkHandler.sendPacket(new EntityVelocityUpdateS2CPacket(player));
        player.fallDistance = 0.0F;

        world.spawnParticles(ParticleTypes.SOUL_FIRE_FLAME, player.getX(), player.getY() + 0.25, player.getZ(), 22, 0.35, 0.12, 0.35, 0.035);
        world.spawnParticles(ParticleTypes.GLOW, player.getX(), player.getY() + 0.2, player.getZ(), 12, 0.28, 0.08, 0.28, 0.02);
        world.playSound(null, player.getBlockPos(), SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP, SoundCategory.PLAYERS, 0.75F, 0.65F);
    }

    private static void onWorldTick(ServerWorld world) {
        for (ServerPlayerEntity player : world.getPlayers()) {
            UUID uuid = player.getUuid();
            if (!player.isOnGround()) {
                AIR_TICKS.merge(uuid, 1, Integer::sum);
                continue;
            }

            AIR_TICKS.remove(uuid);
            USED_AIR_JUMP.remove(uuid);
            if (LANDING_SLAM_READY.remove(uuid)) {
                player.fallDistance = 0.0F;
                FALL_PROTECTION_GRACE_TICKS.put(uuid, 10);
                doLandingSlam(world, player);
                continue;
            }

            Integer graceTicks = FALL_PROTECTION_GRACE_TICKS.get(uuid);
            if (graceTicks != null) {
                if (graceTicks <= 1) {
                    FALL_PROTECTION_GRACE_TICKS.remove(uuid);
                    FALL_DAMAGE_PROTECTED.remove(uuid);
                } else {
                    FALL_PROTECTION_GRACE_TICKS.put(uuid, graceTicks - 1);
                }
            }
        }
    }

    public static boolean consumeFallDamageProtection(ServerPlayerEntity player) {
        UUID uuid = player.getUuid();
        if (!FALL_DAMAGE_PROTECTED.remove(uuid)) {
            return false;
        }
        FALL_PROTECTION_GRACE_TICKS.remove(uuid);
        player.fallDistance = 0.0F;
        return true;
    }

    private static void doLandingSlam(ServerWorld world, ServerPlayerEntity player) {
        ItemStack axe = findInfusedBronzeAxe(player);
        if (!axe.isEmpty() && !player.isCreative()) {
            axe.damage(LANDING_DAMAGE_DURABILITY, player, player.getPreferredEquipmentSlot(axe));
        }

        BlockPos center = player.getBlockPos();
        Box area = new Box(center).expand(2.5, 1.2, 2.5);
        for (LivingEntity target : world.getEntitiesByClass(LivingEntity.class, area, LivingEntity::isAlive)) {
            if (target == player) {
                continue;
            }

            target.damage(world, player.getDamageSources().playerAttack(player), LANDING_DAMAGE);
            Vec3d offset = new Vec3d(target.getX() - player.getX(), 0.0, target.getZ() - player.getZ());
            if (offset.lengthSquared() < 0.001) {
                offset = player.getRotationVector().multiply(1.0, 0.0, 1.0);
            }
            Vec3d knockback = offset.normalize().multiply(1.25);
            target.addVelocity(knockback.x, 0.35, knockback.z);
            target.velocityDirty = true;
        }

        placeFireRing(world, center);
        world.spawnParticles(ParticleTypes.EXPLOSION, player.getX(), player.getY() + 0.1, player.getZ(), 1, 0.0, 0.0, 0.0, 0.0);
        world.spawnParticles(ParticleTypes.SOUL_FIRE_FLAME, player.getX(), player.getY() + 0.1, player.getZ(), 44, 1.8, 0.08, 1.8, 0.04);
        world.playSound(null, center, SoundEvents.ENTITY_GENERIC_EXPLODE.value(), SoundCategory.PLAYERS, 0.8F, 1.25F);
        world.playSound(null, center, SoundEvents.BLOCK_ANVIL_LAND, SoundCategory.PLAYERS, 0.45F, 0.65F);
    }

    private static void placeFireRing(ServerWorld world, BlockPos center) {
        for (int x = -2; x <= 2; x++) {
            for (int z = -2; z <= 2; z++) {
                if (Math.abs(x) != 2 && Math.abs(z) != 2) {
                    continue;
                }
                BlockPos pos = center.add(x, 0, z);
                if (!world.getBlockState(pos).isAir()) {
                    pos = pos.up();
                }
                if (world.getBlockState(pos).isAir() && world.getBlockState(pos.down()).isSolidBlock(world, pos.down())) {
                    world.setBlockState(pos, Blocks.FIRE.getDefaultState());
                }
            }
        }
    }

    private static ItemStack getHeldInfusedBronzeAxe(PlayerEntity player) {
        ItemStack mainHand = player.getMainHandStack();
        if (mainHand.isOf(ModItems.BRONZE_AXE) && SolarInfusionSystem.isInfused(mainHand)) {
            return mainHand;
        }

        ItemStack offHand = player.getOffHandStack();
        if (offHand.isOf(ModItems.BRONZE_AXE) && SolarInfusionSystem.isInfused(offHand)) {
            return offHand;
        }

        return ItemStack.EMPTY;
    }

    private static ItemStack findInfusedBronzeAxe(PlayerEntity player) {
        ItemStack held = getHeldInfusedBronzeAxe(player);
        if (!held.isEmpty()) {
            return held;
        }

        for (int slot = 0; slot < player.getInventory().size(); slot++) {
            ItemStack stack = player.getInventory().getStack(slot);
            if (stack.isOf(ModItems.BRONZE_AXE) && SolarInfusionSystem.isInfused(stack)) {
                return stack;
            }
        }

        return ItemStack.EMPTY;
    }
}
