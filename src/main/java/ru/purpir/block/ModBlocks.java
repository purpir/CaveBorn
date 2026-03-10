package ru.purpir.block;

import net.minecraft.block.*;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundEvents;
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

    // Vacuumite - rare End material, easy to mine (stone pickaxe)
    public static final Block VACUUMITE_ORE = registerBlock("vacuumite_ore",
        settings -> new ExperienceDroppingBlock(UniformIntProvider.create(4, 9), settings),
        AbstractBlock.Settings.create()
            .strength(1.5f, 1.5f)
            .requiresTool()
            .sounds(BlockSoundGroup.STONE));
    
    public static final Block VACUUMITE_BLOCK = registerBlock("vacuumite_block",
        Block::new,
        AbstractBlock.Settings.create()
            .strength(2.0f, 2.0f)
            .requiresTool()
            .sounds(BlockSoundGroup.METAL));

    // Weed blocks
    public static final Block WEED = registerBlockNoItem("weed",
        WeedBlock::new,
        AbstractBlock.Settings.create()
            .noCollision()
            .breakInstantly()
            .sounds(BlockSoundGroup.GRASS)
            .ticksRandomly());
    
    public static final Block WEED_TOP = registerBlockNoItem("weed_top",
        WeedTopBlock::new,
        AbstractBlock.Settings.create()
            .noCollision()
            .breakInstantly()
            .sounds(BlockSoundGroup.GRASS));

    // Hogweed (Борщевик) - высокое растение 2-8 блоков, НЕ растёт
    public static final Block HOGWEED = registerBlockNoItem("hogweed",
        HogweedBlock::new,
        AbstractBlock.Settings.create()
            .noCollision()
            .breakInstantly()
            .sounds(BlockSoundGroup.GRASS));

    // Titanium building blocks
    public static final Block TITANIUM_GRATE = registerBlock("titanium_grate",
        GrateBlock::new,
        AbstractBlock.Settings.create()
            .strength(50.0f, 1200.0f)
            .requiresTool()
            .sounds(BlockSoundGroup.COPPER_GRATE)
            .nonOpaque()
            .allowsSpawning(Blocks::never)
            .solidBlock(Blocks::never)
            .suffocates(Blocks::never)
            .blockVision(Blocks::never));

    public static final Block TITANIUM_STAIRS = registerBlock("titanium_stairs",
        settings -> new StairsBlock(TITANIUM_BLOCK.getDefaultState(), settings),
        AbstractBlock.Settings.create()
            .strength(50.0f, 1200.0f)
            .requiresTool()
            .sounds(BlockSoundGroup.METAL));

    public static final Block TITANIUM_SLAB = registerBlock("titanium_slab",
        SlabBlock::new,
        AbstractBlock.Settings.create()
            .strength(50.0f, 1200.0f)
            .requiresTool()
            .sounds(BlockSoundGroup.METAL));

    public static final Block TITANIUM_BARS = registerBlock("titanium_bars",
        PaneBlock::new,
        AbstractBlock.Settings.create()
            .strength(50.0f, 1200.0f)
            .requiresTool()
            .sounds(BlockSoundGroup.METAL)
            .nonOpaque());

    public static final BlockSetType TITANIUM_BLOCK_SET_TYPE = new BlockSetType(
        "titanium",
        false, // canOpenByHand - false для редстоун-only (как железная дверь)
        false, // canOpenByWindCharge
        false, // canButtonBeActivatedByArrows
        BlockSetType.ActivationRule.MOBS, // pressurePlateSensitivity
        BlockSoundGroup.METAL,
        SoundEvents.BLOCK_IRON_DOOR_CLOSE,
        SoundEvents.BLOCK_IRON_DOOR_OPEN,
        SoundEvents.BLOCK_IRON_TRAPDOOR_CLOSE,
        SoundEvents.BLOCK_IRON_TRAPDOOR_OPEN,
        SoundEvents.BLOCK_METAL_PRESSURE_PLATE_CLICK_OFF,
        SoundEvents.BLOCK_METAL_PRESSURE_PLATE_CLICK_ON,
        SoundEvents.BLOCK_STONE_BUTTON_CLICK_OFF,
        SoundEvents.BLOCK_STONE_BUTTON_CLICK_ON
    );

    public static final Block TITANIUM_DOOR = registerBlock("titanium_door",
        settings -> new DoorBlock(TITANIUM_BLOCK_SET_TYPE, settings),
        AbstractBlock.Settings.create()
            .mapColor(MapColor.LIGHT_GRAY)
            .strength(50.0f, 1200.0f)
            .requiresTool()
            .nonOpaque()
            .pistonBehavior(PistonBehavior.DESTROY));

    public static final Block TITANIUM_TRAPDOOR = registerBlock("titanium_trapdoor",
        settings -> new TrapdoorBlock(TITANIUM_BLOCK_SET_TYPE, settings),
        AbstractBlock.Settings.create()
            .mapColor(MapColor.LIGHT_GRAY)
            .strength(50.0f, 1200.0f)
            .requiresTool()
            .nonOpaque()
            .allowsSpawning(Blocks::never));

    // Example multiblock block (for testing)
    public static final Block EXAMPLE_MULTIBLOCK = registerBlock("example_multiblock",
        ru.purpir.multiblock.example.ExampleMultiblockBlock::new,
        AbstractBlock.Settings.create()
            .strength(5.0f, 6.0f)
            .requiresTool()
            .sounds(BlockSoundGroup.METAL));
    
    // Test colored block (for testing dynamic textures)
    public static final Block TEST_COLORED_BLOCK = registerBlock("test_colored_block",
        TestColoredBlock::new,
        AbstractBlock.Settings.create()
            .strength(3.0f, 3.0f)
            .requiresTool()
            .sounds(BlockSoundGroup.STONE));

    private static Block registerBlock(String name, Function<AbstractBlock.Settings, Block> factory, 
                                        AbstractBlock.Settings settings) {
        RegistryKey<Block> blockKey = RegistryKey.of(RegistryKeys.BLOCK, Identifier.of(Caveborn.MOD_ID, name));
        Block block = Registry.register(Registries.BLOCK, blockKey, factory.apply(settings.registryKey(blockKey)));
        registerBlockItem(name, block);
        return block;
    }

    private static Block registerBlockNoItem(String name, Function<AbstractBlock.Settings, Block> factory, 
                                              AbstractBlock.Settings settings) {
        RegistryKey<Block> blockKey = RegistryKey.of(RegistryKeys.BLOCK, Identifier.of(Caveborn.MOD_ID, name));
        return Registry.register(Registries.BLOCK, blockKey, factory.apply(settings.registryKey(blockKey)));
    }

    private static void registerBlockItem(String name, Block block) {
        RegistryKey<Item> itemKey = RegistryKey.of(RegistryKeys.ITEM, Identifier.of(Caveborn.MOD_ID, name));
        Registry.register(Registries.ITEM, itemKey, new BlockItem(block, new Item.Settings().registryKey(itemKey).useBlockPrefixedTranslationKey()));
    }

    public static void registerModBlocks() {
        Caveborn.LOGGER.info("Registering Mod Blocks for " + Caveborn.MOD_ID);
    }
}
