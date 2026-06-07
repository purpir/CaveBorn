package ru.purpir.enchantment;

import net.minecraft.component.type.AttributeModifierSlot;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import ru.purpir.api.SolarInfusionApi;
import ru.purpir.component.ModComponents;
import ru.purpir.item.ModItems;

public class SolarInfusionSystem {
    
    private static final Identifier SOLAR_DAMAGE_MODIFIER_ID = Identifier.of("caveborn", "solar_infusion_damage");
    
    public static boolean canInfuse(ItemStack item, ItemStack crystal) {
        // Проверяем что это меч (имеет TOOL компонент) или заряд ветра, и солнечный кристалл
        boolean isSword = item.getItem().getComponents().contains(net.minecraft.component.DataComponentTypes.TOOL);
        boolean isWindCharge = item.isOf(net.minecraft.item.Items.WIND_CHARGE);
        boolean isVacuumiteMagnet = item.isOf(ModItems.VACUUMITE_MAGNET);
        boolean isShield = item.isOf(net.minecraft.item.Items.SHIELD);
        boolean isBow = item.isOf(net.minecraft.item.Items.BOW);
        boolean isSolarArrow = item.isOf(net.minecraft.item.Items.ARROW) || item.isOf(net.minecraft.item.Items.SPECTRAL_ARROW);
        boolean isTrident = item.isOf(net.minecraft.item.Items.TRIDENT);
        boolean isCrystalDust = item.isOf(ModItems.CRYSTAL_DUST);
        boolean isTotem = item.isOf(net.minecraft.item.Items.TOTEM_OF_UNDYING);
        boolean isEnderPearl = item.isOf(net.minecraft.item.Items.ENDER_PEARL);
        boolean isHogweedPaste = item.isOf(ru.purpir.block.ModBlocks.HOGWEED_PASTE.asItem());
        boolean isRustedMinerKey = item.isOf(ModItems.RUSTED_MINER_KEY);
        boolean isBronzeAxe = item.isOf(ModItems.BRONZE_AXE);
        boolean isApiRegistered = SolarInfusionApi.isRegisteredInfusable(item);
        
        return (isSword || isWindCharge || isVacuumiteMagnet || isShield || isBow || isSolarArrow || isTrident || isCrystalDust || isTotem || isEnderPearl || isHogweedPaste || isRustedMinerKey || isBronzeAxe || isApiRegistered) &&
               crystal.isOf(ModItems.SOLAR_CRYSTAL) &&
               !isInfused(item);
    }
    
    public static boolean isInfused(ItemStack stack) {
        return stack.getOrDefault(ModComponents.SOLAR_INFUSED, false);
    }
    
    public static ItemStack infuseSword(ItemStack item, ItemStack crystal) {
        if (!canInfuse(item, crystal)) {
            return ItemStack.EMPTY;
        }
        
        ItemStack result = item.copy();
        result.set(ModComponents.SOLAR_INFUSED, true);
        result.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);

        if (result.isOf(ModItems.RUSTED_MINER_KEY)) {
            result.setCount(1);
            result.set(DataComponentTypes.MAX_STACK_SIZE, 1);
            result.set(DataComponentTypes.MAX_DAMAGE, 20);
            result.set(DataComponentTypes.DAMAGE, 0);
            return result;
        }
        
        // Для зарядов ветра не добавляем модификатор урона
        if (SolarInfusionApi.isRegisteredInfusable(result) ||
            result.isOf(net.minecraft.item.Items.WIND_CHARGE) || result.isOf(ModItems.VACUUMITE_MAGNET) ||
            result.isOf(net.minecraft.item.Items.SHIELD) || result.isOf(net.minecraft.item.Items.BOW) ||
            result.isOf(net.minecraft.item.Items.ARROW) || result.isOf(net.minecraft.item.Items.SPECTRAL_ARROW) ||
            result.isOf(net.minecraft.item.Items.TRIDENT) || result.isOf(ModItems.CRYSTAL_DUST) ||
            result.isOf(net.minecraft.item.Items.TOTEM_OF_UNDYING) || result.isOf(ModItems.VACUUMITE_SWORD) ||
            result.isOf(net.minecraft.item.Items.ENDER_PEARL) || result.isOf(ru.purpir.block.ModBlocks.HOGWEED_PASTE.asItem()) ||
            result.isOf(ModItems.BRONZE_AXE)) {
            return result;
        }
        
        // Добавляем модификатор урона только для мечей
        AttributeModifiersComponent modifiers = result.getOrDefault(
            DataComponentTypes.ATTRIBUTE_MODIFIERS, 
            AttributeModifiersComponent.DEFAULT
        );
        
        AttributeModifiersComponent.Builder builder = AttributeModifiersComponent.builder();
        
        // Копируем существующие модификаторы
        for (var entry : modifiers.modifiers()) {
            builder.add(
                entry.attribute(),
                entry.modifier(),
                entry.slot()
            );
        }
        
        // Добавляем солнечный модификатор урона
        builder.add(
            EntityAttributes.ATTACK_DAMAGE,
            new EntityAttributeModifier(
                SOLAR_DAMAGE_MODIFIER_ID,
                2.0,
                EntityAttributeModifier.Operation.ADD_VALUE
            ),
            AttributeModifierSlot.MAINHAND
        );
        
        result.set(DataComponentTypes.ATTRIBUTE_MODIFIERS, builder.build());
        
        return result;
    }
    
    public static float getAdditionalDamage(ItemStack stack) {
        if (stack.isOf(net.minecraft.item.Items.BOW) || stack.isOf(net.minecraft.item.Items.ARROW) ||
            stack.isOf(net.minecraft.item.Items.SPECTRAL_ARROW) || stack.isOf(net.minecraft.item.Items.TRIDENT) ||
            stack.isOf(ModItems.RUSTED_MINER_KEY) || stack.isOf(ModItems.BRONZE_AXE)) {
            return 0.0f;
        }

        float apiBonus = SolarInfusionApi.getRegisteredAttackDamageBonus(stack);
        if (apiBonus > 0.0F) {
            return isInfused(stack) ? apiBonus : 0.0F;
        }

        return isInfused(stack) ? 2.0f : 0.0f;
    }
}
