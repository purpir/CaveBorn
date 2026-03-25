package ru.purpir;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.purpir.block.ModBlocks;
import ru.purpir.command.CavebornCommand;
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
        // Загружаем конфигурацию
        ru.purpir.config.SolarAbilityConfig.getInstance();
        
        // Регистрируем команды
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            CavebornCommand.register(dispatcher);
        });
        
        ModArmorMaterials.initialize();
        ru.purpir.component.ModComponents.register();
        ModBlocks.registerModBlocks();
        ModItems.registerModItems();
        ModScreenHandlers.register();
        ModFeatures.register();
        ModOreGeneration.generateOres();
        ru.purpir.enchantment.ModEnchantments.register();
        ru.purpir.event.MultiblockEvents.register();
        ru.purpir.event.SolarCrystalTransformationHandler.register();
        ru.purpir.event.SolarDamageHandler.register();
        ru.purpir.event.SolarStrikeHandler.register();
        ru.purpir.event.PetMobTickHandler.register();
        ru.purpir.event.VeinMinerHandler.register();
        
        LOGGER.info("Caveborn mod initialized!");
    }
}
