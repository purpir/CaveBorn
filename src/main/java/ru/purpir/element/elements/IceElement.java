package ru.purpir.element.elements;

import net.minecraft.text.Text;
import ru.purpir.element.Element;

public final class IceElement extends Element {
    public static final IceElement INSTANCE = new IceElement();

    private IceElement() {
        super("ice", Text.translatable("element.caveborn.ice"));
    }
}
