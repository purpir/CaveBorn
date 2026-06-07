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
    public static final RegistryKey<PlacedFeature> DEEPSLATE_COBALT_ORE_PLACED_KEY = registerKey("deepslate_cobalt_ore");
    public static final RegistryKey<PlacedFeature> ZINC_ORE_PLACED_KEY = registerKey("zinc_ore");
    public static final RegistryKey<PlacedFeature> DEEPSLATE_ZINC_ORE_PLACED_KEY = registerKey("deepslate_zinc_ore");
    public static final RegistryKey<PlacedFeature> VACUUMITE_ORE_PLACED_KEY = registerKey("vacuumite_ore");
    public static final RegistryKey<PlacedFeature> DEEP_GRANITE_PLACED_KEY = registerKey("deep_granite");
    public static final RegistryKey<PlacedFeature> ASHEN_LIMESTONE_PLACED_KEY = registerKey("ashen_limestone");
    public static final RegistryKey<PlacedFeature> WEED_PATCH_PLACED_KEY = registerKey("weed_patch");
    public static final RegistryKey<PlacedFeature> HOGWEED_PATCH_PLACED_KEY = registerKey("hogweed_patch");
    public static final RegistryKey<PlacedFeature> AMETHYST_SPIKE_PLACED_KEY = registerKey("amethyst_spike");
    public static final RegistryKey<PlacedFeature> SOLAR_IRIS_PATCH_PLACED_KEY = registerKey("solar_iris_patch");
    public static final RegistryKey<PlacedFeature> VOID_EYE_PLANT_PATCH_PLACED_KEY = registerKey("void_eye_plant_patch");
    public static final RegistryKey<PlacedFeature> CRYSTAL_GROWTH_PATCH_PLACED_KEY = registerKey("crystal_growth_patch");
    public static final RegistryKey<PlacedFeature> MINERS_CAMP_PLACED_KEY = registerKey("miners_camp");
    public static final RegistryKey<PlacedFeature> HANGING_MINERS_CACHE_PLACED_KEY = registerKey("hanging_miners_cache");
    public static final RegistryKey<PlacedFeature> BROKEN_ALTAR_PLACED_KEY = registerKey("broken_altar");

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

        // Deepslate Cobalt ore - uncommon, useful for future machinery
        register(context, DEEPSLATE_COBALT_ORE_PLACED_KEY,
            configuredFeatures.getOrThrow(ModConfiguredFeatures.DEEPSLATE_COBALT_ORE_KEY),
            modifiersWithCount(4, HeightRangePlacementModifier.uniform(YOffset.fixed(-64), YOffset.fixed(-16))));

        // Zinc ore - common across most overworld mining levels
        register(context, ZINC_ORE_PLACED_KEY,
            configuredFeatures.getOrThrow(ModConfiguredFeatures.ZINC_ORE_KEY),
            modifiersWithCount(12, HeightRangePlacementModifier.uniform(YOffset.fixed(-32), YOffset.fixed(112))));

        register(context, DEEPSLATE_ZINC_ORE_PLACED_KEY,
            configuredFeatures.getOrThrow(ModConfiguredFeatures.DEEPSLATE_ZINC_ORE_KEY),
            modifiersWithCount(10, HeightRangePlacementModifier.uniform(YOffset.fixed(-64), YOffset.fixed(16))));

        // Vacuumite ore in End - rare (3 veins per chunk)
        register(context, VACUUMITE_ORE_PLACED_KEY, 
            configuredFeatures.getOrThrow(ModConfiguredFeatures.VACUUMITE_ORE_KEY),
            modifiersWithCount(3, HeightRangePlacementModifier.uniform(YOffset.fixed(0), YOffset.fixed(80))));

        // Deep Granite - deep overworld stone pockets
        register(context, DEEP_GRANITE_PLACED_KEY,
            configuredFeatures.getOrThrow(ModConfiguredFeatures.DEEP_GRANITE_KEY),
            modifiersWithCount(3, HeightRangePlacementModifier.uniform(YOffset.fixed(-64), YOffset.fixed(16))));

        // Ashen Limestone - upper and middle overworld stone pockets
        register(context, ASHEN_LIMESTONE_PLACED_KEY,
            configuredFeatures.getOrThrow(ModConfiguredFeatures.ASHEN_LIMESTONE_KEY),
            modifiersWithCount(4, HeightRangePlacementModifier.uniform(YOffset.fixed(0), YOffset.fixed(96))));

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

        register(context, AMETHYST_SPIKE_PLACED_KEY,
            configuredFeatures.getOrThrow(ModConfiguredFeatures.AMETHYST_SPIKE_KEY),
            List.of(
                RarityFilterPlacementModifier.of(14),
                SquarePlacementModifier.of(),
                PlacedFeatures.WORLD_SURFACE_WG_HEIGHTMAP,
                BiomePlacementModifier.of()
            ));

        register(context, SOLAR_IRIS_PATCH_PLACED_KEY,
            configuredFeatures.getOrThrow(ModConfiguredFeatures.SOLAR_IRIS_PATCH_KEY),
            List.of(
                RarityFilterPlacementModifier.of(3),
                SquarePlacementModifier.of(),
                PlacedFeatures.MOTION_BLOCKING_HEIGHTMAP,
                BiomePlacementModifier.of()
            ));

        register(context, VOID_EYE_PLANT_PATCH_PLACED_KEY,
            configuredFeatures.getOrThrow(ModConfiguredFeatures.VOID_EYE_PLANT_PATCH_KEY),
            List.of(
                RarityFilterPlacementModifier.of(5),
                SquarePlacementModifier.of(),
                PlacedFeatures.WORLD_SURFACE_WG_HEIGHTMAP,
                BiomePlacementModifier.of()
            ));

        register(context, CRYSTAL_GROWTH_PATCH_PLACED_KEY,
            configuredFeatures.getOrThrow(ModConfiguredFeatures.CRYSTAL_GROWTH_PATCH_KEY),
            List.of(
                CountPlacementModifier.of(3),
                SquarePlacementModifier.of(),
                HeightRangePlacementModifier.uniform(YOffset.fixed(-48), YOffset.fixed(48)),
                BiomePlacementModifier.of()
            ));

        register(context, MINERS_CAMP_PLACED_KEY,
            configuredFeatures.getOrThrow(ModConfiguredFeatures.MINERS_CAMP_KEY),
            List.of(
                RarityFilterPlacementModifier.of(28),
                SquarePlacementModifier.of(),
                HeightRangePlacementModifier.uniform(YOffset.fixed(-60), YOffset.fixed(-8)),
                BiomePlacementModifier.of()
            ));

        register(context, HANGING_MINERS_CACHE_PLACED_KEY,
            configuredFeatures.getOrThrow(ModConfiguredFeatures.HANGING_MINERS_CACHE_KEY),
            List.of(
                RarityFilterPlacementModifier.of(42),
                SquarePlacementModifier.of(),
                HeightRangePlacementModifier.uniform(YOffset.fixed(-58), YOffset.fixed(-12)),
                BiomePlacementModifier.of()
            ));

        register(context, BROKEN_ALTAR_PLACED_KEY,
            configuredFeatures.getOrThrow(ModConfiguredFeatures.BROKEN_ALTAR_KEY),
            List.of(
                RarityFilterPlacementModifier.of(96),
                SquarePlacementModifier.of(),
                PlacedFeatures.WORLD_SURFACE_WG_HEIGHTMAP,
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
