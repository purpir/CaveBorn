package ru.purpir.world;

import net.minecraft.registry.Registerable;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
import net.minecraft.world.gen.YOffset;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.PlacedFeature;
import net.minecraft.world.gen.placementmodifier.*;
import ru.purpir.Caveborn;

import java.util.List;

public class ModPlacedFeatures {
    public static final RegistryKey<PlacedFeature> BRONZE_ORE_PLACED_KEY = registerKey("bronze_ore");
    public static final RegistryKey<PlacedFeature> TITANIUM_ORE_PLACED_KEY = registerKey("titanium_ore");
    public static final RegistryKey<PlacedFeature> DEEPSLATE_TITANIUM_ORE_PLACED_KEY = registerKey("deepslate_titanium_ore");

    public static void bootstrap(Registerable<PlacedFeature> context) {
        var configuredFeatures = context.getRegistryLookup(RegistryKeys.CONFIGURED_FEATURE);

        // Bronze ore in End - common (20 veins per chunk)
        register(context, BRONZE_ORE_PLACED_KEY, 
            configuredFeatures.getOrThrow(ModConfiguredFeatures.BRONZE_ORE_KEY),
            modifiersWithCount(20, HeightRangePlacementModifier.uniform(YOffset.fixed(0), YOffset.fixed(80))));

        // Titanium ore - very rare (1 vein per chunk, only deep underground)
        register(context, TITANIUM_ORE_PLACED_KEY, 
            configuredFeatures.getOrThrow(ModConfiguredFeatures.TITANIUM_ORE_KEY),
            modifiersWithCount(1, HeightRangePlacementModifier.uniform(YOffset.fixed(-64), YOffset.fixed(0))));

        // Deepslate Titanium ore - very rare (1 vein per chunk)
        register(context, DEEPSLATE_TITANIUM_ORE_PLACED_KEY, 
            configuredFeatures.getOrThrow(ModConfiguredFeatures.DEEPSLATE_TITANIUM_ORE_KEY),
            modifiersWithCount(1, HeightRangePlacementModifier.uniform(YOffset.fixed(-64), YOffset.fixed(-32))));
    }

    public static RegistryKey<PlacedFeature> registerKey(String name) {
        return RegistryKey.of(RegistryKeys.PLACED_FEATURE, Identifier.of(Caveborn.MOD_ID, name));
    }

    private static void register(Registerable<PlacedFeature> context, RegistryKey<PlacedFeature> key,
                                 RegistryEntry<ConfiguredFeature<?, ?>> config, List<PlacementModifier> modifiers) {
        context.register(key, new PlacedFeature(config, List.copyOf(modifiers)));
    }

    private static List<PlacementModifier> modifiersWithCount(int count, PlacementModifier heightModifier) {
        return List.of(CountPlacementModifier.of(count), SquarePlacementModifier.of(), heightModifier, BiomePlacementModifier.of());
    }
}
