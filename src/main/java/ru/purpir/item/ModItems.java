package ru.purpir.item;

import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.item.HoeItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.equipment.EquipmentType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import ru.purpir.Caveborn;
import ru.purpir.block.ModBlocks;

import java.util.function.Function;

public class ModItems {
    // Misc Items
    public static final Item FIBER = registerItem("fiber", Item::new, new Item.Settings());
    // Solar Crystal
    public static final Item SOLAR_CRYSTAL = registerItem("solar_crystal", Item::new, new Item.Settings());
    public static final Item BAG = registerItem("bag", BagItem::new, new Item.Settings().maxCount(1));
    
    // Bronze Items
    public static final Item BRONZE_INGOT = registerItem("bronze_ingot", Item::new, new Item.Settings());
    public static final Item RAW_BRONZE = registerItem("raw_bronze", Item::new, new Item.Settings());
    
    public static final Item BRONZE_SWORD = registerItem("bronze_sword", Item::new, 
        new Item.Settings().sword(ModToolMaterials.BRONZE, 3, -2.4f));
    public static final Item BRONZE_PICKAXE = registerItem("bronze_pickaxe", Item::new, 
        new Item.Settings().pickaxe(ModToolMaterials.BRONZE, 1, -2.8f));
    public static final Item BRONZE_AXE = registerItem("bronze_axe", Item::new, 
        new Item.Settings().axe(ModToolMaterials.BRONZE, 5, -3.0f));
    public static final Item BRONZE_SHOVEL = registerItem("bronze_shovel", Item::new, 
        new Item.Settings().shovel(ModToolMaterials.BRONZE, 1.5f, -3.0f));
    public static final Item BRONZE_HOE = registerItem("bronze_hoe", 
        settings -> new HoeItem(ModToolMaterials.BRONZE, -3, 0.0f, settings), 
        new Item.Settings());
    
    public static final Item BRONZE_HELMET = registerItem("bronze_helmet", Item::new,
        new Item.Settings().armor(ModArmorMaterials.BRONZE, EquipmentType.HELMET));
    public static final Item BRONZE_CHESTPLATE = registerItem("bronze_chestplate", Item::new,
        new Item.Settings().armor(ModArmorMaterials.BRONZE, EquipmentType.CHESTPLATE));
    public static final Item BRONZE_LEGGINGS = registerItem("bronze_leggings", Item::new,
        new Item.Settings().armor(ModArmorMaterials.BRONZE, EquipmentType.LEGGINGS));
    public static final Item BRONZE_BOOTS = registerItem("bronze_boots", Item::new,
        new Item.Settings().armor(ModArmorMaterials.BRONZE, EquipmentType.BOOTS));

    // Titanium Items
    public static final Item TITANIUM_INGOT = registerItem("titanium_ingot", Item::new, new Item.Settings());
    public static final Item RAW_TITANIUM = registerItem("raw_titanium", Item::new, new Item.Settings());
    
    // Vacuumite Items
    public static final Item VACUUMITE_INGOT = registerItem("vacuumite_ingot", Item::new, new Item.Settings());
    public static final Item RAW_VACUUMITE = registerItem("raw_vacuumite", Item::new, new Item.Settings());

    // Netherite Titanium Items
    public static final Item NETHERITE_TITANIUM_INGOT = registerItem("netherite_titanium_ingot", 
        Item::new, new Item.Settings().fireproof());
    
    public static final Item NETHERITE_TITANIUM_SWORD = registerItem("netherite_titanium_sword", Item::new, 
        new Item.Settings().fireproof().sword(ModToolMaterials.NETHERITE_TITANIUM, 3, -2.4f));
    public static final Item NETHERITE_TITANIUM_PICKAXE = registerItem("netherite_titanium_pickaxe", Item::new, 
        new Item.Settings().fireproof().pickaxe(ModToolMaterials.NETHERITE_TITANIUM, 1, -2.8f));
    public static final Item NETHERITE_TITANIUM_AXE = registerItem("netherite_titanium_axe", Item::new, 
        new Item.Settings().fireproof().axe(ModToolMaterials.NETHERITE_TITANIUM, 5, -3.0f));
    public static final Item NETHERITE_TITANIUM_SHOVEL = registerItem("netherite_titanium_shovel", Item::new, 
        new Item.Settings().fireproof().shovel(ModToolMaterials.NETHERITE_TITANIUM, 1.5f, -3.0f));
    public static final Item NETHERITE_TITANIUM_HOE = registerItem("netherite_titanium_hoe", 
        settings -> new HoeItem(ModToolMaterials.NETHERITE_TITANIUM, -4, 0.0f, settings), 
        new Item.Settings().fireproof());
    
