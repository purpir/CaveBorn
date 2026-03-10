package ru.purpir.ability;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public interface SwordAbility {
    
    long COOLDOWN_TICKS = 1200; // 60 секунд
    double SEARCH_RADIUS = 20.0;
    
    boolean canUse(ItemStack stack);
    
    boolean tryUse(PlayerEntity player, World world, ItemStack stack);
    
    String getAbilityName();
}
