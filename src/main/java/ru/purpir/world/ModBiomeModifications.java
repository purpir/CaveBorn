package ru.purpir.world;

import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.world.gen.GenerationStep;
import ru.purpir.Caveborn;

public class ModBiomeModifications {
    
    public static void register() {
        // Добавляем аметистовые столбы в наш биом
        BiomeModifications.addFeature(
            BiomeSelectors.includeByKey(ModBiomes.AMETHYST_PLAINS),
            GenerationStep.Feature.SURFACE_STRUCTURES,
            RegistryKey.of(RegistryKeys.PLACED_FEATURE, 
                Identifier.of(Caveborn.MOD_ID, "amethyst_spike"))
        );
    }
}
