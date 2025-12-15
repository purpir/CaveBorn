package ru.purpir.item;

import net.minecraft.item.equipment.ArmorMaterial;
import net.minecraft.item.equipment.EquipmentType;
import net.minecraft.sound.SoundEvents;

import java.util.Map;

public class ModArmorMaterials {
    // Bronze - same as diamond
    public static final ArmorMaterial BRONZE = new ArmorMaterial(
        33,  // durability
        Map.of(
            EquipmentType.HELMET, 3,
            EquipmentType.CHESTPLATE, 8,
            EquipmentType.LEGGINGS, 6,
            EquipmentType.BOOTS, 3
        ),
        10,  // enchantability
        SoundEvents.ITEM_ARMOR_EQUIP_DIAMOND,
        2.0f,  // toughness
        0.0f,  // knockback resistance
        ModTags.Items.BRONZE_REPAIR,
        ModEquipmentAssets.BRONZE
    );

    // Netherite Titanium - 2x netherite protection
    public static final ArmorMaterial NETHERITE_TITANIUM = new ArmorMaterial(
        74,  // durability (2x netherite's 37)
        Map.of(
            EquipmentType.HELMET, 6,
            EquipmentType.CHESTPLATE, 16,
            EquipmentType.LEGGINGS, 12,
            EquipmentType.BOOTS, 6
        ),
        20,  // enchantability
        SoundEvents.ITEM_ARMOR_EQUIP_NETHERITE,
        6.0f,  // toughness
        0.2f,  // knockback resistance
        ModTags.Items.NETHERITE_TITANIUM_REPAIR,
        ModEquipmentAssets.NETHERITE_TITANIUM
    );

    public static void initialize() {
    }
}
