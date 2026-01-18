package ru.purpir.screen;

import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.resource.featuretoggle.FeatureFlags;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.Identifier;
import ru.purpir.Caveborn;

public class ModScreenHandlers {
    public static final ScreenHandlerType<BagScreenHandler> BAG_SCREEN_HANDLER = 
        Registry.register(Registries.SCREEN_HANDLER, Identifier.of(Caveborn.MOD_ID, "bag"),
            new ScreenHandlerType<>((syncId, playerInventory) -> 
                new BagScreenHandler(syncId, playerInventory, playerInventory.player.getMainHandStack()), 
                FeatureFlags.VANILLA_FEATURES));

    public static void register() {
        Caveborn.LOGGER.info("Registering Screen Handlers for " + Caveborn.MOD_ID);
    }
}
