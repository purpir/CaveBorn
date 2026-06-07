package ru.purpir.world;

import net.minecraft.block.Blocks;
import net.minecraft.registry.Registerable;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.structure.rule.BlockMatchRuleTest;
import net.minecraft.structure.rule.RuleTest;
import net.minecraft.structure.rule.TagMatchRuleTest;
import net.minecraft.util.Identifier;
import net.minecraft.world.gen.feature.*;
import net.minecraft.world.gen.stateprovider.BlockStateProvider;
import ru.purpir.Caveborn;
import ru.purpir.block.ModBlocks;

import java.util.List;

public class ModConfiguredFeatures {
    public static final RegistryKey<ConfiguredFeature<?, ?>> BRONZE_ORE_KEY = registerKey("bronze_ore");
    public static final RegistryKey<ConfiguredFeature<?, ?>> TITANIUM_ORE_KEY = registerKey("titanium_ore");
    public static final RegistryKey<ConfiguredFeature<?, ?>> DEEPSLATE_TITANIUM_ORE_KEY = registerKey("deepslate_titanium_ore");
    public static final RegistryKey<ConfiguredFeature<?, ?>> DEEPSLATE_COBALT_ORE_KEY = registerKey("deepslate_cobalt_ore");
    public static final RegistryKey<ConfiguredFeature<?, ?>> VACUUMITE_ORE_KEY = registerKey("vacuumite_ore");
    public static final RegistryKey<ConfiguredFeature<?, ?>> DEEP_GRANITE_KEY = registerKey("deep_granite");
    public static final RegistryKey<ConfiguredFeature<?, ?>> ASHEN_LIMESTONE_KEY = registerKey("ashen_limestone");
    public static final RegistryKey<ConfiguredFeature<?, ?>> WEED_PATCH_KEY = registerKey("weed_patch");
    public static final RegistryKey<ConfiguredFeature<?, ?>> HOGWEED_PATCH_KEY = registerKey("hogweed_patch");
    public static final RegistryKey<ConfiguredFeature<?, ?>> AMETHYST_SPIKE_KEY = registerKey("amethyst_spike");
    public static final RegistryKey<ConfiguredFeature<?, ?>> SOLAR_IRIS_PATCH_KEY = registerKey("solar_iris_patch");
    public static final RegistryKey<ConfiguredFeature<?, ?>> VOID_EYE_PLANT_PATCH_KEY = registerKey("void_eye_plant_patch");
    public static final RegistryKey<ConfiguredFeature<?, ?>> CRYSTAL_GROWTH_PATCH_KEY = registerKey("crystal_growth_patch");
    public static final RegistryKey<ConfiguredFeature<?, ?>> MINERS_CAMP_KEY = registerKey("miners_camp");
    public static final RegistryKey<ConfiguredFeature<?, ?>> HANGING_MINERS_CACHE_KEY = registerKey("hanging_miners_cache");

