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

                // Cobalt smelting
                offerSmelting(java.util.List.of(ModItems.RAW_COBALT), RecipeCategory.MISC,
                    ModItems.COBALT_INGOT, 0.8f, 200, "cobalt");
                offerBlasting(java.util.List.of(ModItems.RAW_COBALT), RecipeCategory.MISC,
                    ModItems.COBALT_INGOT, 0.8f, 100, "cobalt");
                offerSmelting(java.util.List.of(ModBlocks.COBALT_ORE), RecipeCategory.MISC,
                    ModItems.COBALT_INGOT, 0.8f, 200, "cobalt");
                offerBlasting(java.util.List.of(ModBlocks.COBALT_ORE), RecipeCategory.MISC,
                    ModItems.COBALT_INGOT, 0.8f, 100, "cobalt");
                offerSmelting(java.util.List.of(ModBlocks.DEEPSLATE_COBALT_ORE), RecipeCategory.MISC,
                    ModItems.COBALT_INGOT, 0.8f, 200, "cobalt");
                offerBlasting(java.util.List.of(ModBlocks.DEEPSLATE_COBALT_ORE), RecipeCategory.MISC,
                    ModItems.COBALT_INGOT, 0.8f, 100, "cobalt");

                // Zinc smelting
                offerSmelting(java.util.List.of(ModItems.RAW_ZINC), RecipeCategory.MISC,
                    ModItems.ZINC_INGOT, 0.6f, 200, "zinc");
                offerBlasting(java.util.List.of(ModItems.RAW_ZINC), RecipeCategory.MISC,
                    ModItems.ZINC_INGOT, 0.6f, 100, "zinc");
                offerSmelting(java.util.List.of(ModBlocks.ZINC_ORE), RecipeCategory.MISC,
                    ModItems.ZINC_INGOT, 0.6f, 200, "zinc");
                offerBlasting(java.util.List.of(ModBlocks.ZINC_ORE), RecipeCategory.MISC,
                    ModItems.ZINC_INGOT, 0.6f, 100, "zinc");
                offerSmelting(java.util.List.of(ModBlocks.DEEPSLATE_ZINC_ORE), RecipeCategory.MISC,
                    ModItems.ZINC_INGOT, 0.6f, 200, "zinc");
                offerBlasting(java.util.List.of(ModBlocks.DEEPSLATE_ZINC_ORE), RecipeCategory.MISC,
                    ModItems.ZINC_INGOT, 0.6f, 100, "zinc");

                // Vacuumite smelting
                offerSmelting(java.util.List.of(ModItems.RAW_VACUUMITE), RecipeCategory.MISC,
                    ModItems.VACUUMITE_INGOT, 1.0f, 200, "vacuumite");
                offerBlasting(java.util.List.of(ModItems.RAW_VACUUMITE), RecipeCategory.MISC,
                    ModItems.VACUUMITE_INGOT, 1.0f, 100, "vacuumite");
                offerSmelting(java.util.List.of(ModBlocks.VACUUMITE_ORE), RecipeCategory.MISC,
                    ModItems.VACUUMITE_INGOT, 1.0f, 200, "vacuumite");
                offerBlasting(java.util.List.of(ModBlocks.VACUUMITE_ORE), RecipeCategory.MISC,
                    ModItems.VACUUMITE_INGOT, 1.0f, 100, "vacuumite");

                // Bronze block
                offerReversibleCompactingRecipes(RecipeCategory.BUILDING_BLOCKS, 
                    ModItems.BRONZE_INGOT, RecipeCategory.DECORATIONS, ModBlocks.BRONZE_BLOCK);
                
                // Titanium block
                offerReversibleCompactingRecipes(RecipeCategory.BUILDING_BLOCKS, 
                    ModItems.TITANIUM_INGOT, RecipeCategory.DECORATIONS, ModBlocks.TITANIUM_BLOCK);

                // Cobalt block
                offerReversibleCompactingRecipes(RecipeCategory.BUILDING_BLOCKS,
                    ModItems.COBALT_INGOT, RecipeCategory.DECORATIONS, ModBlocks.COBALT_BLOCK);

                // Zinc block
                offerReversibleCompactingRecipes(RecipeCategory.BUILDING_BLOCKS,
                    ModItems.ZINC_INGOT, RecipeCategory.DECORATIONS, ModBlocks.ZINC_BLOCK);

                createShaped(RecipeCategory.COMBAT, ModItems.ZINC_KNIFE)
                    .pattern("Z")
                    .pattern("S")
                    .input('Z', ModItems.ZINC_INGOT)
                    .input('S', Items.STICK)
                    .criterion(hasItem(ModItems.ZINC_INGOT), conditionsFromItem(ModItems.ZINC_INGOT))
                    .offerTo(exporter);
                
                // Netherite Titanium block
                offerReversibleCompactingRecipes(RecipeCategory.BUILDING_BLOCKS, 
                    ModItems.NETHERITE_TITANIUM_INGOT, RecipeCategory.DECORATIONS, ModBlocks.NETHERITE_TITANIUM_BLOCK);

                // Vacuumite block
                offerReversibleCompactingRecipes(RecipeCategory.BUILDING_BLOCKS,
                    ModItems.VACUUMITE_INGOT, RecipeCategory.DECORATIONS, ModBlocks.VACUUMITE_BLOCK);

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

                createShaped(RecipeCategory.TOOLS, ModItems.CRACK_HAMMER)
                    .pattern("BGB")
                    .pattern(" S ")
                    .pattern(" S ")
                    .input('B', ModBlocks.BRONZE_BLOCK)
                    .input('G', ModBlocks.DEEP_GRANITE)
                    .input('S', Items.STICK)
                    .criterion(hasItem(ModBlocks.BRONZE_BLOCK), conditionsFromItem(ModBlocks.BRONZE_BLOCK))
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

                // Solar Infusion Guide
                createShaped(RecipeCategory.MISC, ModItems.SOLAR_INFUSION_GUIDE)
                    .pattern("PSP")
                    .pattern("ABA")
                    .pattern("PPP")
                    .input('P', Items.PAPER)
                    .input('S', ModItems.SOLAR_CRYSTAL)
                    .input('A', Items.AMETHYST_SHARD)
                    .input('B', Items.BOOK)
                    .criterion(hasItem(ModItems.SOLAR_CRYSTAL), conditionsFromItem(ModItems.SOLAR_CRYSTAL))
                    .offerTo(exporter);

                // Vacuumite Magnet
                createShaped(RecipeCategory.TOOLS, ModItems.VACUUMITE_MAGNET)
                    .pattern(" E ")
                    .pattern("IVI")
                    .pattern(" R ")
                    .input('E', Items.ENDER_PEARL)
                    .input('I', Items.IRON_INGOT)
                    .input('V', ModItems.VACUUMITE_INGOT)
                    .input('R', Items.REDSTONE)
                    .criterion(hasItem(ModItems.VACUUMITE_INGOT), conditionsFromItem(ModItems.VACUUMITE_INGOT))
                    .offerTo(exporter);

                createShaped(RecipeCategory.COMBAT, ModItems.VACUUMITE_SWORD)
                    .pattern("V")
                    .pattern("V")
                    .pattern("S")
                    .input('V', ModItems.VACUUMITE_INGOT)
                    .input('S', Items.STICK)
                    .criterion(hasItem(ModItems.VACUUMITE_INGOT), conditionsFromItem(ModItems.VACUUMITE_INGOT))
                    .offerTo(exporter);

                createShaped(RecipeCategory.BUILDING_BLOCKS, ModBlocks.IRON_CASING)
                    .pattern("BIB")
                    .pattern("I I")
                    .pattern("BIB")
                    .input('B', Items.IRON_BLOCK)
                    .input('I', Items.IRON_INGOT)
                    .criterion(hasItem(Items.IRON_BLOCK), conditionsFromItem(Items.IRON_BLOCK))
                    .criterion(hasItem(Items.IRON_INGOT), conditionsFromItem(Items.IRON_INGOT))
                    .offerTo(exporter);

                createShaped(RecipeCategory.BUILDING_BLOCKS, ModBlocks.COBALT_CASING)
                    .pattern("BIB")
                    .pattern("I I")
                    .pattern("BIB")
                    .input('B', ModBlocks.COBALT_BLOCK)
                    .input('I', ModItems.COBALT_INGOT)
                    .criterion(hasItem(ModBlocks.COBALT_BLOCK), conditionsFromItem(ModBlocks.COBALT_BLOCK))
                    .criterion(hasItem(ModItems.COBALT_INGOT), conditionsFromItem(ModItems.COBALT_INGOT))
                    .offerTo(exporter);

                createShaped(RecipeCategory.REDSTONE, ModBlocks.CRUSHER)
                    .pattern("CPC")
                    .pattern("GBG")
                    .pattern("CPC")
                    .input('C', ModBlocks.IRON_CASING)
                    .input('P', Items.FURNACE)
                    .input('G', ModBlocks.DEEP_GRANITE)
                    .input('B', Items.REDSTONE_BLOCK)
                    .criterion(hasItem(ModBlocks.IRON_CASING), conditionsFromItem(ModBlocks.IRON_CASING))
                    .criterion(hasItem(ModBlocks.DEEP_GRANITE), conditionsFromItem(ModBlocks.DEEP_GRANITE))
                    .criterion(hasItem(Items.REDSTONE_BLOCK), conditionsFromItem(Items.REDSTONE_BLOCK))
                    .offerTo(exporter);

                createShapeless(RecipeCategory.MISC, Items.LIGHT_BLUE_DYE)
                    .input(ModBlocks.SOLAR_IRIS)
                    .criterion(hasItem(ModBlocks.SOLAR_IRIS), conditionsFromItem(ModBlocks.SOLAR_IRIS))
                    .offerTo(exporter, "light_blue_dye_from_solar_iris");

                createShapeless(RecipeCategory.MISC, ModItems.LIMESTONE_DUST, 4)
                    .input(ModBlocks.ASHEN_LIMESTONE)
                    .criterion(hasItem(ModBlocks.ASHEN_LIMESTONE), conditionsFromItem(ModBlocks.ASHEN_LIMESTONE))
                    .offerTo(exporter, "limestone_dust_from_ashen_limestone");

                createShapeless(RecipeCategory.MISC, ModItems.ENDER_PEARL_SHARD)
                    .input(ModBlocks.VOID_EYE_PLANT)
                    .criterion(hasItem(ModBlocks.VOID_EYE_PLANT), conditionsFromItem(ModBlocks.VOID_EYE_PLANT))
                    .offerTo(exporter, "ender_pearl_shard_from_void_eye_plant");

                createShaped(RecipeCategory.MISC, Items.ENDER_PEARL)
                    .pattern("SS")
                    .pattern("SS")
                    .input('S', ModItems.ENDER_PEARL_SHARD)
                    .criterion(hasItem(ModItems.ENDER_PEARL_SHARD), conditionsFromItem(ModItems.ENDER_PEARL_SHARD))
                    .offerTo(exporter, "ender_pearl_from_shards");

                createShaped(RecipeCategory.DECORATIONS, ModBlocks.VOID_LANTERN)
                    .pattern(" S ")
                    .pattern("VLV")
                    .pattern(" S ")
                    .input('S', ModItems.ENDER_PEARL_SHARD)
                    .input('V', ModBlocks.VOID_EYE_PLANT)
                    .input('L', Items.LANTERN)
                    .criterion(hasItem(ModBlocks.VOID_EYE_PLANT), conditionsFromItem(ModBlocks.VOID_EYE_PLANT))
                    .criterion(hasItem(ModItems.ENDER_PEARL_SHARD), conditionsFromItem(ModItems.ENDER_PEARL_SHARD))
                    .offerTo(exporter);
            }
        };
    }

    @Override
    public String getName() {
        return "Caveborn Recipes";
    }
}
