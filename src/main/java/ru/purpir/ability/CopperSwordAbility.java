package ru.purpir.ability;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.BreezeEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;
import ru.purpir.component.CooldownComponent;
import ru.purpir.component.ModComponents;
import ru.purpir.enchantment.SolarInfusionSystem;
import ru.purpir.util.PetMobUtil;

public class CopperSwordAbility implements SwordAbility {
    
    private static final int BREEZE_COUNT = 3; // Количество вихрей
    private static final long BREEZE_DURATION = 200; // 10 секунд (200 тиков)
    
    @Override
    public boolean canUse(ItemStack stack) {
        return stack.isOf(Items.COPPER_SWORD) && SolarInfusionSystem.isInfused(stack);
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
        
        ServerWorld serverWorld = (ServerWorld) world;
        
        // Призываем вихрей вокруг игрока
        for (int i = 0; i < BREEZE_COUNT; i++) {
            double angle = (2 * Math.PI * i) / BREEZE_COUNT;
            double radius = 2.0;
            double x = player.getX() + Math.cos(angle) * radius;
            double z = player.getZ() + Math.sin(angle) * radius;
            double y = player.getY();
            
            BreezeEntity breeze = EntityType.BREEZE.create(serverWorld, net.minecraft.entity.SpawnReason.MOB_SUMMONED);
            if (breeze != null) {
                breeze.refreshPositionAndAngles(x, y, z, 0, 0);
                serverWorld.spawnEntity(breeze);
                
                // Регистрируем как временного питомца ПОСЛЕ спавна
                PetMobUtil.registerTemporaryPet(player, breeze, BREEZE_DURATION, serverWorld);
                
                // Делаем вихря пассивным и настраиваем AI
                breeze.setAiDisabled(false);
                
                // Эффекты призыва
                serverWorld.spawnParticles(ParticleTypes.CLOUD,
                    x, y + 1, z,
                    20, 0.3, 0.5, 0.3, 0.05);
            }
        }
        
        // Звук и сообщение
        world.playSound(null, player.getBlockPos(),
            SoundEvents.ENTITY_BOGGED_STEP,
            SoundCategory.PLAYERS, 1.0f, 1.0f);
        
        stack.set(ModComponents.ABILITY_COOLDOWN, new CooldownComponent(currentTime));
        player.sendMessage(Text.translatable("ability.caveborn.copper_sword").formatted(Formatting.GOLD), true);
        
        return true;
    }
    
    @Override
    public String getAbilityName() {
        return "copper_sword";
    }
}
