package ru.purpir.element;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;

public class ElementTickHandler {

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(ElementTickHandler::tick);
    }

    private static void tick(MinecraftServer server) {
        if (server.getOverworld() != null) {
            ElementSavedData.get(server).tick();
        }
    }
}
