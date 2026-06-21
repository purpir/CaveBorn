package ru.purpir.element.reactions;

import ru.purpir.Caveborn;

public final class ModReactions {
    private ModReactions() {
    }

    public static void register() {
        ReactionManager.register(RootBindingReaction.INSTANCE);

        Caveborn.LOGGER.info("Registering CaveBorn element reactions");
    }
}
