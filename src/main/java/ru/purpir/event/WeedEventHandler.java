package ru.purpir.event;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import ru.purpir.block.WeedBlock;

public class WeedEventHandler {
    private static int tickCounter = 0;
    private static final int CHECK_INTERVAL = 20; // Проверяем каждые 20 тиков (1 секунда)
    
    public static void register() {
        ServerTickEvents.END_WORLD_TICK.register(world -> {
            tickCounter++;
            if (tickCounter >= CHECK_INTERVAL) {
                tickCounter = 0;
                checkFarmlandForWeeds(world);
            }
        });
    }
    
    private static void checkFarmlandForWeeds(ServerWorld world) {
        Random random = world.getRandom();
        
        // Проверяем грядки вокруг игроков
        world.getPlayers().forEach(player -> {
            BlockPos playerPos = player.getBlockPos();
            int radius = 16; // Радиус проверки вокруг игрока
            
            // Проверяем больше позиций
            for (int i = 0; i < 20; i++) {
                int x = playerPos.getX() + random.nextInt(radius * 2) - radius;
                int z = playerPos.getZ() + random.nextInt(radius * 2) - radius;
                
                // Ищем грядку по высоте
                for (int y = playerPos.getY() - 5; y <= playerPos.getY() + 5; y++) {
                    BlockPos checkPos = new BlockPos(x, y, z);
                    BlockState state = world.getBlockState(checkPos);
                    
                    if (state.isOf(Blocks.FARMLAND)) {
                        WeedBlock.trySpawnOnFarmland(world, checkPos, random);
                        break;
                    }
                }
            }
        });
    }
}
