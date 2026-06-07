package ru.purpir.api;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import ru.purpir.component.ModComponents;
import ru.purpir.enchantment.SolarInfusionSystem;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * Public API for mods that want their own items to work with CaveBorn's Solar Infusion.
 *
 * <p>Call these methods from your mod initializer after depending on CaveBorn.
 */
public final class SolarInfusionApi {
    private static final List<Entry> ENTRIES = new ArrayList<>();

    private SolarInfusionApi() {
    }

    /**
     * Allows a single item to receive Solar Infusion in an anvil.
     */
    public static void registerInfusable(Item item) {
        registerInfusable(item, 0.0F);
    }

    /**
     * Allows a single item to receive Solar Infusion and optionally grants extra attack damage
     * through CaveBorn's attack hook while the item is infused.
     */
    public static void registerInfusable(Item item, float attackDamageBonus) {
        Objects.requireNonNull(item, "item");
        registerInfusable(stack -> stack.isOf(item), attackDamageBonus);
    }

    /**
     * Allows matching item stacks to receive Solar Infusion in an anvil.
     */
    public static void registerInfusable(Predicate<ItemStack> predicate) {
        registerInfusable(predicate, 0.0F);
    }

    /**
     * Allows matching item stacks to receive Solar Infusion and optionally grants extra attack damage
     * through CaveBorn's attack hook while the item is infused.
     */
    public static void registerInfusable(Predicate<ItemStack> predicate, float attackDamageBonus) {
        Objects.requireNonNull(predicate, "predicate");
        ENTRIES.add(new Entry(predicate, Math.max(0.0F, attackDamageBonus)));
    }

    /**
     * Checks CaveBorn's Solar Infusion component on a stack.
     */
    public static boolean isInfused(ItemStack stack) {
        return SolarInfusionSystem.isInfused(stack);
    }

    /**
     * Creates an infused copy of the stack without checking anvil ingredients.
     */
    public static ItemStack createInfusedCopy(ItemStack stack) {
        ItemStack result = stack.copy();
        result.set(ModComponents.SOLAR_INFUSED, true);
        result.set(net.minecraft.component.DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        return result;
    }

    public static boolean isRegisteredInfusable(ItemStack stack) {
        for (Entry entry : ENTRIES) {
            if (entry.predicate.test(stack)) {
                return true;
            }
        }
        return false;
    }

    public static float getRegisteredAttackDamageBonus(ItemStack stack) {
        float bonus = 0.0F;
        for (Entry entry : ENTRIES) {
            if (entry.predicate.test(stack)) {
                bonus = Math.max(bonus, entry.attackDamageBonus);
            }
        }
        return bonus;
    }

    private record Entry(Predicate<ItemStack> predicate, float attackDamageBonus) {
    }
}
