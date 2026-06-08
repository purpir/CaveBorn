package ru.purpir.event;

import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.block.BlockState;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.EntityAnimationS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import ru.purpir.enchantment.SolarInfusionSystem;
import ru.purpir.item.ModItems;
import ru.purpir.solar.SolarPointBank;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class SolarZincKnifeHandler {
    private static final int SOLO_DASH_COST = 5;
    private static final int DUO_DASH_COST = 10;
    private static final int DUO_POINTS_GAIN = 10;
    private static final int ATTACK_COOLDOWN_TICKS = 7;
    private static final int COMBO_RESET_TICKS = 45;
    private static final float INFUSED_KNIFE_DAMAGE = 6.0F;
    private static final float DASH_DAMAGE = 2.0F;
    private static final Map<UUID, ComboState> COMBOS = new HashMap<>();
    private static final Map<UUID, Long> NEXT_ATTACK = new HashMap<>();

    private SolarZincKnifeHandler() {
    }

    public static void register() {
        AttackEntityCallback.EVENT.register(SolarZincKnifeHandler::onAttackEntity);
        UseItemCallback.EVENT.register(SolarZincKnifeHandler::onUseItem);
    }

    private static ActionResult onAttackEntity(PlayerEntity player, World world, Hand hand, net.minecraft.entity.Entity entity,
                                               net.minecraft.util.hit.EntityHitResult hitResult) {
        if (world.isClient() || !(world instanceof ServerWorld serverWorld) || !(player instanceof ServerPlayerEntity serverPlayer) ||
            !(entity instanceof LivingEntity target) || !hasDuoInfusedKnives(player)) {
            return ActionResult.PASS;
        }

        long now = serverWorld.getTime();
        if (NEXT_ATTACK.getOrDefault(player.getUuid(), 0L) > now) {
            return ActionResult.PASS;
        }
        NEXT_ATTACK.put(player.getUuid(), now + ATTACK_COOLDOWN_TICKS);

        ComboState combo = COMBOS.getOrDefault(player.getUuid(), new ComboState(0, 0L));
        int step = combo.expiresAt() > now ? combo.step() : 0;
        ItemStack off = player.getOffHandStack();

        if (step == 0) {
            spawnMainStrike(serverWorld, target);
            COMBOS.put(player.getUuid(), new ComboState(1, now + COMBO_RESET_TICKS));
            return ActionResult.PASS;
        } else if (step == 1) {
            strike(serverWorld, serverPlayer, target, off, EquipmentSlot.OFFHAND, INFUSED_KNIFE_DAMAGE, false);
            playOffhandSwing(serverPlayer);
            COMBOS.put(player.getUuid(), new ComboState(2, now + COMBO_RESET_TICKS));
            return ActionResult.SUCCESS;
        } else {
            strike(serverWorld, serverPlayer, target, off, EquipmentSlot.OFFHAND, INFUSED_KNIFE_DAMAGE, true);
            target.setOnFireFor(4.0F);
            SolarPointBank.addPoints(serverPlayer, DUO_POINTS_GAIN);
            playOffhandSwing(serverPlayer);
            COMBOS.put(player.getUuid(), new ComboState(0, now + COMBO_RESET_TICKS));
            spawnDoubleStrike(serverWorld, target);
            return ActionResult.PASS;
        }
    }

    private static ActionResult onUseItem(PlayerEntity player, World world, Hand hand) {
        ItemStack stack = player.getStackInHand(hand);
        if (!stack.isOf(ModItems.ZINC_KNIFE) || !SolarInfusionSystem.isInfused(stack)) {
            return ActionResult.PASS;
        }

        if (world.isClient()) {
            return ActionResult.SUCCESS;
        }

        if (!(player instanceof ServerPlayerEntity serverPlayer) || !(world instanceof ServerWorld serverWorld)) {
            return ActionResult.PASS;
        }

        boolean duo = hasDuoInfusedKnives(player);
        int cost = duo ? DUO_DASH_COST : SOLO_DASH_COST;
        if (!SolarPointBank.trySpendPoints(serverPlayer, cost)) {
            serverWorld.playSound(null, player.getBlockPos(), SoundEvents.BLOCK_NOTE_BLOCK_BASS.value(), SoundCategory.PLAYERS, 0.6F, 0.7F);
            return ActionResult.FAIL;
        }

        dash(serverWorld, serverPlayer, duo ? 4.0D : 2.0D, duo);
        return ActionResult.SUCCESS;
    }

    private static void strike(ServerWorld world, ServerPlayerEntity player, LivingEntity target, ItemStack knife,
                               EquipmentSlot slot, float damage, boolean quiet) {
        target.damage(world, player.getDamageSources().playerAttack(player), damage);
        if (!player.isCreative()) {
            knife.damage(1, player, slot);
        }
        if (!quiet) {
            world.spawnParticles(ParticleTypes.SWEEP_ATTACK, target.getX(), target.getBodyY(0.5), target.getZ(), 1, 0.0, 0.0, 0.0, 0.0);
            world.playSound(null, target.getBlockPos(), SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP, SoundCategory.PLAYERS, 0.55F, 1.45F);
        }
    }

    private static void playOffhandSwing(ServerPlayerEntity player) {
        EntityAnimationS2CPacket packet = new EntityAnimationS2CPacket(player, EntityAnimationS2CPacket.SWING_OFF_HAND);
        for (ServerPlayerEntity viewer : ((ServerWorld) player.getEntityWorld()).getPlayers()) {
            viewer.networkHandler.sendPacket(packet);
        }
    }

    private static void dash(ServerWorld world, ServerPlayerEntity player, double distance, boolean duo) {
        Vec3d direction = player.getRotationVector().multiply(1.0D, 0.0D, 1.0D);
        if (direction.lengthSquared() < 0.001D) {
            return;
        }
        direction = direction.normalize();
        Vec3d start = new Vec3d(player.getX(), player.getY(), player.getZ());
        Vec3d target = findDashTarget(world, player, start, direction, distance);

        if (duo) {
            damageDashTargets(world, player, start, target);
        }

        player.requestTeleport(target.x, target.y, target.z);
        player.setVelocity(direction.multiply(0.35D).add(0.0D, 0.05D, 0.0D));
        player.velocityDirty = true;
        player.networkHandler.sendPacket(new EntityVelocityUpdateS2CPacket(player));

        double traveled = target.subtract(start).length();
        int particles = Math.max(8, (int) (traveled * 8.0D));
        for (int i = 0; i < particles; i++) {
            double progress = i / (double) particles;
            Vec3d pos = start.lerp(target, progress);
            world.spawnParticles(duo ? ParticleTypes.FLAME : ParticleTypes.END_ROD, pos.x, pos.y + 0.55D, pos.z, 2, 0.12, 0.08, 0.12, 0.015);
        }
        world.playSound(null, player.getBlockPos(), SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP, SoundCategory.PLAYERS, 0.8F, duo ? 1.65F : 1.35F);
    }

    private static Vec3d findDashTarget(ServerWorld world, ServerPlayerEntity player, Vec3d start, Vec3d direction, double distance) {
        Vec3d best = start;
        for (double step = 0.35D; step <= distance; step += 0.25D) {
            Vec3d candidate = start.add(direction.multiply(step));
            BlockPos feet = BlockPos.ofFloored(candidate.x, candidate.y, candidate.z);
            if (canStandAt(world, feet, player)) {
                best = new Vec3d(candidate.x, candidate.y, candidate.z);
                continue;
            }

            BlockPos up = feet.up();
            if (canStandAt(world, up, player)) {
                best = new Vec3d(candidate.x, candidate.y + 1.0D, candidate.z);
                continue;
            }
            break;
        }
        return best;
    }

    private static boolean canStandAt(ServerWorld world, BlockPos feet, ServerPlayerEntity player) {
        BlockState feetState = world.getBlockState(feet);
        BlockState headState = world.getBlockState(feet.up());
        return feetState.getCollisionShape(world, feet).isEmpty()
            && headState.getCollisionShape(world, feet.up()).isEmpty()
            && world.getWorldBorder().contains(feet)
            && feet.getY() > world.getBottomY()
            && feet.getY() < 320;
    }

    private static void damageDashTargets(ServerWorld world, ServerPlayerEntity player, Vec3d start, Vec3d end) {
        Box area = new Box(start, end).expand(1.2D, 0.9D, 1.2D);
        for (LivingEntity target : world.getEntitiesByClass(LivingEntity.class, area, LivingEntity::isAlive)) {
            if (target == player) {
                continue;
            }
            target.damage(world, player.getDamageSources().playerAttack(player), DASH_DAMAGE);
            target.setOnFireFor(3.0F);
            world.spawnParticles(ParticleTypes.FLAME, target.getX(), target.getBodyY(0.5), target.getZ(), 8, 0.2, 0.25, 0.2, 0.02);
        }
    }

    private static boolean hasDuoInfusedKnives(PlayerEntity player) {
        return isInfusedZincKnife(player.getMainHandStack()) && isInfusedZincKnife(player.getOffHandStack());
    }

    private static boolean isInfusedZincKnife(ItemStack stack) {
        return stack.isOf(ModItems.ZINC_KNIFE) && SolarInfusionSystem.isInfused(stack);
    }

    private static void spawnDoubleStrike(ServerWorld world, LivingEntity target) {
        world.spawnParticles(ParticleTypes.FLAME, target.getX(), target.getBodyY(0.55), target.getZ(), 16, 0.35, 0.35, 0.35, 0.03);
        world.spawnParticles(ParticleTypes.CRIT, target.getX(), target.getBodyY(0.55), target.getZ(), 18, 0.35, 0.35, 0.35, 0.08);
        world.playSound(null, target.getBlockPos(), SoundEvents.ITEM_FIRECHARGE_USE, SoundCategory.PLAYERS, 0.7F, 1.45F);
    }

    private static void spawnMainStrike(ServerWorld world, LivingEntity target) {
        world.spawnParticles(ParticleTypes.CRIT, target.getX(), target.getBodyY(0.55), target.getZ(), 5, 0.2, 0.2, 0.2, 0.04);
    }

    private record ComboState(int step, long expiresAt) {
    }
}
