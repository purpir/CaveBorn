package ru.purpir.element.reactions;

import net.minecraft.entity.Entity;
import net.minecraft.text.Text;
import ru.purpir.element.Element;
import ru.purpir.element.elements.GrassElement;
import ru.purpir.element.elements.WaterElement;

import java.util.List;

public class RootBindingReaction extends Reaction {
    public static final RootBindingReaction INSTANCE = new RootBindingReaction();

    private RootBindingReaction() {
        super("root_binding", Text.translatable("reaction.caveborn.root_binding"),
            List.of(GrassElement.INSTANCE, WaterElement.INSTANCE));
    }

    @Override
    public void onActivate(Entity entity) {
    }

    @Override
    public void onDeactivate(Entity entity) {
        ReactionManager.removeEntityFromChains(entity.getUuid());
    }
}
