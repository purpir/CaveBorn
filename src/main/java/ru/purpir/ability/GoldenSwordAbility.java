package ru.purpir.ability;

import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;
import ru.purpir.component.CooldownComponent;
import ru.purpir.component.ModComponents;
import ru.purpir.enchantment.SolarInfusionSystem;

public class GoldenSwordAbility implements SwordAbility {
    
    @Override
    public boolean canUse(ItemStack stack) {
        return stack.isOf(Items.GOLDEN_SWORD) && SolarInfusionSystem.isInfused(stack);
    }
    
    @Override
    public boolean tryUse(PlayerEntity player, World world, ItemStack stack) {
        if (!canUse(stack) || world.isClient()) {
            return false;
        }
        
        long currentTime = world.getTime();
        CooldownComponent cooldown = stack.getOrDefault(ModComponents.ABILITY_COOLDOWN, CooldownComponent.DEFAULT);
        
        if (cooldown.isOnCooldown(currentTime, COOLDOWN_TICKS)) {
            long remainingSeconds = cooldown.getRemainingCooldown(currentTime, COOLDOWN_TICKS) / 20;
            player.sendMessage(Text.translatable("ability.caveborn.cooldown", remainingSeconds).formatted(Formatting.RED), true);
            return false;
        }
        
        // Даем силу 3 уровня на 5 секунд (100 тиков)
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.STRENGTH, 100, 2));
        
        stack.set(ModComponents.ABILITY_COOLDOWN, new CooldownComponent(currentTime));
        player.sendMessage(Text.translatable("ability.caveborn.golden_sword").formatted(Formatting.GOLD), true);
        
        return true;
    }
    
    @Override
    public String getAbilityName() {
        return "golden_sword";
    }
}
