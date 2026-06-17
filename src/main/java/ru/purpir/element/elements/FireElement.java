package ru.purpir.element.elements;

import net.minecraft.text.Text;
import ru.purpir.element.Element;

public final class FireElement extends Element {
    public static final FireElement INSTANCE = new FireElement();

    private FireElement() {
        super("fire", Text.translatable("element.caveborn.fire"));
    }
}
