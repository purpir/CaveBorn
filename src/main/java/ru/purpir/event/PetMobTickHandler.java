package ru.purpir.event;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.world.ServerWorld;
import ru.purpir.util.PetMobUtil;

/**
 * Обработчик тиков для временных питомцев
 */
public class PetMobTickHandler {
    
    public static void register() {
        ServerTickEvents.END_WORLD_TICK.register(PetMobTickHandler::onWorldTick);
    }
    
    private static void onWorldTick(ServerWorld world) {
        PetMobUtil.tickTemporaryPets(world);
    }
}
