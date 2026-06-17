package ru.purpir.element;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record ElementInstance(Element element, int remainingTicks) {
    public static final Codec<ElementInstance> CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
            Codec.STRING.fieldOf("element").forGetter(e -> e.element().getId()),
            Codec.INT.fieldOf("remaining_ticks").forGetter(ElementInstance::remainingTicks)
        ).apply(instance, (id, ticks) -> {
            Element element = Element.byId(id);
            if (element == null) {
                throw new IllegalArgumentException("Unknown element: " + id);
            }
            return new ElementInstance(element, ticks);
        })
    );

    public ElementInstance {
        if (element == null) {
            throw new IllegalArgumentException("Element cannot be null");
        }
    }
}
