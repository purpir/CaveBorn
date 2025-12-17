package ru.purpir.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.BlockRenderLayerMap;
import net.minecraft.client.render.BlockRenderLayer;
import ru.purpir.block.ModBlocks;

public class CavebornClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        // Регистрируем прозрачный рендер для сорняков
        BlockRenderLayerMap.putBlock(ModBlocks.WEED, BlockRenderLayer.CUTOUT);
        BlockRenderLayerMap.putBlock(ModBlocks.WEED_TOP, BlockRenderLayer.CUTOUT);
        BlockRenderLayerMap.putBlock(ModBlocks.HOGWEED, BlockRenderLayer.CUTOUT);
        
        // Прозрачность для титановых блоков
        BlockRenderLayerMap.putBlock(ModBlocks.TITANIUM_GRATE, BlockRenderLayer.CUTOUT);
        BlockRenderLayerMap.putBlock(ModBlocks.TITANIUM_BARS, BlockRenderLayer.CUTOUT);
        BlockRenderLayerMap.putBlock(ModBlocks.TITANIUM_DOOR, BlockRenderLayer.CUTOUT);
        BlockRenderLayerMap.putBlock(ModBlocks.TITANIUM_TRAPDOOR, BlockRenderLayer.CUTOUT);
    }
}
