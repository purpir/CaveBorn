package ru.purpir.event;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LightningEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.LightType;
import ru.purpir.component.ModComponents;
import ru.purpir.item.ModItems;

public class SolarCrystalTransformationHandler {
    private static final long TRANSFORMATION_TIME = 1200; // 60 секунд

    public static void register() {
        ServerTickEvents.END_WORLD_TICK.register(world -> {
            if (!(world instanceof ServerWorld serverWorld)) return;

            var amethysts = serverWorld.getEntitiesByType(EntityType.ITEM, entity -> 
                entity.getStack().isOf(Items.AMETHYST_SHARD));
            
            for (ItemEntity itemEntity : amethysts) {
                ItemStack stack = itemEntity.getStack();
                BlockPos pos = itemEntity.getBlockPos();
                
                boolean isDaytime = serverWorld.isDay();
                int skyLight = serverWorld.getLightLevel(LightType.SKY, pos);
                boolean hasMaxLight = skyLight >= 15;
                
                if (isDaytime && hasMaxLight && !serverWorld.isRaining()) {
                    long ticksInSun = stack.getOrDefault(ModComponents.SUN_EXPOSURE_TIME, 0L) + 1;
                    stack.set(ModComponents.SUN_EXPOSURE_TIME, ticksInSun);
                    
                    if (ticksInSun >= TRANSFORMATION_TIME) {
                        transformToSolarCrystal(serverWorld, itemEntity);
                    }
                } else {
                    if (stack.contains(ModComponents.SUN_EXPOSURE_TIME)) {
                        stack.remove(ModComponents.SUN_EXPOSURE_TIME);
                    }
                }
            }
        });
    }

    private static void transformToSolarCrystal(ServerWorld world, ItemEntity amethystEntity) {
        BlockPos pos = amethystEntity.getBlockPos();
        ItemStack amethystStack = amethystEntity.getStack();
        int count = amethystStack.getCount();
        
        LightningEntity lightning = new LightningEntity(EntityType.LIGHTNING_BOLT, world);
        lightning.refreshPositionAfterTeleport(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
        lightning.setCosmetic(true);
        world.spawnEntity(lightning);
        
        amethystEntity.discard();
        
        ItemStack solarCrystal = new ItemStack(ModItems.SOLAR_CRYSTAL, count);
        ItemEntity solarEntity = new ItemEntity(world, pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, solarCrystal);
        solarEntity.setVelocity(0, 0.2, 0);
        world.spawnEntity(solarEntity);
    }
}
