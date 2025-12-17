package ru.purpir.world;

import net.minecraft.registry.Registerable;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
import net.minecraft.world.gen.YOffset;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.PlacedFeature;
import net.minecraft.world.gen.feature.PlacedFeatures;
import net.minecraft.world.gen.placementmodifier.*;
import ru.purpir.Caveborn;

import java.util.List;

public class ModPlacedFeatures {
    public static final RegistryKey<PlacedFeature> BRONZE_ORE_PLACED_KEY = registerKey("bronze_ore");
    public static final RegistryKey<PlacedFeature> TITANIUM_ORE_PLACED_KEY = registerKey("titanium_ore");
    public static final RegistryKey<PlacedFeature> DEEPSLATE_TITANIUM_ORE_PLACED_KEY = registerKey("deepslate_titanium_ore");
    public static final RegistryKey<PlacedFeature> WEED_PATCH_PLACED_KEY = registerKey("weed_patch");
    public static final RegistryKey<PlacedFeature> HOGWEED_PATCH_PLACED_KEY = registerKey("hogweed_patch");

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

        // Weed patch - rare in plains (1 per 32 chunks roughly)
        register(context, WEED_PATCH_PLACED_KEY,
            configuredFeatures.getOrThrow(ModConfiguredFeatures.WEED_PATCH_KEY),
            List.of(
                RarityFilterPlacementModifier.of(32), // 1 в 32 чанках - редко
                SquarePlacementModifier.of(),
                PlacedFeatures.MOTION_BLOCKING_HEIGHTMAP,
                BiomePlacementModifier.of()
            ));

        // Hogweed patch - moderate in taiga/jungle (1 per 8 chunks)
        register(context, HOGWEED_PATCH_PLACED_KEY,
            configuredFeatures.getOrThrow(ModConfiguredFeatures.HOGWEED_PATCH_KEY),
            List.of(
                RarityFilterPlacementModifier.of(8), // 1 в 8 чанках - умеренно
                SquarePlacementModifier.of(),
                PlacedFeatures.MOTION_BLOCKING_HEIGHTMAP,
                BiomePlacementModifier.of()
            ));
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
