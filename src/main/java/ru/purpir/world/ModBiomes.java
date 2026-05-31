package ru.purpir.world;

import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.world.biome.Biome;
import ru.purpir.Caveborn;

public class ModBiomes {
    public static final RegistryKey<Biome> AMETHYST_PLAINS = register("amethyst_plains");
    
    private static RegistryKey<Biome> register(String name) {
        return RegistryKey.of(RegistryKeys.BIOME, Identifier.of(Caveborn.MOD_ID, name));
    }
    
    public static void initialize() {
        Caveborn.LOGGER.info("Registering biomes for " + Caveborn.MOD_ID);
    }
}
