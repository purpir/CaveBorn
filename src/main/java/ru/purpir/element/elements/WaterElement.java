package ru.purpir.element.elements;

import net.minecraft.text.Text;
import ru.purpir.element.Element;

public final class WaterElement extends Element {
    public static final WaterElement INSTANCE = new WaterElement();

    private WaterElement() {
        super("water", Text.translatable("element.caveborn.water"));
    }
}
