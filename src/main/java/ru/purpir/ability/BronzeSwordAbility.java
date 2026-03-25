package ru.purpir.ability;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import ru.purpir.component.CooldownComponent;
import ru.purpir.component.ModComponents;
import ru.purpir.enchantment.SolarInfusionSystem;
import ru.purpir.item.ModItems;

public class BronzeSwordAbility implements SwordAbility {
    
    @Override
    public boolean canUse(ItemStack stack) {
        return stack.isOf(ModItems.BRONZE_SWORD) && SolarInfusionSystem.isInfused(stack);
    }
    
    @Override
    public boolean tryUse(PlayerEntity player, World world, ItemStack stack) {
        if (!canUse(stack) || world.isClient()) {
            return false;
        }
        
        // Проверка конфигурации
        if (!ru.purpir.config.SolarAbilityConfig.getInstance().isAbilityEnabled(getAbilityName())) {
            player.sendMessage(net.minecraft.text.Text.translatable("ability.caveborn.disabled").formatted(net.minecraft.util.Formatting.RED), true);
            return false;
        }
        
        long currentTime = world.getTime();
        CooldownComponent cooldown = stack.getOrDefault(ModComponents.ABILITY_COOLDOWN, CooldownComponent.DEFAULT);
        
        if (cooldown.isOnCooldown(currentTime, COOLDOWN_TICKS)) {
            long remainingSeconds = cooldown.getRemainingCooldown(currentTime, COOLDOWN_TICKS) / 20;
            player.sendMessage(Text.translatable("ability.caveborn.cooldown", remainingSeconds).formatted(Formatting.RED), true);
            return false;
        }
        
        ServerWorld serverWorld = (ServerWorld) world;
        
        // Телепортируем как хорус - случайная позиция в радиусе 8 блоков
        for (int i = 0; i < 16; i++) {
            double x = player.getX() + (world.random.nextDouble() - 0.5) * 16;
            double y = player.getY() + (world.random.nextInt(16) - 8);
            double z = player.getZ() + (world.random.nextDouble() - 0.5) * 16;
            
            BlockPos targetPos = new BlockPos((int) x, (int) y, (int) z);
            
            if (world.getBlockState(targetPos).isReplaceable() && 
                world.getBlockState(targetPos.up()).isReplaceable() &&
                !world.getBlockState(targetPos.down()).isReplaceable()) {
                
                // Эффекты телепортации
                serverWorld.spawnParticles(ParticleTypes.PORTAL, 
                    player.getX(), player.getY(), player.getZ(), 
                    50, 0.5, 0.5, 0.5, 0.1);
                
                player.teleport(x, y, z, true);
                
                serverWorld.spawnParticles(ParticleTypes.PORTAL, 
                    x, y, z, 50, 0.5, 0.5, 0.5, 0.1);
                
                world.playSound(null, player.getBlockPos(), 
                    SoundEvents.ITEM_CHORUS_FRUIT_TELEPORT, 
                    SoundCategory.PLAYERS, 1.0f, 1.0f);
                
                stack.set(ModComponents.ABILITY_COOLDOWN, new CooldownComponent(currentTime));
                player.sendMessage(Text.translatable("ability.caveborn.bronze_sword").formatted(Formatting.GOLD), true);
                
                return true;
            }
        }
        
        player.sendMessage(Text.translatable("ability.caveborn.teleport_failed").formatted(Formatting.RED), true);
        return false;
    }
    
    @Override
    public String getAbilityName() {
        return "bronze_sword";
    }
}
