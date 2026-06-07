package ru.purpir.world;

import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.minecraft.world.biome.BiomeKeys;
import net.minecraft.world.gen.GenerationStep;

public class ModOreGeneration {
    public static void generateOres() {
        // Bronze ore in End - common
        BiomeModifications.addFeature(
            BiomeSelectors.foundInTheEnd(),
            GenerationStep.Feature.UNDERGROUND_ORES,
            ModPlacedFeatures.BRONZE_ORE_PLACED_KEY
        );

        // Titanium ore in Overworld - very rare
        BiomeModifications.addFeature(
            BiomeSelectors.foundInOverworld(),
            GenerationStep.Feature.UNDERGROUND_ORES,
            ModPlacedFeatures.TITANIUM_ORE_PLACED_KEY
        );

        // Deepslate Titanium ore in Overworld - very rare
        BiomeModifications.addFeature(
            BiomeSelectors.foundInOverworld(),
            GenerationStep.Feature.UNDERGROUND_ORES,
            ModPlacedFeatures.DEEPSLATE_TITANIUM_ORE_PLACED_KEY
        );

        // Deepslate Cobalt ore in Overworld - deep technical material
        BiomeModifications.addFeature(
            BiomeSelectors.foundInOverworld(),
            GenerationStep.Feature.UNDERGROUND_ORES,
            ModPlacedFeatures.DEEPSLATE_COBALT_ORE_PLACED_KEY
        );

        // Vacuumite ore in End - rare
        BiomeModifications.addFeature(
            BiomeSelectors.foundInTheEnd(),
            GenerationStep.Feature.UNDERGROUND_ORES,
            ModPlacedFeatures.VACUUMITE_ORE_PLACED_KEY
        );

        BiomeModifications.addFeature(
            BiomeSelectors.includeByKey(BiomeKeys.END_HIGHLANDS, BiomeKeys.END_MIDLANDS, BiomeKeys.SMALL_END_ISLANDS),
            GenerationStep.Feature.VEGETAL_DECORATION,
            ModPlacedFeatures.VOID_EYE_PLANT_PATCH_PLACED_KEY
        );

        // Natural stone variants in Overworld
        BiomeModifications.addFeature(
            BiomeSelectors.foundInOverworld(),
            GenerationStep.Feature.UNDERGROUND_ORES,
            ModPlacedFeatures.DEEP_GRANITE_PLACED_KEY
        );

        BiomeModifications.addFeature(
            BiomeSelectors.foundInOverworld(),
            GenerationStep.Feature.UNDERGROUND_ORES,
            ModPlacedFeatures.ASHEN_LIMESTONE_PLACED_KEY
        );

        BiomeModifications.addFeature(
            BiomeSelectors.foundInOverworld(),
            GenerationStep.Feature.VEGETAL_DECORATION,
            ModPlacedFeatures.CRYSTAL_GROWTH_PATCH_PLACED_KEY
        );

        BiomeModifications.addFeature(
            BiomeSelectors.foundInOverworld(),
            GenerationStep.Feature.UNDERGROUND_DECORATION,
            ModPlacedFeatures.MINERS_CAMP_PLACED_KEY
        );

        BiomeModifications.addFeature(
            BiomeSelectors.foundInOverworld(),
            GenerationStep.Feature.UNDERGROUND_DECORATION,
            ModPlacedFeatures.HANGING_MINERS_CACHE_PLACED_KEY
        );

        // Weed patches - only in plains biome
        BiomeModifications.addFeature(
            BiomeSelectors.includeByKey(BiomeKeys.PLAINS, BiomeKeys.SUNFLOWER_PLAINS),
            GenerationStep.Feature.VEGETAL_DECORATION,
            ModPlacedFeatures.WEED_PATCH_PLACED_KEY
        );

        // Hogweed (Борщевик) - in taiga and jungle biomes
        BiomeModifications.addFeature(
            BiomeSelectors.includeByKey(
                BiomeKeys.TAIGA, BiomeKeys.OLD_GROWTH_PINE_TAIGA, BiomeKeys.OLD_GROWTH_SPRUCE_TAIGA,
                BiomeKeys.SNOWY_TAIGA,
                BiomeKeys.JUNGLE, BiomeKeys.SPARSE_JUNGLE, BiomeKeys.BAMBOO_JUNGLE
            ),
            GenerationStep.Feature.VEGETAL_DECORATION,
            ModPlacedFeatures.HOGWEED_PATCH_PLACED_KEY
        );
    }
}
