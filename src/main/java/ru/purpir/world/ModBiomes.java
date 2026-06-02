package ru.purpir.world;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registerable;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.sound.BiomeMoodSound;
import net.minecraft.util.Identifier;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeEffects;
import net.minecraft.world.biome.GenerationSettings;
import net.minecraft.world.biome.SpawnSettings;
import net.minecraft.world.gen.GenerationStep;
import ru.purpir.Caveborn;

public class ModBiomes {
    public static final RegistryKey<Biome> AMETHYST_PLAINS = register("amethyst_plains");
    
    private static RegistryKey<Biome> register(String name) {
        return RegistryKey.of(RegistryKeys.BIOME, Identifier.of(Caveborn.MOD_ID, name));
    }

    public static void bootstrap(Registerable<Biome> context) {
        GenerationSettings.LookupBackedBuilder generation = new GenerationSettings.LookupBackedBuilder(
            context.getRegistryLookup(RegistryKeys.PLACED_FEATURE),
            context.getRegistryLookup(RegistryKeys.CONFIGURED_CARVER)
        );
        generation.feature(GenerationStep.Feature.SURFACE_STRUCTURES, ModPlacedFeatures.AMETHYST_SPIKE_PLACED_KEY);

        SpawnSettings.Builder spawns = new SpawnSettings.Builder();
        spawns.spawn(SpawnGroup.CREATURE, 8, new SpawnSettings.SpawnEntry(EntityType.SHEEP, 2, 4));
        spawns.spawn(SpawnGroup.CREATURE, 6, new SpawnSettings.SpawnEntry(EntityType.RABBIT, 2, 3));
        spawns.spawn(SpawnGroup.MONSTER, 95, new SpawnSettings.SpawnEntry(EntityType.ZOMBIE, 4, 4));
        spawns.spawn(SpawnGroup.MONSTER, 100, new SpawnSettings.SpawnEntry(EntityType.SKELETON, 4, 4));
        spawns.spawn(SpawnGroup.MONSTER, 100, new SpawnSettings.SpawnEntry(EntityType.SPIDER, 4, 4));
        spawns.spawn(SpawnGroup.MONSTER, 100, new SpawnSettings.SpawnEntry(EntityType.CREEPER, 4, 4));
        spawns.spawn(SpawnGroup.AMBIENT, 10, new SpawnSettings.SpawnEntry(EntityType.BAT, 8, 8));

        context.register(AMETHYST_PLAINS, new Biome.Builder()
            .precipitation(true)
            .temperature(0.65F)
            .downfall(0.35F)
            .effects(new BiomeEffects.Builder()
                .skyColor(0xB82DFF)
                .fogColor(0xC0D0FF)
                .waterColor(0x4F3DFF)
                .waterFogColor(0x050533)
                .grassColor(0x385AFF)
                .foliageColor(0x4674FF)
                .moodSound(BiomeMoodSound.CAVE)
                .build())
            .spawnSettings(spawns.build())
            .generationSettings(generation.build())
            .build());
    }
    
    public static void initialize() {
        Caveborn.LOGGER.info("Registering biomes for " + Caveborn.MOD_ID);
    }
}
