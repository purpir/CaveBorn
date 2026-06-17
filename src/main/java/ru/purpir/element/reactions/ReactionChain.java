package ru.purpir.element.reactions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class ReactionChain {
    public static final int MAX_LENGTH = 10;

    private final int id;
    private final List<UUID> entityUuids = new ArrayList<>();

    public ReactionChain(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public List<UUID> getEntityUuids() {
        return Collections.unmodifiableList(entityUuids);
    }

    public int size() {
        return entityUuids.size();
    }

    public boolean contains(UUID uuid) {
        return entityUuids.contains(uuid);
    }

    public boolean addEntity(UUID uuid) {
        if (entityUuids.size() >= MAX_LENGTH || entityUuids.contains(uuid)) {
            return false;
        }
        entityUuids.add(uuid);
        return true;
    }

    public boolean removeEntity(UUID uuid) {
        return entityUuids.remove(uuid);
    }

    public boolean isEmpty() {
        return entityUuids.isEmpty();
    }
}