    public static final Item NETHERITE_TITANIUM_HELMET = registerItem("netherite_titanium_helmet", Item::new,
        new Item.Settings().fireproof().armor(ModArmorMaterials.NETHERITE_TITANIUM, EquipmentType.HELMET));
    public static final Item NETHERITE_TITANIUM_CHESTPLATE = registerItem("netherite_titanium_chestplate", Item::new,
        new Item.Settings().fireproof().armor(ModArmorMaterials.NETHERITE_TITANIUM, EquipmentType.CHESTPLATE));
    public static final Item NETHERITE_TITANIUM_LEGGINGS = registerItem("netherite_titanium_leggings", Item::new,
        new Item.Settings().fireproof().armor(ModArmorMaterials.NETHERITE_TITANIUM, EquipmentType.LEGGINGS));
    public static final Item NETHERITE_TITANIUM_BOOTS = registerItem("netherite_titanium_boots", Item::new,
        new Item.Settings().fireproof().armor(ModArmorMaterials.NETHERITE_TITANIUM, EquipmentType.BOOTS));

    public static final RegistryKey<ItemGroup> CAVEBORN_GROUP = RegistryKey.of(RegistryKeys.ITEM_GROUP, 
        Identifier.of(Caveborn.MOD_ID, "caveborn_group"));

    private static Item registerItem(String name, Function<Item.Settings, Item> factory, Item.Settings settings) {
        RegistryKey<Item> key = RegistryKey.of(RegistryKeys.ITEM, Identifier.of(Caveborn.MOD_ID, name));
        return Registry.register(Registries.ITEM, key, factory.apply(settings.registryKey(key)));
    }

    public static void registerModItems() {
        Caveborn.LOGGER.info("Registering Mod Items for " + Caveborn.MOD_ID);
        
        Registry.register(Registries.ITEM_GROUP, CAVEBORN_GROUP,
            FabricItemGroup.builder()
                .icon(() -> new ItemStack(NETHERITE_TITANIUM_INGOT))
                .displayName(Text.translatable("itemGroup.caveborn.caveborn_group"))
                .entries((context, entries) -> {
                    // Misc
                    entries.add(FIBER);
                    entries.add(BAG);
                    entries.add(SOLAR_CRYSTAL);
                    // Bronze
                    entries.add(ModBlocks.BRONZE_ORE);
                    entries.add(ModBlocks.BRONZE_BLOCK);
                    entries.add(BRONZE_INGOT);
                    entries.add(RAW_BRONZE);
                    entries.add(BRONZE_SWORD);
                    entries.add(BRONZE_PICKAXE);
                    entries.add(BRONZE_AXE);
                    entries.add(BRONZE_SHOVEL);
                    entries.add(BRONZE_HOE);
                    entries.add(BRONZE_HELMET);
                    entries.add(BRONZE_CHESTPLATE);
                    entries.add(BRONZE_LEGGINGS);
                    entries.add(BRONZE_BOOTS);
                    // Titanium
                    entries.add(ModBlocks.TITANIUM_ORE);
                    entries.add(ModBlocks.DEEPSLATE_TITANIUM_ORE);
                    entries.add(ModBlocks.TITANIUM_BLOCK);
                    entries.add(ModBlocks.TITANIUM_GRATE);
                    entries.add(ModBlocks.TITANIUM_STAIRS);
                    entries.add(ModBlocks.TITANIUM_SLAB);
                    entries.add(ModBlocks.TITANIUM_BARS);
                    entries.add(ModBlocks.TITANIUM_DOOR);
                    entries.add(ModBlocks.TITANIUM_TRAPDOOR);
                    entries.add(TITANIUM_INGOT);
                    entries.add(RAW_TITANIUM);
                    // Vacuumite
                    entries.add(ModBlocks.VACUUMITE_ORE);
                    entries.add(ModBlocks.VACUUMITE_BLOCK);
                    entries.add(VACUUMITE_INGOT);
                    entries.add(RAW_VACUUMITE);
                    // Netherite Titanium
                    entries.add(ModBlocks.NETHERITE_TITANIUM_BLOCK);
                    entries.add(NETHERITE_TITANIUM_INGOT);
                    entries.add(NETHERITE_TITANIUM_SWORD);
                    entries.add(NETHERITE_TITANIUM_PICKAXE);
                    entries.add(NETHERITE_TITANIUM_AXE);
                    entries.add(NETHERITE_TITANIUM_SHOVEL);
                    entries.add(NETHERITE_TITANIUM_HOE);
                    entries.add(NETHERITE_TITANIUM_HELMET);
                    entries.add(NETHERITE_TITANIUM_CHESTPLATE);
                    entries.add(NETHERITE_TITANIUM_LEGGINGS);
                    entries.add(NETHERITE_TITANIUM_BOOTS);
                })
                .build());
    }
}
