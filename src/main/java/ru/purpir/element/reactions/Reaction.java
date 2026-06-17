package ru.purpir.element.reactions;

import net.minecraft.entity.Entity;
import net.minecraft.text.Text;

import java.util.List;
import java.util.Objects;

public abstract class Reaction {
    private final String id;
    private final Text displayName;
    private final List<ru.purpir.element.Element> requiredElements;

    protected Reaction(String id, Text displayName, List<ru.purpir.element.Element> requiredElements) {
        this.id = id;
        this.displayName = displayName;
        this.requiredElements = requiredElements;
    }

    public String getId() {
        return id;
    }

    public Text getDisplayName() {
        return displayName;
    }

    public List<ru.purpir.element.Element> getRequiredElements() {
        return requiredElements;
    }

    public abstract void onActivate(Entity entity);

    public abstract void onDeactivate(Entity entity);

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Reaction reaction)) return false;
        return Objects.equals(id, reaction.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
