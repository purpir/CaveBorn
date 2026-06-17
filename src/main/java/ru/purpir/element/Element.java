package ru.purpir.element;

import net.minecraft.text.Text;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

public abstract class Element {
    private static final Map<String, Element> REGISTRY = new LinkedHashMap<>();

    private final String id;
    private final Text displayName;

    protected Element(String id, Text displayName) {
        this.id = id;
        this.displayName = displayName;
    }

    public String getId() {
        return id;
    }

    public Text getDisplayName() {
        return displayName;
    }

    public static void register(Element element) {
        REGISTRY.put(element.getId(), element);
    }

    public static Element byId(String id) {
        return REGISTRY.get(id);
    }

    public static Collection<Element> values() {
        return REGISTRY.values();
    }
}
