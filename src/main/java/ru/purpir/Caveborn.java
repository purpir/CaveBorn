package ru.purpir;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.purpir.block.ModBlocks;
import ru.purpir.block.entity.ModBlockEntities;
import ru.purpir.command.CavebornCommand;
import ru.purpir.element.Element;
import ru.purpir.element.ElementCommand;
import ru.purpir.element.ElementTickHandler;
import ru.purpir.element.elements.FireElement;
import ru.purpir.element.elements.WaterElement;
import ru.purpir.element.elements.IceElement;
import ru.purpir.element.elements.GrassElement;
import ru.purpir.element.elements.MoonElement;
import ru.purpir.element.reactions.ReactionManager;
import ru.purpir.element.reactions.RootBindingReaction;
import ru.purpir.entity.ModEntities;
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
        
        // Регистрируем элементы
        Element.register(FireElement.INSTANCE);
        Element.register(WaterElement.INSTANCE);
        Element.register(IceElement.INSTANCE);
        Element.register(GrassElement.INSTANCE);
        Element.register(MoonElement.INSTANCE);

        // Регистрируем реакции
        ReactionManager.register(RootBindingReaction.INSTANCE);

        // Регистрируем команды
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            CavebornCommand.register(dispatcher);
            ElementCommand.register(dispatcher);
        });
        
        ModArmorMaterials.initialize();
        ru.purpir.component.ModComponents.register();
        ModBlocks.registerModBlocks();
        ru.purpir.effect.ModStatusEffects.register();
        ModBlockEntities.register();
        ModEntities.register();
        ModItems.registerModItems();
        ModScreenHandlers.register();
        ru.purpir.network.ModPackets.registerServer();
        ModFeatures.register();
        ModOreGeneration.generateOres();
        ru.purpir.enchantment.ModEnchantments.register();
        ru.purpir.event.MultiblockEvents.register();
        ru.purpir.event.SolarCrystalTransformationHandler.register();
        ru.purpir.event.SolarDamageHandler.register();
        ru.purpir.event.SolarStrikeHandler.register();
        ru.purpir.event.SolarShieldHandler.register();
        ru.purpir.event.SolarCrystalDustHandler.register();
        ru.purpir.event.SolarTotemHandler.register();
        ru.purpir.event.SolarRustedMinerKeyHandler.register();
        ru.purpir.event.SolarBronzeAxeHandler.register();
        ru.purpir.event.ZincKnifeHandler.register();
        ru.purpir.event.SolarZincKnifeHandler.register();
        ru.purpir.event.SolarInfusionGuideGiftHandler.register();
        ru.purpir.event.PetMobTickHandler.register();
        ru.purpir.event.VeinMinerHandler.register();
        ru.purpir.event.VacuumiteMagnetHandler.register();
        ru.purpir.event.VacuumiteSwordHandler.register();
        ru.purpir.eventaltar.EventAltarHandler.register();
        ru.purpir.item.CrackHammerItem.registerTicker();
        ElementTickHandler.register();
        ReactionManager.registerEvents();
        
        ru.purpir.solar.SolarPointBank.register();
        ru.purpir.util.WallManager.register();
        
        LOGGER.info("Caveborn mod initialized!");
    }
}
