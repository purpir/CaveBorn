package ru.purpir;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.purpir.block.ModBlocks;
import ru.purpir.item.ModArmorMaterials;
import ru.purpir.item.ModItems;
import ru.purpir.screen.ModScreenHandlers;
import ru.purpir.world.ModFeatures;
import ru.purpir.world.ModOreGeneration;

public class Caveborn implements ModInitializer {
    public static final String MOD_ID = "caveborn";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        ModArmorMaterials.initialize();
        ModBlocks.registerModBlocks();
        ModItems.registerModItems();
        ModScreenHandlers.register();
        ModFeatures.register();
        ModOreGeneration.generateOres();
        ru.purpir.event.MultiblockEvents.register();
        
        LOGGER.info("Caveborn mod initialized!");
    }
}
