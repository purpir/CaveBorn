package ru.purpir.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.fabricmc.fabric.api.client.rendering.v1.BlockRenderLayerMap;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.client.render.BlockRenderLayer;
import net.minecraft.client.render.item.tint.TintSourceTypes;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import ru.purpir.Caveborn;
import ru.purpir.block.ModBlocks;
import ru.purpir.client.render.BlockTintSource;
import ru.purpir.client.screen.BagScreen;
import ru.purpir.client.screen.CrusherScreen;
import ru.purpir.client.screen.SolarInfusionGuideScreen;
import ru.purpir.item.ModItems;
import ru.purpir.screen.ModScreenHandlers;

public class CavebornClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        // Регистрируем свой TintSource для блоков
        TintSourceTypes.ID_MAPPER.put(
            Identifier.of(Caveborn.MOD_ID, "block"),
            BlockTintSource.CODEC
        );
        
        // Инициализация динамических текстур
        TextureInitializer.initialize();
        
        // Регистрируем тултип для солнечной инфузии
        SolarInfusionTooltip.register();
        
        // Регистрируем экран сумки
        HandledScreens.register(ModScreenHandlers.BAG_SCREEN_HANDLER, BagScreen::new);
        HandledScreens.register(ModScreenHandlers.CRUSHER_SCREEN_HANDLER, CrusherScreen::new);

        UseItemCallback.EVENT.register((player, world, hand) -> {
            if (!world.isClient()) {
                return ActionResult.PASS;
            }

            if (player.getStackInHand(hand).isOf(ModItems.SOLAR_INFUSION_GUIDE)) {
                MinecraftClient.getInstance().setScreen(new SolarInfusionGuideScreen());
                return ActionResult.SUCCESS;
            }

            return ActionResult.PASS;
        });
        
        // Регистрируем прозрачный рендер для сорняков
        BlockRenderLayerMap.putBlock(ModBlocks.WEED, BlockRenderLayer.CUTOUT);
        BlockRenderLayerMap.putBlock(ModBlocks.WEED_TOP, BlockRenderLayer.CUTOUT);
        BlockRenderLayerMap.putBlock(ModBlocks.HOGWEED, BlockRenderLayer.CUTOUT);
        BlockRenderLayerMap.putBlock(ModBlocks.SOLAR_IRIS, BlockRenderLayer.CUTOUT);
        
        // Прозрачность для титановых блоков
        BlockRenderLayerMap.putBlock(ModBlocks.TITANIUM_GRATE, BlockRenderLayer.CUTOUT);
        BlockRenderLayerMap.putBlock(ModBlocks.TITANIUM_BARS, BlockRenderLayer.CUTOUT);
        BlockRenderLayerMap.putBlock(ModBlocks.TITANIUM_DOOR, BlockRenderLayer.CUTOUT);
        BlockRenderLayerMap.putBlock(ModBlocks.TITANIUM_TRAPDOOR, BlockRenderLayer.CUTOUT);
    }
}
