package ru.purpir.item;

import net.minecraft.item.equipment.EquipmentAsset;
import net.minecraft.item.equipment.EquipmentAssetKeys;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;
import ru.purpir.Caveborn;

public class ModEquipmentAssets {
    public static final RegistryKey<EquipmentAsset> BRONZE = of("bronze");
    public static final RegistryKey<EquipmentAsset> NETHERITE_TITANIUM = of("netherite_titanium");

    private static RegistryKey<EquipmentAsset> of(String name) {
        return RegistryKey.of(EquipmentAssetKeys.REGISTRY_KEY, Identifier.of(Caveborn.MOD_ID, name));
    }
}
