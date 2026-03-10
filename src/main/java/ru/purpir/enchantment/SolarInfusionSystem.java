package ru.purpir.enchantment;

import net.minecraft.component.type.AttributeModifierSlot;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import ru.purpir.component.ModComponents;
import ru.purpir.item.ModItems;

public class SolarInfusionSystem {
    
    private static final Identifier SOLAR_DAMAGE_MODIFIER_ID = Identifier.of("caveborn", "solar_infusion_damage");
    
    public static boolean canInfuse(ItemStack sword, ItemStack crystal) {
        // Проверяем что это меч (имеет sword компонент) и солнечный кристалл
        return sword.getItem().getComponents().contains(net.minecraft.component.DataComponentTypes.TOOL) &&
               crystal.isOf(ModItems.SOLAR_CRYSTAL) &&
               !isInfused(sword);
    }
    
    public static boolean isInfused(ItemStack stack) {
        return stack.getOrDefault(ModComponents.SOLAR_INFUSED, false);
    }
    
    public static ItemStack infuseSword(ItemStack sword, ItemStack crystal) {
        if (!canInfuse(sword, crystal)) {
            return ItemStack.EMPTY;
        }
        
        ItemStack result = sword.copy();
        result.set(ModComponents.SOLAR_INFUSED, true);
        result.set(net.minecraft.component.DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        
        // Добавляем модификатор урона
        AttributeModifiersComponent modifiers = result.getOrDefault(
            net.minecraft.component.DataComponentTypes.ATTRIBUTE_MODIFIERS, 
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
        
        result.set(net.minecraft.component.DataComponentTypes.ATTRIBUTE_MODIFIERS, builder.build());
        
        return result;
    }
    
    public static float getAdditionalDamage(ItemStack stack) {
        return isInfused(stack) ? 2.0f : 0.0f;
    }
}
