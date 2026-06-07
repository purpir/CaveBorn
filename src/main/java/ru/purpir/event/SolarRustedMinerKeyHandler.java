package ru.purpir.event;

import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.WorldChunk;
import ru.purpir.block.LockedMinerCrateBlock;
import ru.purpir.block.entity.LockedMinerCrateBlockEntity;
import ru.purpir.block.ModBlocks;
import ru.purpir.enchantment.SolarInfusionSystem;
import ru.purpir.item.ModItems;

public final class SolarRustedMinerKeyHandler {
    private static final int SEARCH_RADIUS = 500;
    private static final int COOLDOWN_TICKS = 100;

    private SolarRustedMinerKeyHandler() {
    }

    public static void register() {
        UseItemCallback.EVENT.register(SolarRustedMinerKeyHandler::onUseItem);
    }

    private static ActionResult onUseItem(PlayerEntity player, net.minecraft.world.World world, Hand hand) {
        ItemStack stack = player.getStackInHand(hand);
        if (!stack.isOf(ModItems.RUSTED_MINER_KEY) || !SolarInfusionSystem.isInfused(stack)) {
            return ActionResult.PASS;
        }

        if (world.isClient()) {
            return ActionResult.SUCCESS;
        }

        if (!(player instanceof ServerPlayerEntity serverPlayer) || !(world instanceof ServerWorld serverWorld)) {
            return ActionResult.FAIL;
        }

        if (player.getItemCooldownManager().isCoolingDown(stack)) {
            return ActionResult.FAIL;
        }

        BlockPos cratePos = findNearestLockedCrate(serverWorld, player.getBlockPos());
        if (cratePos == null) {
            player.sendMessage(Text.translatable("ability.caveborn.rusted_key.not_found"), true);
        } else {
            int distance = (int) Math.ceil(Math.sqrt(player.getBlockPos().getSquaredDistance(cratePos)));
            player.sendMessage(Text.translatable("ability.caveborn.rusted_key.distance", distance), true);
            serverWorld.spawnParticles(ParticleTypes.END_ROD, player.getX(), player.getBodyY(0.55), player.getZ(), 12, 0.25, 0.35, 0.25, 0.015);
        }

        spawnCallOfSun(serverWorld, player);
        player.getItemCooldownManager().set(stack, COOLDOWN_TICKS);
        serverWorld.playSound(null, player.getBlockPos(), SoundEvents.BLOCK_AMETHYST_BLOCK_CHIME, SoundCategory.PLAYERS, 0.65F, 1.45F);
        serverWorld.playSound(null, player.getBlockPos(), SoundEvents.BLOCK_BEACON_POWER_SELECT, SoundCategory.PLAYERS, 0.9F, 1.35F);
        serverWorld.playSound(null, player.getBlockPos(), SoundEvents.ENTITY_BLAZE_SHOOT, SoundCategory.PLAYERS, 0.55F, 0.65F);
        damageKey(serverWorld, serverPlayer, stack);
        return ActionResult.SUCCESS;
    }

    private static void spawnCallOfSun(ServerWorld world, PlayerEntity player) {
        for (int i = 0; i <= 10; i++) {
            double y = player.getY() + 0.35 + i;
            double radius = 0.22 + i * 0.035;
            world.spawnParticles(ParticleTypes.FLAME, player.getX(), y, player.getZ(), 7, radius, 0.02, radius, 0.018);
            world.spawnParticles(ParticleTypes.END_ROD, player.getX(), y, player.getZ(), 4, radius * 0.7, 0.02, radius * 0.7, 0.012);
            if (i % 2 == 0) {
                world.spawnParticles(ParticleTypes.GLOW, player.getX(), y, player.getZ(), 3, radius, 0.02, radius, 0.0);
            }
        }
    }

    private static BlockPos findNearestLockedCrate(ServerWorld world, BlockPos origin) {
        int radiusChunks = (SEARCH_RADIUS + 15) / 16;
        ChunkPos centerChunk = new ChunkPos(origin);
        BlockPos nearest = null;
        double nearestDistance = SEARCH_RADIUS * SEARCH_RADIUS;

        for (int chunkX = centerChunk.x - radiusChunks; chunkX <= centerChunk.x + radiusChunks; chunkX++) {
            for (int chunkZ = centerChunk.z - radiusChunks; chunkZ <= centerChunk.z + radiusChunks; chunkZ++) {
                Chunk chunk = world.getChunk(chunkX, chunkZ, ChunkStatus.FULL, false);
                if (!(chunk instanceof WorldChunk worldChunk)) {
                    continue;
                }

                for (BlockEntity blockEntity : worldChunk.getBlockEntities().values()) {
                    if (!(blockEntity instanceof LockedMinerCrateBlockEntity)) {
                        continue;
                    }

                    BlockPos pos = blockEntity.getPos();
                    double distance = origin.getSquaredDistance(pos);
                    if (distance >= nearestDistance) {
                        continue;
                    }

                    BlockState state = world.getBlockState(pos);
                    if (state.isOf(ModBlocks.LOCKED_MINER_CRATE) && state.get(LockedMinerCrateBlock.LOCKED)) {
                        nearest = pos.toImmutable();
                        nearestDistance = distance;
                    }
                }
            }
        }

        if (nearest != null) {
            return nearest;
        }

        for (BlockPos pos : BlockPos.iterateOutwards(origin, 32, 32, 32)) {
            BlockState state = world.getBlockState(pos);
            if (state.isOf(ModBlocks.LOCKED_MINER_CRATE) && state.get(LockedMinerCrateBlock.LOCKED)) {
                return pos.toImmutable();
            }
        }

        return null;
    }

    private static void damageKey(ServerWorld world, ServerPlayerEntity player, ItemStack stack) {
        if (player.isCreative()) {
            return;
        }

        boolean willBreak = stack.getDamage() >= stack.getMaxDamage() - 1;
        stack.damage(1, player, player.getPreferredEquipmentSlot(stack));
        if (willBreak) {
            LightningEntity lightning = EntityType.LIGHTNING_BOLT.create(world, SpawnReason.TRIGGERED);
            if (lightning != null) {
                lightning.refreshPositionAfterTeleport(Vec3d.ofBottomCenter(player.getBlockPos()));
                world.spawnEntity(lightning);
            }
        }
    }
}
