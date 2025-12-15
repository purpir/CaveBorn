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

        // Weed patches - only in plains biome
        BiomeModifications.addFeature(
            BiomeSelectors.includeByKey(BiomeKeys.PLAINS, BiomeKeys.SUNFLOWER_PLAINS),
            GenerationStep.Feature.VEGETAL_DECORATION,
            ModPlacedFeatures.WEED_PATCH_PLACED_KEY
        );
    }
}
