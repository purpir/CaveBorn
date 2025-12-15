package ru.purpir.client.datagen;

import net.fabricmc.fabric.api.client.datagen.v1.provider.FabricModelProvider;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.minecraft.client.data.BlockStateModelGenerator;
import net.minecraft.client.data.ItemModelGenerator;
import net.minecraft.client.data.Models;
import ru.purpir.block.ModBlocks;
import ru.purpir.item.ModItems;

public class ModModelProvider extends FabricModelProvider {
    public ModModelProvider(FabricDataOutput output) {
        super(output);
    }

    @Override
    public void generateBlockStateModels(BlockStateModelGenerator blockStateModelGenerator) {
        blockStateModelGenerator.registerSimpleCubeAll(ModBlocks.BRONZE_ORE);
        blockStateModelGenerator.registerSimpleCubeAll(ModBlocks.BRONZE_BLOCK);
        blockStateModelGenerator.registerSimpleCubeAll(ModBlocks.TITANIUM_ORE);
        blockStateModelGenerator.registerSimpleCubeAll(ModBlocks.DEEPSLATE_TITANIUM_ORE);
        blockStateModelGenerator.registerSimpleCubeAll(ModBlocks.TITANIUM_BLOCK);
        blockStateModelGenerator.registerSimpleCubeAll(ModBlocks.NETHERITE_TITANIUM_BLOCK);
    }

    @Override
    public void generateItemModels(ItemModelGenerator itemModelGenerator) {
        itemModelGenerator.register(ModItems.BRONZE_INGOT, Models.GENERATED);
        itemModelGenerator.register(ModItems.RAW_BRONZE, Models.GENERATED);
        itemModelGenerator.register(ModItems.BRONZE_SWORD, Models.HANDHELD);
        itemModelGenerator.register(ModItems.BRONZE_PICKAXE, Models.HANDHELD);
        itemModelGenerator.register(ModItems.BRONZE_AXE, Models.HANDHELD);
        itemModelGenerator.register(ModItems.BRONZE_SHOVEL, Models.HANDHELD);
        itemModelGenerator.register(ModItems.BRONZE_HOE, Models.HANDHELD);
        itemModelGenerator.register(ModItems.BRONZE_HELMET, Models.GENERATED);
        itemModelGenerator.register(ModItems.BRONZE_CHESTPLATE, Models.GENERATED);
        itemModelGenerator.register(ModItems.BRONZE_LEGGINGS, Models.GENERATED);
        itemModelGenerator.register(ModItems.BRONZE_BOOTS, Models.GENERATED);

        itemModelGenerator.register(ModItems.TITANIUM_INGOT, Models.GENERATED);
        itemModelGenerator.register(ModItems.RAW_TITANIUM, Models.GENERATED);

        itemModelGenerator.register(ModItems.NETHERITE_TITANIUM_INGOT, Models.GENERATED);
        itemModelGenerator.register(ModItems.NETHERITE_TITANIUM_SWORD, Models.HANDHELD);
        itemModelGenerator.register(ModItems.NETHERITE_TITANIUM_PICKAXE, Models.HANDHELD);
        itemModelGenerator.register(ModItems.NETHERITE_TITANIUM_AXE, Models.HANDHELD);
        itemModelGenerator.register(ModItems.NETHERITE_TITANIUM_SHOVEL, Models.HANDHELD);
        itemModelGenerator.register(ModItems.NETHERITE_TITANIUM_HOE, Models.HANDHELD);
        itemModelGenerator.register(ModItems.NETHERITE_TITANIUM_HELMET, Models.GENERATED);
        itemModelGenerator.register(ModItems.NETHERITE_TITANIUM_CHESTPLATE, Models.GENERATED);
        itemModelGenerator.register(ModItems.NETHERITE_TITANIUM_LEGGINGS, Models.GENERATED);
        itemModelGenerator.register(ModItems.NETHERITE_TITANIUM_BOOTS, Models.GENERATED);
    }
}
