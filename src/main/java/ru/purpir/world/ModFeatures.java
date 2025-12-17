package ru.purpir.world;

import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.minecraft.world.gen.feature.DefaultFeatureConfig;
import net.minecraft.world.gen.feature.Feature;
import ru.purpir.Caveborn;

public class ModFeatures {
    public static final Feature<DefaultFeatureConfig> HOGWEED = new HogweedFeature(DefaultFeatureConfig.CODEC);
    
    public static void register() {
        Registry.register(Registries.FEATURE, Identifier.of(Caveborn.MOD_ID, "hogweed"), HOGWEED);
    }
}
