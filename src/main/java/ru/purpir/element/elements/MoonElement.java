package ru.purpir.element.elements;

import net.minecraft.text.Text;
import ru.purpir.element.Element;

public final class MoonElement extends Element {
    public static final MoonElement INSTANCE = new MoonElement();

    private MoonElement() {
        super("moon", Text.translatable("element.caveborn.moon"));
    }
}
