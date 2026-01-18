package ru.purpir.client.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.minecraft.data.recipe.RecipeExporter;
import net.minecraft.data.recipe.RecipeGenerator;
import net.minecraft.item.Items;
import net.minecraft.recipe.book.RecipeCategory;
import net.minecraft.registry.RegistryWrapper;
import ru.purpir.block.ModBlocks;
import ru.purpir.item.ModItems;

import java.util.concurrent.CompletableFuture;

public class ModRecipeProvider extends FabricRecipeProvider {
    public ModRecipeProvider(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
        super(output, registriesFuture);
    }

    @Override
    protected RecipeGenerator getRecipeGenerator(RegistryWrapper.WrapperLookup registries, RecipeExporter exporter) {
        return new RecipeGenerator(registries, exporter) {
            @Override
            public void generate() {
                // Bronze smelting
                offerSmelting(java.util.List.of(ModItems.RAW_BRONZE), RecipeCategory.MISC, 
                    ModItems.BRONZE_INGOT, 0.7f, 200, "bronze");
                offerBlasting(java.util.List.of(ModItems.RAW_BRONZE), RecipeCategory.MISC, 
                    ModItems.BRONZE_INGOT, 0.7f, 100, "bronze");
                offerSmelting(java.util.List.of(ModBlocks.BRONZE_ORE), RecipeCategory.MISC, 
                    ModItems.BRONZE_INGOT, 0.7f, 200, "bronze");
                offerBlasting(java.util.List.of(ModBlocks.BRONZE_ORE), RecipeCategory.MISC, 
                    ModItems.BRONZE_INGOT, 0.7f, 100, "bronze");

                // Titanium smelting
                offerSmelting(java.util.List.of(ModItems.RAW_TITANIUM), RecipeCategory.MISC, 
                    ModItems.TITANIUM_INGOT, 1.0f, 200, "titanium");
                offerBlasting(java.util.List.of(ModItems.RAW_TITANIUM), RecipeCategory.MISC, 
                    ModItems.TITANIUM_INGOT, 1.0f, 100, "titanium");

                // Bronze block
                offerReversibleCompactingRecipes(RecipeCategory.BUILDING_BLOCKS, 
                    ModItems.BRONZE_INGOT, RecipeCategory.DECORATIONS, ModBlocks.BRONZE_BLOCK);
                
                // Titanium block
                offerReversibleCompactingRecipes(RecipeCategory.BUILDING_BLOCKS, 
                    ModItems.TITANIUM_INGOT, RecipeCategory.DECORATIONS, ModBlocks.TITANIUM_BLOCK);
                
                // Netherite Titanium block
                offerReversibleCompactingRecipes(RecipeCategory.BUILDING_BLOCKS, 
                    ModItems.NETHERITE_TITANIUM_INGOT, RecipeCategory.DECORATIONS, ModBlocks.NETHERITE_TITANIUM_BLOCK);

                // Netherite Titanium Ingot - 4 netherite ingots + 4 titanium ingots
                createShaped(RecipeCategory.MISC, ModItems.NETHERITE_TITANIUM_INGOT, 1)
                    .pattern("NTN")
                    .pattern("TNT")
                    .pattern("NTN")
                    .input('N', Items.NETHERITE_INGOT)
                    .input('T', ModItems.TITANIUM_INGOT)
                    .criterion(hasItem(Items.NETHERITE_INGOT), conditionsFromItem(Items.NETHERITE_INGOT))
                    .criterion(hasItem(ModItems.TITANIUM_INGOT), conditionsFromItem(ModItems.TITANIUM_INGOT))
                    .offerTo(exporter, "netherite_titanium_ingot_from_crafting");

                // Bronze Tools
                createShaped(RecipeCategory.TOOLS, ModItems.BRONZE_SWORD)
                    .pattern("B").pattern("B").pattern("S")
                    .input('B', ModItems.BRONZE_INGOT).input('S', Items.STICK)
                    .criterion(hasItem(ModItems.BRONZE_INGOT), conditionsFromItem(ModItems.BRONZE_INGOT))
                    .offerTo(exporter);

                createShaped(RecipeCategory.TOOLS, ModItems.BRONZE_PICKAXE)
                    .pattern("BBB").pattern(" S ").pattern(" S ")
                    .input('B', ModItems.BRONZE_INGOT).input('S', Items.STICK)
                    .criterion(hasItem(ModItems.BRONZE_INGOT), conditionsFromItem(ModItems.BRONZE_INGOT))
                    .offerTo(exporter);

                createShaped(RecipeCategory.TOOLS, ModItems.BRONZE_AXE)
                    .pattern("BB").pattern("BS").pattern(" S")
                    .input('B', ModItems.BRONZE_INGOT).input('S', Items.STICK)
                    .criterion(hasItem(ModItems.BRONZE_INGOT), conditionsFromItem(ModItems.BRONZE_INGOT))
                    .offerTo(exporter);

                createShaped(RecipeCategory.TOOLS, ModItems.BRONZE_SHOVEL)
                    .pattern("B").pattern("S").pattern("S")
                    .input('B', ModItems.BRONZE_INGOT).input('S', Items.STICK)
                    .criterion(hasItem(ModItems.BRONZE_INGOT), conditionsFromItem(ModItems.BRONZE_INGOT))
                    .offerTo(exporter);

                createShaped(RecipeCategory.TOOLS, ModItems.BRONZE_HOE)
                    .pattern("BB").pattern(" S").pattern(" S")
                    .input('B', ModItems.BRONZE_INGOT).input('S', Items.STICK)
                    .criterion(hasItem(ModItems.BRONZE_INGOT), conditionsFromItem(ModItems.BRONZE_INGOT))
                    .offerTo(exporter);

                // Bronze Armor
                createShaped(RecipeCategory.COMBAT, ModItems.BRONZE_HELMET)
                    .pattern("BBB").pattern("B B")
                    .input('B', ModItems.BRONZE_INGOT)
                    .criterion(hasItem(ModItems.BRONZE_INGOT), conditionsFromItem(ModItems.BRONZE_INGOT))
                    .offerTo(exporter);

                createShaped(RecipeCategory.COMBAT, ModItems.BRONZE_CHESTPLATE)
                    .pattern("B B").pattern("BBB").pattern("BBB")
                    .input('B', ModItems.BRONZE_INGOT)
                    .criterion(hasItem(ModItems.BRONZE_INGOT), conditionsFromItem(ModItems.BRONZE_INGOT))
                    .offerTo(exporter);

                createShaped(RecipeCategory.COMBAT, ModItems.BRONZE_LEGGINGS)
                    .pattern("BBB").pattern("B B").pattern("B B")
                    .input('B', ModItems.BRONZE_INGOT)
                    .criterion(hasItem(ModItems.BRONZE_INGOT), conditionsFromItem(ModItems.BRONZE_INGOT))
                    .offerTo(exporter);

                createShaped(RecipeCategory.COMBAT, ModItems.BRONZE_BOOTS)
                    .pattern("B B").pattern("B B")
                    .input('B', ModItems.BRONZE_INGOT)
                    .criterion(hasItem(ModItems.BRONZE_INGOT), conditionsFromItem(ModItems.BRONZE_INGOT))
                    .offerTo(exporter);

                // Netherite Titanium Tools
                createShaped(RecipeCategory.TOOLS, ModItems.NETHERITE_TITANIUM_SWORD)
                    .pattern("N").pattern("N").pattern("S")
                    .input('N', ModItems.NETHERITE_TITANIUM_INGOT).input('S', Items.STICK)
                    .criterion(hasItem(ModItems.NETHERITE_TITANIUM_INGOT), conditionsFromItem(ModItems.NETHERITE_TITANIUM_INGOT))
                    .offerTo(exporter);

                createShaped(RecipeCategory.TOOLS, ModItems.NETHERITE_TITANIUM_PICKAXE)
                    .pattern("NNN").pattern(" S ").pattern(" S ")
                    .input('N', ModItems.NETHERITE_TITANIUM_INGOT).input('S', Items.STICK)
                    .criterion(hasItem(ModItems.NETHERITE_TITANIUM_INGOT), conditionsFromItem(ModItems.NETHERITE_TITANIUM_INGOT))
                    .offerTo(exporter);

                createShaped(RecipeCategory.TOOLS, ModItems.NETHERITE_TITANIUM_AXE)
                    .pattern("NN").pattern("NS").pattern(" S")
                    .input('N', ModItems.NETHERITE_TITANIUM_INGOT).input('S', Items.STICK)
                    .criterion(hasItem(ModItems.NETHERITE_TITANIUM_INGOT), conditionsFromItem(ModItems.NETHERITE_TITANIUM_INGOT))
                    .offerTo(exporter);

                createShaped(RecipeCategory.TOOLS, ModItems.NETHERITE_TITANIUM_SHOVEL)
                    .pattern("N").pattern("S").pattern("S")
                    .input('N', ModItems.NETHERITE_TITANIUM_INGOT).input('S', Items.STICK)
                    .criterion(hasItem(ModItems.NETHERITE_TITANIUM_INGOT), conditionsFromItem(ModItems.NETHERITE_TITANIUM_INGOT))
                    .offerTo(exporter);

                createShaped(RecipeCategory.TOOLS, ModItems.NETHERITE_TITANIUM_HOE)
                    .pattern("NN").pattern(" S").pattern(" S")
                    .input('N', ModItems.NETHERITE_TITANIUM_INGOT).input('S', Items.STICK)
                    .criterion(hasItem(ModItems.NETHERITE_TITANIUM_INGOT), conditionsFromItem(ModItems.NETHERITE_TITANIUM_INGOT))
                    .offerTo(exporter);

                // Netherite Titanium Armor
                createShaped(RecipeCategory.COMBAT, ModItems.NETHERITE_TITANIUM_HELMET)
                    .pattern("NNN").pattern("N N")
                    .input('N', ModItems.NETHERITE_TITANIUM_INGOT)
                    .criterion(hasItem(ModItems.NETHERITE_TITANIUM_INGOT), conditionsFromItem(ModItems.NETHERITE_TITANIUM_INGOT))
                    .offerTo(exporter);

                createShaped(RecipeCategory.COMBAT, ModItems.NETHERITE_TITANIUM_CHESTPLATE)
                    .pattern("N N").pattern("NNN").pattern("NNN")
                    .input('N', ModItems.NETHERITE_TITANIUM_INGOT)
                    .criterion(hasItem(ModItems.NETHERITE_TITANIUM_INGOT), conditionsFromItem(ModItems.NETHERITE_TITANIUM_INGOT))
                    .offerTo(exporter);

                createShaped(RecipeCategory.COMBAT, ModItems.NETHERITE_TITANIUM_LEGGINGS)
                    .pattern("NNN").pattern("N N").pattern("N N")
                    .input('N', ModItems.NETHERITE_TITANIUM_INGOT)
                    .criterion(hasItem(ModItems.NETHERITE_TITANIUM_INGOT), conditionsFromItem(ModItems.NETHERITE_TITANIUM_INGOT))
                    .offerTo(exporter);

                createShaped(RecipeCategory.COMBAT, ModItems.NETHERITE_TITANIUM_BOOTS)
                    .pattern("N N").pattern("N N")
                    .input('N', ModItems.NETHERITE_TITANIUM_INGOT)
                    .criterion(hasItem(ModItems.NETHERITE_TITANIUM_INGOT), conditionsFromItem(ModItems.NETHERITE_TITANIUM_INGOT))
                    .offerTo(exporter);

                // Titanium building blocks
                createShaped(RecipeCategory.BUILDING_BLOCKS, ModBlocks.TITANIUM_GRATE, 4)
                    .pattern(" T ")
                    .pattern("T T")
                    .pattern(" T ")
                    .input('T', ModItems.TITANIUM_INGOT)
                    .criterion(hasItem(ModItems.TITANIUM_INGOT), conditionsFromItem(ModItems.TITANIUM_INGOT))
                    .offerTo(exporter);

                createShaped(RecipeCategory.BUILDING_BLOCKS, ModBlocks.TITANIUM_STAIRS, 4)
                    .pattern("T  ")
                    .pattern("TT ")
                    .pattern("TTT")
                    .input('T', ModBlocks.TITANIUM_BLOCK)
                    .criterion(hasItem(ModBlocks.TITANIUM_BLOCK), conditionsFromItem(ModBlocks.TITANIUM_BLOCK))
                    .offerTo(exporter);

                createShaped(RecipeCategory.BUILDING_BLOCKS, ModBlocks.TITANIUM_SLAB, 6)
                    .pattern("TTT")
                    .input('T', ModBlocks.TITANIUM_BLOCK)
                    .criterion(hasItem(ModBlocks.TITANIUM_BLOCK), conditionsFromItem(ModBlocks.TITANIUM_BLOCK))
                    .offerTo(exporter);

                createShaped(RecipeCategory.DECORATIONS, ModBlocks.TITANIUM_BARS, 16)
                    .pattern("TTT")
                    .pattern("TTT")
                    .input('T', ModItems.TITANIUM_INGOT)
                    .criterion(hasItem(ModItems.TITANIUM_INGOT), conditionsFromItem(ModItems.TITANIUM_INGOT))
                    .offerTo(exporter);

                createShaped(RecipeCategory.REDSTONE, ModBlocks.TITANIUM_DOOR, 3)
                    .pattern("TT")
                    .pattern("TT")
                    .pattern("TT")
                    .input('T', ModItems.TITANIUM_INGOT)
                    .criterion(hasItem(ModItems.TITANIUM_INGOT), conditionsFromItem(ModItems.TITANIUM_INGOT))
                    .offerTo(exporter);

                createShaped(RecipeCategory.REDSTONE, ModBlocks.TITANIUM_TRAPDOOR)
                    .pattern("TT")
                    .pattern("TT")
                    .input('T', ModItems.TITANIUM_INGOT)
                    .criterion(hasItem(ModItems.TITANIUM_INGOT), conditionsFromItem(ModItems.TITANIUM_INGOT))
                    .offerTo(exporter);

                // Bag
                createShaped(RecipeCategory.TOOLS, ModItems.BAG)
                    .pattern("KCK")
                    .pattern("VIV")
                    .pattern("KKK")
                    .input('K', Items.LEATHER)
                    .input('C', Items.CHEST)
                    .input('V', ModItems.FIBER)
                    .input('I', Items.IRON_INGOT)
                    .criterion(hasItem(ModItems.FIBER), conditionsFromItem(ModItems.FIBER))
                    .offerTo(exporter);
            }
        };
    }

    @Override
    public String getName() {
        return "Caveborn Recipes";
    }
}
