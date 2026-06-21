package ru.purpir.eventaltar;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.SkeletonEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.registry.RegistryKey;
import net.minecraft.world.World;
import ru.purpir.block.ModBlocks;
import ru.purpir.eventaltar.util.AltarMobAiUtil;
import ru.purpir.multiblock.MultiblockManager;
import ru.purpir.multiblock.MultiblockStructure;
import ru.purpir.network.ModPackets;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class EventAltarHandler {
    private static final int SCAN_INTERVAL_TICKS = 20;
    private static final int SCAN_RADIUS = 6;
    private static final Set<BlockPos> KNOWN_ALTARS = new HashSet<>();
    private static final Map<ChallengeCooldownKey, Long> CHALLENGE_COOLDOWNS = new HashMap<>();
    private static final List<ActiveChallenge> ACTIVE_CHALLENGES = new ArrayList<>();
    private static int tickTimer;

    private EventAltarHandler() {
    }

    public static void register() {
        ServerTickEvents.END_WORLD_TICK.register(EventAltarHandler::tickWorld);
        UseBlockCallback.EVENT.register(EventAltarHandler::onUseBlock);
        ServerPlayNetworking.registerGlobalReceiver(ModPackets.SubmitAltarTaskPayload.ID, (payload, context) ->
            context.server().execute(() -> openScreen(context.player(), new BlockPos(payload.x(), payload.y(), payload.z()))));
        ServerPlayNetworking.registerGlobalReceiver(ModPackets.AltarActionPayload.ID, (payload, context) ->
            context.server().execute(() -> handleAction(context.player(), payload.data())));
    }

    private static void tickWorld(ServerWorld world) {
        tickChallenges(world);
        if (++tickTimer < SCAN_INTERVAL_TICKS) {
            return;
        }
        tickTimer = 0;

        for (ServerPlayerEntity player : world.getPlayers()) {
            BlockPos center = player.getBlockPos();
            BlockPos.iterate(center.add(-SCAN_RADIUS, -SCAN_RADIUS, -SCAN_RADIUS), center.add(SCAN_RADIUS, SCAN_RADIUS, SCAN_RADIUS))
                .forEach(pos -> {
                    if (world.getBlockState(pos).isOf(Blocks.RESPAWN_ANCHOR) && isValidAltar(world, pos)) {
                        activateAltar(world, pos.toImmutable());
                    }
                });
        }
    }

    private static ActionResult onUseBlock(net.minecraft.entity.player.PlayerEntity player, World world, Hand hand, BlockHitResult hit) {
        if (world.isClient() || !(player instanceof ServerPlayerEntity serverPlayer)) {
            return ActionResult.PASS;
        }

        BlockPos origin = getAltarOrigin(world, hit.getBlockPos());
        if (origin == null) {
            return ActionResult.PASS;
        }

        openScreen(serverPlayer, origin);
        return ActionResult.SUCCESS;
    }

    public static void activateAltar(ServerWorld world, BlockPos origin) {
        if (KNOWN_ALTARS.contains(origin)) {
            return;
        }

        MultiblockManager manager = MultiblockManager.getInstance();
        if (manager.getStructureByOrigin(origin) == null) {
            MultiblockStructure structure = manager.createStructure(origin);
            addAltarLayer(world, structure, -2);
            addAltarLayer(world, structure, 2);
            addIfPresent(world, structure, new BlockPos(0, -1, 0));
            addIfPresent(world, structure, BlockPos.ORIGIN);
            addIfPresent(world, structure, new BlockPos(0, 1, 0));
            manager.registerStructure(structure);
        }

        KNOWN_ALTARS.add(origin);
        strikeLightning(world, origin);
        world.spawnParticles(ParticleTypes.END_ROD, origin.getX() + 0.5, origin.getY() + 1.4, origin.getZ() + 0.5, 70, 1.1, 0.8, 1.1, 0.08);
        world.spawnParticles(ParticleTypes.FIREWORK, origin.getX() + 0.5, origin.getY() + 1.2, origin.getZ() + 0.5, 35, 0.9, 0.55, 0.9, 0.08);
        world.playSound(null, origin, SoundEvents.BLOCK_BEACON_ACTIVATE, SoundCategory.BLOCKS, 1.2F, 0.75F);
    }

    private static void addAltarLayer(ServerWorld world, MultiblockStructure structure, int y) {
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                addIfPresent(world, structure, new BlockPos(x, y, z));
            }
        }
    }

    private static void addIfPresent(ServerWorld world, MultiblockStructure structure, BlockPos relativePos) {
        structure.addBlock(relativePos, world.getBlockState(structure.getOrigin().add(relativePos)));
    }

    private static void handleAction(ServerPlayerEntity player, String payloadData) {
        String[] parts = payloadData.split("\\|");
        if (parts.length < 5) {
            return;
        }

        String action = parts[0];
        BlockPos origin = new BlockPos(parseInt(parts[1]), parseInt(parts[2]), parseInt(parts[3]));
        int arg = parseInt(parts[4]);
        ServerWorld world = (ServerWorld) player.getEntityWorld();
        if (!isValidAltar(world, origin)) {
            player.sendMessage(net.minecraft.text.Text.translatable("event_altar.caveborn.message.broken"), true);
            return;
        }

        EventAltarSavedData data = EventAltarSavedData.get(world.getServer());
        EventAltarSavedData.QuestState quest = data.getQuest(arg);
        switch (action) {
            case "claim" -> {
                if (quest != null && !quest.completed() && !quest.isClaimed()) {
                    data.replaceQuest(quest.claim(player.getUuid()));
                }
            }
            case "cancel" -> {
                if (quest != null && quest.isClaimedBy(player.getUuid()) && !quest.completed()) {
                    data.replaceQuest(quest.cancel());
                }
            }
            case "finish" -> {
                if (quest != null && quest.isClaimedBy(player.getUuid()) && quest.rewardReady() && !quest.completed()) {
                    int xp = EventAltarQuestPool.xp(quest);
                    data.completeQuest(quest, xp);
                    playCompletion(world, origin, EventAltarQuestPool.rewards(quest, data.getAltarLevel(), world.random));
                }
            }
            case "wave" -> {
                startWaveChallenge(player, world, origin, data);
                return;
            }
            case "defend" -> {
                startDefendChallenge(player, world, origin, data);
                return;
            }
            default -> {
            }
        }
        openScreen(player, origin);
    }

    private static int parseInt(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException ignored) {
            return 0;
        }
    }

    public static void onMobKilled(ServerPlayerEntity player, LivingEntity killed) {
        if (killed instanceof HostileEntity) {
            progressQuest(player, EventAltarQuestPool.TYPE_KILL_HOSTILE, 1);
        }
        if (killed instanceof ZombieEntity ||
            killed.getType() == EntityType.SKELETON ||
            killed.getType() == EntityType.STRAY ||
            killed.getType() == EntityType.HUSK ||
            killed.getType() == EntityType.DROWNED ||
            killed.getType() == EntityType.WITHER_SKELETON) {
            progressQuest(player, EventAltarQuestPool.TYPE_KILL_UNDEAD, 1);
        }
    }

    public static void onBlockBroken(ServerPlayerEntity player, BlockState state) {
        if (state.isOf(Blocks.STONE)) {
            progressQuest(player, EventAltarQuestPool.TYPE_BREAK_STONE, 1);
        }
        if (state.isOf(Blocks.DEEPSLATE)) {
            progressQuest(player, EventAltarQuestPool.TYPE_BREAK_DEEPSLATE, 1);
        }
        if (state.isOf(Blocks.AMETHYST_BLOCK) || state.isOf(Blocks.BUDDING_AMETHYST) || state.isOf(Blocks.AMETHYST_CLUSTER)) {
            progressQuest(player, EventAltarQuestPool.TYPE_BREAK_AMETHYST, 1);
        }
    }

    public static void onSolarItemUsed(ServerPlayerEntity player, ItemStack stack) {
        if (!stack.isEmpty()) {
            progressSolarItemQuest(player, stack, 1);
        }
    }

    private static void progressQuest(ServerPlayerEntity player, int type, int amount) {
        EventAltarSavedData data = EventAltarSavedData.get(((ServerWorld) player.getEntityWorld()).getServer());
        UUID uuid = player.getUuid();
        for (EventAltarSavedData.QuestState quest : new ArrayList<>(data.getQuests())) {
            if (!quest.completed() && quest.type() == type && quest.isClaimedBy(uuid) && !quest.rewardReady()) {
                data.replaceQuest(quest.advance(amount));
                return;
            }
        }
    }

    private static void progressSolarItemQuest(ServerPlayerEntity player, ItemStack stack, int amount) {
        EventAltarSavedData data = EventAltarSavedData.get(((ServerWorld) player.getEntityWorld()).getServer());
        UUID uuid = player.getUuid();
        for (EventAltarSavedData.QuestState rawQuest : new ArrayList<>(data.getQuests())) {
            EventAltarSavedData.QuestState quest = EventAltarQuestPool.ensureSolarUseTarget(rawQuest);
            if (quest != rawQuest) {
                data.replaceQuest(quest);
            }
            if (!quest.completed() && quest.type() == EventAltarQuestPool.TYPE_USE_SOLAR_ITEM &&
                quest.isClaimedBy(uuid) && !quest.rewardReady() && stack.isOf(EventAltarQuestPool.solarUseTarget(quest))) {
                data.replaceQuest(quest.advance(amount));
                return;
            }
        }
    }

    private static void startWaveChallenge(ServerPlayerEntity player, ServerWorld world, BlockPos origin, EventAltarSavedData data) {
        if (isChallengeActiveAt(world, origin)) {
            player.sendMessage(net.minecraft.text.Text.translatable("event_altar.caveborn.challenge.active"), true);
            return;
        }
        if (isOnChallengeCooldown(player, world, 0)) {
            player.sendMessage(net.minecraft.text.Text.translatable("event_altar.caveborn.challenge.cooldown"), true);
            return;
        }
        CHALLENGE_COOLDOWNS.put(new ChallengeCooldownKey(player.getUuid(), 0), world.getTime() + 2400);
        sendScene(player, "wave|10|Испытание начнется через 10");
        ACTIVE_CHALLENGES.add(new ActiveChallenge(0, player.getUuid(), world.getRegistryKey(), origin, data.getAltarLevel()));
    }

    private static void startDefendChallenge(ServerPlayerEntity player, ServerWorld world, BlockPos origin, EventAltarSavedData data) {
        if (isChallengeActiveAt(world, origin)) {
            player.sendMessage(net.minecraft.text.Text.translatable("event_altar.caveborn.challenge.active"), true);
            return;
        }
        if (isOnChallengeCooldown(player, world, 1)) {
            player.sendMessage(net.minecraft.text.Text.translatable("event_altar.caveborn.challenge.cooldown"), true);
            return;
        }
        CHALLENGE_COOLDOWNS.put(new ChallengeCooldownKey(player.getUuid(), 1), world.getTime() + 2400);
        sendScene(player, "defend|5|Защита начнется через 5");
        ACTIVE_CHALLENGES.add(new ActiveChallenge(1, player.getUuid(), world.getRegistryKey(), origin, data.getAltarLevel()));
    }

    private static boolean isOnChallengeCooldown(ServerPlayerEntity player, ServerWorld world, int challengeType) {
        return CHALLENGE_COOLDOWNS.getOrDefault(new ChallengeCooldownKey(player.getUuid(), challengeType), 0L) > world.getTime();
    }

    private static int getChallengeCooldownSeconds(ServerPlayerEntity player, ServerWorld world, int challengeType) {
        long until = CHALLENGE_COOLDOWNS.getOrDefault(new ChallengeCooldownKey(player.getUuid(), challengeType), 0L);
        return (int) Math.max(0L, (until - world.getTime() + 19L) / 20L);
    }

    private static boolean isChallengeActiveAt(ServerWorld world, BlockPos origin) {
        for (ActiveChallenge challenge : ACTIVE_CHALLENGES) {
            if (challenge.worldKey.equals(world.getRegistryKey()) && challenge.origin.equals(origin)) {
                return true;
            }
        }
        return false;
    }

    private static void tickChallenges(ServerWorld world) {
        List<ActiveChallenge> finished = new ArrayList<>();
        for (ActiveChallenge challenge : ACTIVE_CHALLENGES) {
            if (!challenge.worldKey.equals(world.getRegistryKey())) {
                continue;
            }
            ServerPlayerEntity player = world.getServer().getPlayerManager().getPlayer(challenge.playerUuid);
            if (player == null) {
                finished.add(challenge);
                continue;
            }

            if (--challenge.countdown > 0) {
                continue;
            }

            if (challenge.spawned.isEmpty()) {
                spawnChallengeWave(world, challenge, player);
                continue;
            }

            int alive = 0;
            for (UUID uuid : new ArrayList<>(challenge.spawned)) {
                net.minecraft.entity.Entity entity = world.getEntity(uuid);
                if (entity instanceof MobEntity mob && mob.isAlive()) {
                    alive++;
                    if (challenge.type == 1) {
                        if (AltarMobAiUtil.isNearBlock(mob, challenge.origin, 2.2)) {
                            tickAltarAttack(world, challenge, mob);
                        } else {
                            AltarMobAiUtil.moveToBlock(mob, challenge.origin, 0.62 + challenge.level * 0.012);
                            challenge.altarAttackCooldowns.remove(uuid);
                        }
                    }
                } else {
                    challenge.altarAttackCooldowns.remove(uuid);
                }
            }

            if (challenge.type == 1 && challenge.hp <= 0) {
                player.sendMessage(net.minecraft.text.Text.translatable("event_altar.caveborn.challenge.failed"), true);
                cleanupChallenge(world, challenge);
                finished.add(challenge);
                continue;
            }

            player.sendMessage(net.minecraft.text.Text.literal((challenge.type == 1 ? "HP " + challenge.hp + "/100, " : "") + "Волна " + challenge.wave + "/" + challenge.maxWaves + ": осталось " + alive), true);
            if (alive <= 0) {
                challenge.spawned.clear();
                if (challenge.wave >= challenge.maxWaves) {
                    EventAltarSavedData data = EventAltarSavedData.get(world.getServer());
                    playCompletion(world, challenge.origin, challenge.type == 0
                        ? EventAltarChallengeRewards.waveRewards(data.getAltarLevel(), world.random)
                        : EventAltarChallengeRewards.defendRewards(data.getAltarLevel(), world.random));
                    data.addAltarXp((challenge.type == 0 ? 45 : 55) + data.getAltarLevel() * 5);
                    finished.add(challenge);
                } else {
                    challenge.countdown = 100;
                    sendScene(player, "wave|5|Следующая волна через 5");
                }
            }
        }
        ACTIVE_CHALLENGES.removeAll(finished);
    }

    private static void tickAltarAttack(ServerWorld world, ActiveChallenge challenge, MobEntity mob) {
        UUID uuid = mob.getUuid();
        int cooldown = challenge.altarAttackCooldowns.getOrDefault(uuid, 0);
        if (cooldown > 0) {
            challenge.altarAttackCooldowns.put(uuid, cooldown - 1);
            return;
        }

        challenge.hp -= 5;
        challenge.altarAttackCooldowns.put(uuid, 60);
        mob.swingHand(Hand.MAIN_HAND);
        mob.getNavigation().stop();
        world.playSound(null, challenge.origin, SoundEvents.ENTITY_ZOMBIE_ATTACK_WOODEN_DOOR, SoundCategory.HOSTILE, 0.75F, 0.85F);
        world.spawnParticles(ParticleTypes.CRIT, challenge.origin.getX() + 0.5, challenge.origin.getY() + 0.9, challenge.origin.getZ() + 0.5, 8, 0.35, 0.35, 0.35, 0.05);
    }

    private static void spawnChallengeWave(ServerWorld world, ActiveChallenge challenge, ServerPlayerEntity player) {
        challenge.wave++;
        int count = 3 + challenge.level + challenge.wave * 2;
        int spawned = 0;
        for (int i = 0; i < count; i++) {
            MobEntity mob = (i + challenge.wave + challenge.level) % 3 == 0
                ? EntityType.SKELETON.create(world, SpawnReason.TRIGGERED)
                : EntityType.ZOMBIE.create(world, SpawnReason.TRIGGERED);
            if (mob == null) {
                continue;
            }
            double angle = (Math.PI * 2.0 * i) / Math.max(1, count);
            double radius = 8.0 + world.random.nextDouble() * 4.0;
            BlockPos spawnPos = findChallengeSpawnPos(world, challenge.origin, angle, radius);
            mob.refreshPositionAndAngles(
                spawnPos.getX() + 0.5,
                spawnPos.getY(),
                spawnPos.getZ() + 0.5,
                world.random.nextFloat() * 360.0F,
                0.0F
            );
            if (mob instanceof SkeletonEntity skeleton) {
                skeleton.setStackInHand(Hand.MAIN_HAND, new ItemStack(Items.BOW));
            }
            mob.setGlowing(true);
            mob.setHealth(Math.min(mob.getMaxHealth(), mob.getHealth() + challenge.level * 1.5F));
            if (challenge.type == 0) {
                mob.setTarget(player);
            }
            if (world.spawnEntity(mob)) {
                spawned++;
                challenge.spawned.add(mob.getUuid());
            }
        }
        if (spawned == 0) {
            challenge.wave--;
            challenge.countdown = 20;
            return;
        }
        sendScene(player, "wave|1|Волна " + challenge.wave);
    }

    private static BlockPos findChallengeSpawnPos(ServerWorld world, BlockPos origin, double angle, double radius) {
        int baseX = origin.getX() + (int) Math.round(Math.cos(angle) * radius);
        int baseZ = origin.getZ() + (int) Math.round(Math.sin(angle) * radius);
        for (int dy = 8; dy >= -5; dy--) {
            BlockPos pos = new BlockPos(baseX, origin.getY() + dy, baseZ);
            if (world.getBlockState(pos.down()).isSolidBlock(world, pos.down()) &&
                world.getBlockState(pos).isAir() &&
                world.getBlockState(pos.up()).isAir()) {
                return pos;
            }
        }
        return new BlockPos(baseX, origin.getY(), baseZ);
    }

    private static void cleanupChallenge(ServerWorld world, ActiveChallenge challenge) {
        for (UUID uuid : challenge.spawned) {
            net.minecraft.entity.Entity entity = world.getEntity(uuid);
            if (entity instanceof MobEntity mob) {
                mob.discard();
            }
        }
        challenge.altarAttackCooldowns.clear();
    }

    private static void sendScene(ServerPlayerEntity player, String data) {
        ServerPlayNetworking.send(player, new ModPackets.AltarScenePayload(data));
    }

    private static class ActiveChallenge {
        private final int type;
        private final UUID playerUuid;
        private final RegistryKey<World> worldKey;
        private final BlockPos origin;
        private final int level;
        private final int maxWaves;
        private final List<UUID> spawned = new ArrayList<>();
        private final Map<UUID, Integer> altarAttackCooldowns = new HashMap<>();
        private int wave;
        private int countdown;
        private int hp = 100;

        private ActiveChallenge(int type, UUID playerUuid, RegistryKey<World> worldKey, BlockPos origin, int level) {
            this.type = type;
            this.playerUuid = playerUuid;
            this.worldKey = worldKey;
            this.origin = origin.toImmutable();
            this.level = level;
            this.maxWaves = Math.min(4, 2 + level / 4);
            this.countdown = 20;
        }
    }

    private record ChallengeCooldownKey(UUID playerUuid, int challengeType) {
    }

    private static void playCompletion(ServerWorld world, BlockPos origin, List<ItemStack> rewards) {
        world.spawnParticles(ParticleTypes.END_ROD, origin.getX() + 0.5, origin.getY() + 1.2, origin.getZ() + 0.5, 90, 1.2, 1.0, 1.2, 0.12);
        world.spawnParticles(ParticleTypes.GLOW, origin.getX() + 0.5, origin.getY() + 1.0, origin.getZ() + 0.5, 55, 1.0, 0.7, 1.0, 0.08);
        world.playSound(null, origin, SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundCategory.BLOCKS, 1.0F, 1.05F);
        world.playSound(null, origin, SoundEvents.BLOCK_RESPAWN_ANCHOR_CHARGE, SoundCategory.BLOCKS, 1.0F, 1.45F);

        for (ItemStack reward : rewards) {
            Vec3d rewardPos = chooseRewardDropPos(world, origin);
            ItemEntity rewardEntity = new ItemEntity(world, rewardPos.x, rewardPos.y, rewardPos.z, reward);
            rewardEntity.setVelocity(0.0, -0.08, 0.0);
            rewardEntity.setGlowing(true);
            world.spawnEntity(rewardEntity);
        }
    }

    private static Vec3d chooseRewardDropPos(ServerWorld world, BlockPos origin) {
        Random random = world.random;
        for (int attempt = 0; attempt < 24; attempt++) {
            int dx = random.nextBetween(-5, 5);
            int dz = random.nextBetween(-5, 5);
            int distanceSquared = dx * dx + dz * dz;
            if (distanceSquared < 9 || distanceSquared > 25) {
                continue;
            }

            double x = origin.getX() + 0.5 + dx;
            double y = origin.getY() + 9.0;
            double z = origin.getZ() + 0.5 + dz;
            if (!isTooCloseToPlayer(world, x, z)) {
                return new Vec3d(x, y, z);
            }
        }

        return new Vec3d(origin.getX() + 5.5, origin.getY() + 9.0, origin.getZ() + 0.5);
    }

    private static boolean isTooCloseToPlayer(ServerWorld world, double x, double z) {
        for (ServerPlayerEntity player : world.getPlayers()) {
            double dx = player.getX() - x;
            double dz = player.getZ() - z;
            if (dx * dx + dz * dz < 6.25) {
                return true;
            }
        }
        return false;
    }

    public static void openScreen(ServerPlayerEntity player, BlockPos origin) {
        ServerWorld world = (ServerWorld) player.getEntityWorld();
        EventAltarSavedData data = EventAltarSavedData.get(world.getServer());
        ServerPlayNetworking.send(player, new ModPackets.OpenAltarScreenPayload(encodeScreenData(world, origin, data, player)));
    }

    private static String encodeScreenData(ServerWorld world, BlockPos origin, EventAltarSavedData data, ServerPlayerEntity viewer) {
        StringBuilder builder = new StringBuilder();
        builder.append(origin.getX()).append(';')
            .append(origin.getY()).append(';')
            .append(origin.getZ()).append(';')
            .append(data.getAltarLevel()).append(';')
            .append(data.getAltarXp()).append(';')
            .append(data.getXpForNextLevel()).append(';')
            .append(data.getTotalCompleted()).append(';');
        for (EventAltarSavedData.QuestState quest : data.getQuests()) {
            if (quest.completed()) {
                continue;
            }
            builder.append(quest.id()).append(',')
                .append(quest.type()).append(',')
                .append(quest.rarity()).append(',')
                .append(quest.target()).append(',')
                .append(quest.progress()).append(',')
                .append(quest.targetItem()).append(',')
                .append(quest.isClaimed() ? 1 : 0).append(',')
                .append(quest.isClaimedBy(viewer.getUuid()) ? 1 : 0).append(',')
                .append(quest.rewardReady() ? 1 : 0).append('|');
        }
        builder.append(';')
            .append(data.getBoardRefreshRemainingSeconds()).append(';')
            .append(getChallengeCooldownSeconds(viewer, world, 0)).append(';')
            .append(getChallengeCooldownSeconds(viewer, world, 1)).append(';')
            .append(isChallengeActiveAt(world, origin) ? 1 : 0);
        return builder.toString();
    }

    private static void strikeLightning(ServerWorld world, BlockPos origin) {
        LightningEntity lightning = EntityType.LIGHTNING_BOLT.create(world, SpawnReason.TRIGGERED);
        if (lightning != null) {
            lightning.refreshPositionAfterTeleport(Vec3d.ofBottomCenter(origin.up()));
            world.spawnEntity(lightning);
        }
    }

    private static BlockPos getAltarOrigin(World world, BlockPos partPos) {
        MultiblockStructure structure = MultiblockManager.getInstance().getStructureAt(partPos);
        if (structure != null && isValidAltar(world, structure.getOrigin())) {
            return structure.getOrigin();
        }

        for (BlockPos pos : BlockPos.iterate(partPos.add(-2, -2, -2), partPos.add(2, 2, 2))) {
            if (world.getBlockState(pos).isOf(Blocks.RESPAWN_ANCHOR) && isValidAltar(world, pos)) {
                if (world instanceof ServerWorld serverWorld) {
                    activateAltar(serverWorld, pos.toImmutable());
                }
                return pos.toImmutable();
            }
        }
        return null;
    }

    public static boolean isEventAltarOrigin(World world, BlockPos origin) {
        return origin != null && isValidAltar(world, origin);
    }

    public static boolean isValidAltar(World world, BlockPos origin) {
        if (!world.getBlockState(origin).isOf(Blocks.RESPAWN_ANCHOR)) {
            return false;
        }

        return isCrimsonWood(world, origin.down())
            && isCrimsonWood(world, origin.up())
            && checkLayer(world, origin, -2)
            && checkLayer(world, origin, 2);
    }

    private static boolean checkLayer(World world, BlockPos origin, int y) {
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                Block expected = (Math.abs(x) + Math.abs(z) == 1) ? Blocks.GOLD_BLOCK : ModBlocks.BRONZE_BLOCK;
                if (!world.getBlockState(origin.add(x, y, z)).isOf(expected)) {
                    return false;
                }
            }
        }
        return true;
    }

    private static boolean isCrimsonWood(World world, BlockPos pos) {
        return world.getBlockState(pos).isOf(Blocks.CRIMSON_STEM)
            || world.getBlockState(pos).isOf(Blocks.CRIMSON_HYPHAE)
            || world.getBlockState(pos).isOf(Blocks.STRIPPED_CRIMSON_STEM)
            || world.getBlockState(pos).isOf(Blocks.STRIPPED_CRIMSON_HYPHAE);
    }
}
