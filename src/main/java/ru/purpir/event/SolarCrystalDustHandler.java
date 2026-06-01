package ru.purpir.event;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import ru.purpir.enchantment.SolarInfusionSystem;
import ru.purpir.item.ModItems;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

public class SolarCrystalDustHandler {
    private static final int DURATION_TICKS = 100;
    private static final int COOLDOWN_TICKS = 300;
    private static final int WITHER_TICKS = 100;
    private static final float DAMAGE = 3.0F;
    private static final List<DustField> FIELDS = new ArrayList<>();

    public static void register() {
        UseBlockCallback.EVENT.register(SolarCrystalDustHandler::onUseBlock);
        ServerTickEvents.END_WORLD_TICK.register(SolarCrystalDustHandler::onWorldTick);
    }

    private static ActionResult onUseBlock(PlayerEntity player, net.minecraft.world.World world, Hand hand, BlockHitResult hitResult) {
        ItemStack stack = player.getStackInHand(hand);
        if (!stack.isOf(ModItems.CRYSTAL_DUST) || !SolarInfusionSystem.isInfused(stack)) {
            return ActionResult.PASS;
        }

        if (world.isClient()) {
            return ActionResult.SUCCESS;
        }

        if (player.getItemCooldownManager().isCoolingDown(stack)) {
            return ActionResult.FAIL;
        }

        ServerWorld serverWorld = (ServerWorld) world;
        BlockPos center = hitResult.getBlockPos();
        long currentTime = serverWorld.getTime();
        FIELDS.add(new DustField(serverWorld, center, player.getUuid(), currentTime + DURATION_TICKS));

        if (!player.isCreative()) {
            stack.decrement(1);
        }
        player.getItemCooldownManager().set(stack, COOLDOWN_TICKS);

        spawnBurst(serverWorld, center);
        serverWorld.playSound(null, center, SoundEvents.BLOCK_AMETHYST_CLUSTER_BREAK, SoundCategory.BLOCKS, 0.9f, 1.6f);
        serverWorld.playSound(null, center, SoundEvents.BLOCK_SAND_PLACE, SoundCategory.BLOCKS, 0.55f, 1.25f);

        return ActionResult.SUCCESS;
    }

    private static void onWorldTick(ServerWorld world) {
        long time = world.getTime();
        Iterator<DustField> iterator = FIELDS.iterator();
        while (iterator.hasNext()) {
            DustField field = iterator.next();
            if (field.world != world) {
                continue;
            }

            if (time >= field.expiresAt) {
                iterator.remove();
                continue;
            }

            field.spawnParticles(time);
            if (time >= field.nextDamageAt) {
                field.damageEntities(time);
            }
        }
    }

    private static void spawnBurst(ServerWorld world, BlockPos center) {
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                world.spawnParticles(
                    ParticleTypes.GLOW,
                    center.getX() + x + 0.5,
                    center.getY() + 1.05,
                    center.getZ() + z + 0.5,
                    8,
                    0.35,
                    0.02,
                    0.35,
                    0.015
                );
            }
        }
    }

    private static class DustField {
        private final ServerWorld world;
        private final BlockPos center;
        private final UUID owner;
        private final long expiresAt;
        private long nextDamageAt;

        private DustField(ServerWorld world, BlockPos center, UUID owner, long expiresAt) {
            this.world = world;
            this.center = center;
            this.owner = owner;
            this.expiresAt = expiresAt;
            this.nextDamageAt = world.getTime();
        }

        private void spawnParticles(long time) {
            if (time % 4 != 0) {
                return;
            }

            for (int x = -1; x <= 1; x++) {
                for (int z = -1; z <= 1; z++) {
                    world.spawnParticles(
                        ParticleTypes.WITCH,
                        center.getX() + x + 0.5,
                        center.getY() + 1.02,
                        center.getZ() + z + 0.5,
                        2,
                        0.38,
                        0.01,
                        0.38,
                        0.0
                    );
                    world.spawnParticles(
                        ParticleTypes.GLOW,
                        center.getX() + x + 0.5,
                        center.getY() + 1.04,
                        center.getZ() + z + 0.5,
                        1,
                        0.28,
                        0.01,
                        0.28,
                        0.0
                    );
                }
            }
        }

        private void damageEntities(long time) {
            nextDamageAt = time + 20;
            Box box = new Box(
                center.getX() - 1.0,
                center.getY() + 0.02,
                center.getZ() - 1.0,
                center.getX() + 2.0,
                center.getY() + 2.0,
                center.getZ() + 2.0
            );

            for (LivingEntity target : world.getEntitiesByClass(LivingEntity.class, box, LivingEntity::isAlive)) {
                if (target.getUuid().equals(owner)) {
                    continue;
                }

                target.damage(world, world.getDamageSources().magic(), DAMAGE);
                target.addStatusEffect(new StatusEffectInstance(StatusEffects.WITHER, WITHER_TICKS, 0));
            }
        }
    }
}
