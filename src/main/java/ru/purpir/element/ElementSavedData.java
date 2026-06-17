package ru.purpir.element;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class ElementSavedData extends PersistentState {
    private static final String NAME = "caveborn_elements";
    private static final int MAX_ELEMENTS = 2;

    private final Map<UUID, List<ElementInstance>> elements = new HashMap<>();

    public static final Codec<ElementSavedData> CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
            Codec.unboundedMap(
                Codec.STRING.xmap(UUID::fromString, UUID::toString),
                ElementInstance.CODEC.listOf()
            ).fieldOf("elements").forGetter(data -> data.elements)
        ).apply(instance, ElementSavedData::new)
    );

    public static final PersistentStateType<ElementSavedData> TYPE = new PersistentStateType<>(
        NAME,
        context -> new ElementSavedData(),
        context -> CODEC,
        null
    );

    public ElementSavedData() {
    }

    private ElementSavedData(Map<UUID, List<ElementInstance>> elements) {
        for (Map.Entry<UUID, List<ElementInstance>> entry : elements.entrySet()) {
            List<ElementInstance> cleaned = new ArrayList<>();
            for (ElementInstance inst : entry.getValue()) {
                if (inst.remainingTicks() > 0 && cleaned.size() < MAX_ELEMENTS) {
                    cleaned.add(inst);
                }
            }
            if (!cleaned.isEmpty()) {
                this.elements.put(entry.getKey(), cleaned);
            }
        }
    }

    public static ElementSavedData get(MinecraftServer server) {
        return server.getOverworld().getPersistentStateManager().getOrCreate(TYPE);
    }

    public void addElement(Entity entity, Element element, int seconds) {
        int ticks = seconds * 20;
        UUID uuid = entity.getUuid();
        List<ElementInstance> list = elements.computeIfAbsent(uuid, k -> new ArrayList<>());

        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).element() == element) {
                int newTicks = Math.max(list.get(i).remainingTicks(), ticks);
                list.set(i, new ElementInstance(element, newTicks));
                markDirty();
                return;
            }
        }

        if (list.size() < MAX_ELEMENTS) {
            list.add(new ElementInstance(element, ticks));
            markDirty();
            return;
        }

        list.remove(0);
        list.add(new ElementInstance(element, ticks));
        markDirty();
    }

    public void removeElement(Entity entity, Element element) {
        UUID uuid = entity.getUuid();
        List<ElementInstance> list = elements.get(uuid);
        if (list == null || list.isEmpty()) {
            return;
        }

        list.removeIf(e -> e.element() == element);
        if (list.isEmpty()) {
            elements.remove(uuid);
        }
        markDirty();
    }

    public void clearEntity(Entity entity) {
        UUID uuid = entity.getUuid();
        if (elements.remove(uuid) != null) {
            markDirty();
        }
    }

    public List<ElementInstance> getElements(Entity entity) {
        return elements.getOrDefault(entity.getUuid(), List.of());
    }

    public Set<UUID> getAllEntityUuids() {
        return elements.keySet();
    }

    public boolean hasElement(Entity entity, Element element) {
        List<ElementInstance> list = elements.get(entity.getUuid());
        if (list == null) {
            return false;
        }
        return list.stream().anyMatch(e -> e.element() == element);
    }

    public void tick() {
        boolean changed = false;
        Iterator<Map.Entry<UUID, List<ElementInstance>>> iterator = elements.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<UUID, List<ElementInstance>> entry = iterator.next();
            List<ElementInstance> list = entry.getValue();
            List<ElementInstance> updated = new ArrayList<>();

            for (ElementInstance instance : list) {
                if (instance.remainingTicks() <= 1) {
                    changed = true;
                    continue;
                }
                updated.add(new ElementInstance(instance.element(), instance.remainingTicks() - 1));
                changed = true;
            }

            if (updated.isEmpty()) {
                iterator.remove();
            } else {
                entry.setValue(updated);
            }
        }

        if (changed) {
            markDirty();
        }
    }
}
