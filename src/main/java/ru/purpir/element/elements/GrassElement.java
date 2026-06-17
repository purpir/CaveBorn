package ru.purpir.element.elements;

import net.minecraft.text.Text;
import ru.purpir.element.Element;

public final class GrassElement extends Element {
    public static final GrassElement INSTANCE = new GrassElement();

    private GrassElement() {
        super("grass", Text.translatable("element.caveborn.grass"));
    }
}
