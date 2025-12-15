package ru.purpir.block;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.ExperienceDroppingBlock;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.intprovider.UniformIntProvider;
import ru.purpir.Caveborn;

import java.util.function.Function;

public class ModBlocks {
    // Bronze Ore - spawns in End
    public static final Block BRONZE_ORE = registerBlock("bronze_ore",
        settings -> new ExperienceDroppingBlock(UniformIntProvider.create(3, 7), settings),
        AbstractBlock.Settings.create()
            .strength(3.0f, 3.0f)
            .requiresTool()
            .sounds(BlockSoundGroup.STONE));
    
    public static final Block BRONZE_BLOCK = registerBlock("bronze_block",
        Block::new,
        AbstractBlock.Settings.create()
            .strength(5.0f, 6.0f)
            .requiresTool()
            .sounds(BlockSoundGroup.METAL));

    // Titanium Ore - spawns in Overworld, very rare, needs netherite pickaxe
    public static final Block TITANIUM_ORE = registerBlock("titanium_ore",
        settings -> new ExperienceDroppingBlock(UniformIntProvider.create(5, 10), settings),
        AbstractBlock.Settings.create()
            .strength(50.0f, 1200.0f)
            .requiresTool()
            .sounds(BlockSoundGroup.STONE));
    
    public static final Block DEEPSLATE_TITANIUM_ORE = registerBlock("deepslate_titanium_ore",
        settings -> new ExperienceDroppingBlock(UniformIntProvider.create(5, 10), settings),
        AbstractBlock.Settings.create()
            .strength(55.0f, 1200.0f)
            .requiresTool()
            .sounds(BlockSoundGroup.DEEPSLATE));
    
    public static final Block TITANIUM_BLOCK = registerBlock("titanium_block",
        Block::new,
        AbstractBlock.Settings.create()
            .strength(50.0f, 1200.0f)
            .requiresTool()
            .sounds(BlockSoundGroup.METAL));

    // Netherite Titanium Block
    public static final Block NETHERITE_TITANIUM_BLOCK = registerBlock("netherite_titanium_block",
        Block::new,
        AbstractBlock.Settings.create()
            .strength(100.0f, 2400.0f)
            .requiresTool()
            .sounds(BlockSoundGroup.NETHERITE));

    private static Block registerBlock(String name, Function<AbstractBlock.Settings, Block> factory, 
                                        AbstractBlock.Settings settings) {
        RegistryKey<Block> blockKey = RegistryKey.of(RegistryKeys.BLOCK, Identifier.of(Caveborn.MOD_ID, name));
        Block block = Registry.register(Registries.BLOCK, blockKey, factory.apply(settings.registryKey(blockKey)));
        registerBlockItem(name, block);
        return block;
    }

    private static void registerBlockItem(String name, Block block) {
        RegistryKey<Item> itemKey = RegistryKey.of(RegistryKeys.ITEM, Identifier.of(Caveborn.MOD_ID, name));
        Registry.register(Registries.ITEM, itemKey, new BlockItem(block, new Item.Settings().registryKey(itemKey).useBlockPrefixedTranslationKey()));
    }

    public static void registerModBlocks() {
        Caveborn.LOGGER.info("Registering Mod Blocks for " + Caveborn.MOD_ID);
    }
}
