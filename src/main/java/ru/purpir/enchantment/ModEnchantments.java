package ru.purpir.enchantment;

import net.minecraft.component.type.AttributeModifierSlot;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.effect.EnchantmentEffectTarget;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.util.Identifier;
import ru.purpir.Caveborn;

public class ModEnchantments {
    public static final RegistryKey<Enchantment> VEIN_MINER = RegistryKey.of(
        RegistryKeys.ENCHANTMENT,
        Identifier.of(Caveborn.MOD_ID, "vein_miner")
    );

    public static void register() {
        Caveborn.LOGGER.info("Registering enchantments");
    }
}
