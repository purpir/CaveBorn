package ru.purpir.ability;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import ru.purpir.component.CooldownComponent;
import ru.purpir.component.ModComponents;
import ru.purpir.enchantment.SolarInfusionSystem;
import ru.purpir.util.TargetFinder;

import java.util.Optional;

public class WoodenSwordAbility implements SwordAbility {
    
    @Override
    public boolean canUse(ItemStack stack) {
        return stack.isOf(Items.WOODEN_SWORD) && SolarInfusionSystem.isInfused(stack);
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
        Vec3d velocity = new Vec3d(0, 2.0, 0); // Подбрасываем вверх
        entity.setVelocity(velocity);
        entity.velocityModified = true;
        
        stack.set(ModComponents.ABILITY_COOLDOWN, new CooldownComponent(currentTime));
        player.sendMessage(Text.translatable("ability.caveborn.wooden_sword").formatted(Formatting.GREEN), true);
        
        return true;
    }
    
    @Override
    public String getAbilityName() {
        return "wooden_sword";
    }
}
