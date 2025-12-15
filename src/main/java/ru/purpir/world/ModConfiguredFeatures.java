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
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.FeatureConfig;
import net.minecraft.world.gen.feature.OreFeatureConfig;
import ru.purpir.Caveborn;
import ru.purpir.block.ModBlocks;

import java.util.List;

public class ModConfiguredFeatures {
    public static final RegistryKey<ConfiguredFeature<?, ?>> BRONZE_ORE_KEY = registerKey("bronze_ore");
    public static final RegistryKey<ConfiguredFeature<?, ?>> TITANIUM_ORE_KEY = registerKey("titanium_ore");
    public static final RegistryKey<ConfiguredFeature<?, ?>> DEEPSLATE_TITANIUM_ORE_KEY = registerKey("deepslate_titanium_ore");

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
