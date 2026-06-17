package ru.purpir.element.reactions;

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.entity.EntityType;
import ru.purpir.element.Element;
import ru.purpir.element.ElementInstance;
import ru.purpir.element.ElementSavedData;

import java.util.*;

public class ReactionManager {
    private static final Map<String, Reaction> REGISTRY = new LinkedHashMap<>();
    private static final Map<UUID, Set<String>> activeReactions = new HashMap<>();
    private static final Map<Integer, ReactionChain> chains = new HashMap<>();
    private static final Map<UUID, Integer> entityToChain = new HashMap<>();
    private static boolean processingChainDamage = false;
    private static int nextChainId = 0;
    private static int tickCounter = 0;
    private static final int CHAIN_UPDATE_INTERVAL = 20;

    public static void register(Reaction reaction) {
        REGISTRY.put(reaction.getId(), reaction);
    }

    public static Reaction byId(String id) {
        return REGISTRY.get(id);
    }

    public static void registerEvents() {
        ServerTickEvents.END_SERVER_TICK.register(ReactionManager::tick);
        ServerLivingEntityEvents.ALLOW_DAMAGE.register((entity, source, amount) -> {
            if (amount > 0) shareDamage(entity, source, amount);
            return true;
        });
    }

    private static void tick(MinecraftServer server) {
        tickCounter++;

        ElementSavedData data = ElementSavedData.get(server);
        Set<UUID> loadedEntities = new HashSet<>();

        for (UUID uuid : data.getAllEntityUuids()) {
            Entity entity = findEntity(server, uuid);
            if (entity != null) {
                loadedEntities.add(uuid);
                updateEntityReactions(entity, data);
            }
        }

        activeReactions.keySet().retainAll(loadedEntities);

        if (tickCounter % CHAIN_UPDATE_INTERVAL == 0) {
            updateChains(server);
        }
    }

    private static void updateEntityReactions(Entity entity, ElementSavedData data) {
        List<ElementInstance> elements = data.getElements(entity);
        UUID uuid = entity.getUuid();
        Set<String> active = activeReactions.computeIfAbsent(uuid, k -> new HashSet<>());

        for (Reaction reaction : REGISTRY.values()) {
            boolean hasAll = reaction.getRequiredElements().stream()
                .allMatch(req -> elements.stream().anyMatch(e -> e.element() == req));

            if (hasAll && !active.contains(reaction.getId())) {
                active.add(reaction.getId());
                reaction.onActivate(entity);
            } else if (!hasAll && active.contains(reaction.getId())) {
                active.remove(reaction.getId());
                reaction.onDeactivate(entity);
            }
        }
    }

    private static void updateChains(MinecraftServer server) {
        List<CandidateEntity> candidates = new ArrayList<>();

        for (Map.Entry<UUID, Set<String>> entry : activeReactions.entrySet()) {
            if (!entry.getValue().contains("root_binding")) continue;
            Entity entity = findEntity(server, entry.getKey());
            if (entity != null && entity.isAlive()) {
                candidates.add(new CandidateEntity(entity, (ServerWorld) entity.getEntityWorld()));
            }
        }

        clearAllChains();
        boolean[] visited = new boolean[candidates.size()];

        for (int i = 0; i < candidates.size(); i++) {
            if (visited[i]) continue;

            ReactionChain chain = createChain();
            Queue<Integer> queue = new LinkedList<>();
            queue.add(i);

            while (!queue.isEmpty() && chain.size() < ReactionChain.MAX_LENGTH) {
                int idx = queue.poll();
                if (visited[idx]) continue;
                visited[idx] = true;

                CandidateEntity current = candidates.get(idx);
                chain.addEntity(current.uuid);
                entityToChain.put(current.uuid, chain.getId());

                for (int j = 0; j < candidates.size(); j++) {
                    if (visited[j]) continue;
                    CandidateEntity other = candidates.get(j);
                    if (current.world == other.world &&
                        current.entity.squaredDistanceTo(other.entity) <= 100.0) {
                        visited[j] = true;
                        chain.addEntity(other.uuid);
                        entityToChain.put(other.uuid, chain.getId());
                        if (chain.size() >= ReactionChain.MAX_LENGTH) break;
                    }
                }
            }
        }
    }

    private static void shareDamage(LivingEntity entity, DamageSource source, float amount) {
        if (processingChainDamage) return;

        Integer chainId = entityToChain.get(entity.getUuid());
        if (chainId == null) return;

        ReactionChain chain = chains.get(chainId);
        if (chain == null) return;

        float sharedDamage = amount / 2.0f;
        if (sharedDamage <= 0) return;

        processingChainDamage = true;
        try {
            ServerWorld world = (ServerWorld) entity.getEntityWorld();
            for (UUID memberUuid : chain.getEntityUuids()) {
                if (memberUuid.equals(entity.getUuid())) continue;

                Entity member = findEntity(entity.getEntityWorld().getServer(), memberUuid);
                if (member instanceof LivingEntity living && member.isAlive()) {
                    living.damage(world, source, sharedDamage);
                }
            }
        } finally {
            processingChainDamage = false;
        }
    }

    public static void removeEntityFromChains(UUID uuid) {
        Integer chainId = entityToChain.remove(uuid);
        if (chainId != null) {
            ReactionChain chain = chains.get(chainId);
            if (chain != null) {
                chain.removeEntity(uuid);
                if (chain.isEmpty()) {
                    chains.remove(chainId);
                }
            }
        }
    }

    private static Entity findEntity(MinecraftServer server, UUID uuid) {
        for (ServerWorld world : server.getWorlds()) {
            Entity entity = world.getEntity(uuid);
            if (entity != null) return entity;
        }
        return null;
    }

    private static ReactionChain createChain() {
        ReactionChain chain = new ReactionChain(nextChainId++);
        chains.put(chain.getId(), chain);
        return chain;
    }

    private static void clearAllChains() {
        chains.clear();
        entityToChain.clear();
    }

    private record CandidateEntity(Entity entity, ServerWorld world, UUID uuid) {
        CandidateEntity(Entity entity, ServerWorld world) {
            this(entity, world, entity.getUuid());
        }
    }
}
