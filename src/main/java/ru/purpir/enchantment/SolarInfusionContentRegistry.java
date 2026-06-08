package ru.purpir.enchantment;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import ru.purpir.api.SolarInfusionApi;
import ru.purpir.block.ModBlocks;
import ru.purpir.item.ModItems;

public final class SolarInfusionContentRegistry {
    private SolarInfusionContentRegistry() {
    }

    public static boolean hasContent(ItemStack stack) {
        return SolarInfusionApi.isRegisteredInfusable(stack) ||
            stack.isOf(Items.WOODEN_SWORD) ||
            stack.isOf(Items.STONE_SWORD) ||
            stack.isOf(Items.GOLDEN_SWORD) ||
            stack.isOf(Items.IRON_SWORD) ||
            stack.isOf(Items.DIAMOND_SWORD) ||
            stack.isOf(Items.NETHERITE_SWORD) ||
            stack.isOf(Items.COPPER_SWORD) ||
            stack.isOf(Items.MACE) ||
            stack.isOf(ModItems.BRONZE_SWORD) ||
            stack.isOf(ModItems.VACUUMITE_SWORD) ||
            stack.isOf(ModItems.NETHERITE_TITANIUM_SWORD) ||
            stack.isOf(Items.SHIELD) ||
            stack.isOf(ModItems.VACUUMITE_MAGNET) ||
            stack.isOf(Items.BOW) ||
            stack.isOf(Items.TRIDENT) ||
            stack.isOf(Items.ARROW) ||
            stack.isOf(Items.SPECTRAL_ARROW) ||
            stack.isOf(Items.WIND_CHARGE) ||
            stack.isOf(ModItems.CRYSTAL_DUST) ||
            stack.isOf(Items.TOTEM_OF_UNDYING) ||
            stack.isOf(Items.ENDER_PEARL) ||
            stack.isOf(ModBlocks.HOGWEED_PASTE.asItem()) ||
            stack.isOf(ModItems.RUSTED_MINER_KEY) ||
            stack.isOf(ModItems.BRONZE_AXE) ||
            stack.isOf(ModItems.ZINC_KNIFE) ||
            stack.isOf(ModItems.CRACK_HAMMER);
    }
}
