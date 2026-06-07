package ru.purpir.world;

import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.minecraft.world.gen.feature.DefaultFeatureConfig;
import net.minecraft.world.gen.feature.Feature;
import ru.purpir.Caveborn;

public class ModFeatures {
    public static final Feature<DefaultFeatureConfig> HOGWEED = new HogweedFeature(DefaultFeatureConfig.CODEC);
    public static final Feature<DefaultFeatureConfig> AMETHYST_SPIKE = new AmethystSpikeFeature(DefaultFeatureConfig.CODEC);
    public static final Feature<DefaultFeatureConfig> CRYSTAL_GROWTH = new CrystalGrowthFeature(DefaultFeatureConfig.CODEC);
    public static final Feature<DefaultFeatureConfig> MINERS_CAMP = new MinersCampFeature(DefaultFeatureConfig.CODEC);
    public static final Feature<DefaultFeatureConfig> HANGING_MINERS_CACHE = new HangingMinersCacheFeature(DefaultFeatureConfig.CODEC);
    public static final Feature<DefaultFeatureConfig> BROKEN_ALTAR = new BrokenAltarFeature(DefaultFeatureConfig.CODEC);
    
    public static void register() {
        Registry.register(Registries.FEATURE, Identifier.of(Caveborn.MOD_ID, "hogweed"), HOGWEED);
        Registry.register(Registries.FEATURE, Identifier.of(Caveborn.MOD_ID, "amethyst_spike"), AMETHYST_SPIKE);
        Registry.register(Registries.FEATURE, Identifier.of(Caveborn.MOD_ID, "crystal_growth"), CRYSTAL_GROWTH);
        Registry.register(Registries.FEATURE, Identifier.of(Caveborn.MOD_ID, "miners_camp"), MINERS_CAMP);
        Registry.register(Registries.FEATURE, Identifier.of(Caveborn.MOD_ID, "hanging_miners_cache"), HANGING_MINERS_CACHE);
        Registry.register(Registries.FEATURE, Identifier.of(Caveborn.MOD_ID, "broken_altar"), BROKEN_ALTAR);
    }
}
