package ru.purpir.element;

import ru.purpir.Caveborn;
import ru.purpir.element.elements.FireElement;
import ru.purpir.element.elements.GrassElement;
import ru.purpir.element.elements.IceElement;
import ru.purpir.element.elements.MoonElement;
import ru.purpir.element.elements.WaterElement;

public final class ModElements {
    private ModElements() {
    }

    public static void register() {
        Element.register(FireElement.INSTANCE);
        Element.register(WaterElement.INSTANCE);
        Element.register(IceElement.INSTANCE);
        Element.register(GrassElement.INSTANCE);
        Element.register(MoonElement.INSTANCE);

        Caveborn.LOGGER.info("Registering CaveBorn elements");
    }
}
