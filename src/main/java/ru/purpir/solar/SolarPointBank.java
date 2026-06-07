package ru.purpir.solar;

import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import ru.purpir.network.ModPackets;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SolarPointBank {
    public static final int MAX_POINTS = 100;
    public static final int VOID_WOUND_GAIN = 5;
    public static final int VACUUMITE_SWORD_COST = 40;
    public static final int VACUUMITE_SWORD_ACTIVE_TICKS = 200;

    private static final Map<UUID, Integer> POINTS = new HashMap<>();
    private static final Map<UUID, Long> VACUUMITE_SWORD_ACTIVE_UNTIL = new HashMap<>();

    public static void register() {
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> sync(handler.player));
    }

    public static int getPoints(PlayerEntity player) {
        return POINTS.getOrDefault(player.getUuid(), 0);
    }

    public static void addVoidWoundPoints(ServerPlayerEntity player) {
        addPoints(player, VOID_WOUND_GAIN);
    }

    public static void addPoints(ServerPlayerEntity player, int amount) {
        int previous = getPoints(player);
        int updated = Math.min(MAX_POINTS, previous + Math.max(0, amount));
        if (updated != previous) {
            POINTS.put(player.getUuid(), updated);
            sync(player);
        }
    }

    public static boolean trySpendPoints(ServerPlayerEntity player, int amount) {
        int points = getPoints(player);
        if (points < amount) {
            sync(player);
            return false;
        }
        POINTS.put(player.getUuid(), points - amount);
        sync(player);
        return true;
    }

    public static boolean tryActivateVacuumiteSword(ServerPlayerEntity player, ServerWorld world) {
        int points = getPoints(player);
        if (points < VACUUMITE_SWORD_COST) {
            player.sendMessage(
                Text.literal(points + "/" + VACUUMITE_SWORD_COST + " солнечных очков").formatted(Formatting.RED),
                true
            );
            sync(player);
            return false;
        }

        POINTS.put(player.getUuid(), points - VACUUMITE_SWORD_COST);
        VACUUMITE_SWORD_ACTIVE_UNTIL.put(player.getUuid(), world.getTime() + VACUUMITE_SWORD_ACTIVE_TICKS);
        sync(player);

        world.spawnParticles(ParticleTypes.END_ROD, player.getX(), player.getBodyY(0.5), player.getZ(), 24, 0.45, 0.65, 0.45, 0.04);
        world.spawnParticles(ParticleTypes.REVERSE_PORTAL, player.getX(), player.getBodyY(0.5), player.getZ(), 18, 0.35, 0.45, 0.35, 0.03);
        world.playSound(null, player.getBlockPos(), SoundEvents.BLOCK_BEACON_ACTIVATE, SoundCategory.PLAYERS, 0.75F, 1.65F);
        player.sendMessage(Text.literal("Пустотная серия активна").formatted(Formatting.GOLD), true);
        return true;
    }

    public static boolean isVacuumiteSwordActive(PlayerEntity player, ServerWorld world) {
        long activeUntil = VACUUMITE_SWORD_ACTIVE_UNTIL.getOrDefault(player.getUuid(), 0L);
        if (activeUntil <= world.getTime()) {
            VACUUMITE_SWORD_ACTIVE_UNTIL.remove(player.getUuid());
            return false;
        }
        return true;
    }

    public static void sync(ServerPlayerEntity player) {
        if (ServerPlayNetworking.canSend(player, ModPackets.SolarPointsPayload.ID)) {
            ServerPlayNetworking.send(player, new ModPackets.SolarPointsPayload(getPoints(player)));
        }
    }
}