    public static void bootstrap(Registerable<ConfiguredFeature<?, ?>> context) {
        RuleTest endStoneReplaceable = new BlockMatchRuleTest(Blocks.END_STONE);
        RuleTest stoneReplaceable = new TagMatchRuleTest(BlockTags.STONE_ORE_REPLACEABLES);
        RuleTest deepslateReplaceable = new TagMatchRuleTest(BlockTags.DEEPSLATE_ORE_REPLACEABLES);

        // Bronze ore in End - common, vein size 8
        register(context, BRONZE_ORE_KEY, Feature.ORE, new OreFeatureConfig(
            List.of(OreFeatureConfig.createTarget(endStoneReplaceable, ModBlocks.BRONZE_ORE.getDefaultState())),
            8));

        // Titanium ore in stone - very rare, vein size 3
        register(context, TITANIUM_ORE_KEY, Feature.ORE, new OreFeatureConfig(
            List.of(OreFeatureConfig.createTarget(stoneReplaceable, ModBlocks.TITANIUM_ORE.getDefaultState())),
            3));

        // Titanium ore in deepslate - very rare, vein size 3
        register(context, DEEPSLATE_TITANIUM_ORE_KEY, Feature.ORE, new OreFeatureConfig(
            List.of(OreFeatureConfig.createTarget(deepslateReplaceable, ModBlocks.DEEPSLATE_TITANIUM_ORE.getDefaultState())),
            3));

        // Cobalt ore in deepslate - uncommon technical material, vein size 5
        register(context, DEEPSLATE_COBALT_ORE_KEY, Feature.ORE, new OreFeatureConfig(
            List.of(OreFeatureConfig.createTarget(deepslateReplaceable, ModBlocks.DEEPSLATE_COBALT_ORE.getDefaultState())),
            5));

        // Vacuumite ore in End - rare, vein size 4
        register(context, VACUUMITE_ORE_KEY, Feature.ORE, new OreFeatureConfig(
            List.of(OreFeatureConfig.createTarget(endStoneReplaceable, ModBlocks.VACUUMITE_ORE.getDefaultState())),
            4));

        // Natural stone variants
        register(context, DEEP_GRANITE_KEY, Feature.ORE, new OreFeatureConfig(
            List.of(
                OreFeatureConfig.createTarget(stoneReplaceable, ModBlocks.DEEP_GRANITE.getDefaultState()),
                OreFeatureConfig.createTarget(deepslateReplaceable, ModBlocks.DEEP_GRANITE.getDefaultState())
            ),
            48));

        register(context, ASHEN_LIMESTONE_KEY, Feature.ORE, new OreFeatureConfig(
            List.of(OreFeatureConfig.createTarget(stoneReplaceable, ModBlocks.ASHEN_LIMESTONE.getDefaultState())),
            40));

        // Weed patch - random flower-like patch on grass
        register(context, WEED_PATCH_KEY, Feature.FLOWER, new RandomPatchFeatureConfig(
            50, // tries (20-50 сорняков)
            6,  // xz spread
            2,  // y spread
            PlacedFeatures.createEntry(Feature.SIMPLE_BLOCK,
                new SimpleBlockFeatureConfig(BlockStateProvider.of(ModBlocks.WEED)))
        ));

        // Hogweed (Борщевик) patch - использует нашу кастомную Feature
        register(context, HOGWEED_PATCH_KEY, ModFeatures.HOGWEED, DefaultFeatureConfig.INSTANCE);
        register(context, AMETHYST_SPIKE_KEY, ModFeatures.AMETHYST_SPIKE, DefaultFeatureConfig.INSTANCE);
        register(context, SOLAR_IRIS_PATCH_KEY, Feature.FLOWER, new RandomPatchFeatureConfig(
            32,
            5,
            2,
            PlacedFeatures.createEntry(Feature.SIMPLE_BLOCK,
                new SimpleBlockFeatureConfig(BlockStateProvider.of(ModBlocks.SOLAR_IRIS)))
        ));

        register(context, VOID_EYE_PLANT_PATCH_KEY, Feature.FLOWER, new RandomPatchFeatureConfig(
            12,
            3,
            1,
            PlacedFeatures.createEntry(Feature.SIMPLE_BLOCK,
                new SimpleBlockFeatureConfig(BlockStateProvider.of(ModBlocks.VOID_EYE_PLANT)))
        ));

        register(context, CRYSTAL_GROWTH_PATCH_KEY, ModFeatures.CRYSTAL_GROWTH, DefaultFeatureConfig.INSTANCE);
        register(context, MINERS_CAMP_KEY, ModFeatures.MINERS_CAMP, DefaultFeatureConfig.INSTANCE);
        register(context, HANGING_MINERS_CACHE_KEY, ModFeatures.HANGING_MINERS_CACHE, DefaultFeatureConfig.INSTANCE);
    }

    public static RegistryKey<ConfiguredFeature<?, ?>> registerKey(String name) {
        return RegistryKey.of(RegistryKeys.CONFIGURED_FEATURE, Identifier.of(Caveborn.MOD_ID, name));
    }

    private static <FC extends FeatureConfig, F extends Feature<FC>> void register(
            Registerable<ConfiguredFeature<?, ?>> context, RegistryKey<ConfiguredFeature<?, ?>> key, 
            F feature, FC config) {
        context.register(key, new ConfiguredFeature<>(feature, config));
    }
}
