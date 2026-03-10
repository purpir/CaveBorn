package ru.purpir.ability;

import net.minecraft.block.Blocks;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import ru.purpir.component.CooldownComponent;
import ru.purpir.component.ModComponents;
import ru.purpir.enchantment.SolarInfusionSystem;
import ru.purpir.util.TargetFinder;

import java.util.Optional;

public class NetheriteSwordAbility implements SwordAbility {
    
    @Override
    public boolean canUse(ItemStack stack) {
        return stack.isOf(Items.NETHERITE_SWORD) && SolarInfusionSystem.isInfused(stack);
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
        
        Optional<LivingEntity> target = TargetFinder.findNearestEntity(world, player, SEARCH_RADIUS);
        if (target.isEmpty()) {
            player.sendMessage(Text.translatable("ability.caveborn.no_target").formatted(Formatting.RED), true);
            return false;
        }
        
        LivingEntity entity = target.get();
        BlockPos underPos = entity.getBlockPos().down();
        
        // Ставим лаву под целью - проверяем несколько вариантов
        boolean lavaPlaced = false;
        if (world.getBlockState(underPos).isReplaceable()) {
            world.setBlockState(underPos, Blocks.LAVA.getDefaultState());
            lavaPlaced = true;
        } else if (world.getBlockState(underPos).isAir()) {
            world.setBlockState(underPos, Blocks.LAVA.getDefaultState());
            lavaPlaced = true;
        } else {
            // Если под ногами не заменяемый блок, ставим лаву на уровне ног
            BlockPos feetPos = entity.getBlockPos();
            if (world.getBlockState(feetPos).isReplaceable() || world.getBlockState(feetPos).isAir()) {
                world.setBlockState(feetPos, Blocks.LAVA.getDefaultState());
                lavaPlaced = true;
            }
        }
        
        // Накладываем иссушение 3 уровня на 10 секунд (200 тиков)
        entity.addStatusEffect(new StatusEffectInstance(StatusEffects.WITHER, 200, 2));
        
        // Поджигаем цель
        entity.setOnFireFor(10);
        
        stack.set(ModComponents.ABILITY_COOLDOWN, new CooldownComponent(currentTime));
        player.sendMessage(Text.translatable("ability.caveborn.netherite_sword").formatted(Formatting.DARK_GRAY), true);
        
        return true;
    }
    
    @Override
    public String getAbilityName() {
        return "netherite_sword";
    }
}
