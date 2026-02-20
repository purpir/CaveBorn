package ru.purpir.event;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.minecraft.server.world.ServerWorld;
import ru.purpir.multiblock.MultiblockManager;
import ru.purpir.multiblock.MultiblockSavedData;

public class MultiblockEvents {
    
    public static void register() {
        ServerWorldEvents.LOAD.register((server, world) -> {
            // Устанавливаем текущий мир для менеджера
            MultiblockManager.getInstance().setWorld(world);
            
            // Загружаем сохранённые структуры
            MultiblockSavedData.get(world);
        });
        
        ServerWorldEvents.UNLOAD.register((server, world) -> {
            // Очищаем менеджер при выгрузке мира
            MultiblockManager.getInstance().clear();
        });
    }
}
