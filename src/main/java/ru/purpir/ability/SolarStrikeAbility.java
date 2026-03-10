package ru.purpir.ability;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;
import ru.purpir.component.CooldownComponent;
import ru.purpir.component.ModComponents;
import ru.purpir.enchantment.SolarInfusionSystem;
import ru.purpir.item.ModItems;
import ru.purpir.util.TargetFinder;

import java.util.Optional;

public class SolarStrikeAbility implements SwordAbility {
    
    private static final int LIGHTNING_COUNT = 10;
    private static final int LIGHTNING_DELAY = 2;
    
    @Override
    public boolean canUse(ItemStack stack) {
        return stack.isOf(ModItems.NETHERITE_TITANIUM_SWORD) && 
               SolarInfusionSystem.isInfused(stack);
    }
    
    @Override
    public boolean tryUse(PlayerEntity player, World world, ItemStack stack) {
        if (!canUse(stack)) {
            return false;
        }
        
        if (world.isClient()) {
            return true;
        }
        
        ServerWorld serverWorld = (ServerWorld) world;
        long currentTime = world.getTime();
        
        // Проверяем кулдаун
        CooldownComponent cooldown = stack.getOrDefault(ModComponents.ABILITY_COOLDOWN, CooldownComponent.DEFAULT);
        if (cooldown.isOnCooldown(currentTime, COOLDOWN_TICKS)) {
            long remainingTicks = cooldown.getRemainingCooldown(currentTime, COOLDOWN_TICKS);
            long remainingSeconds = remainingTicks / 20;
            player.sendMessage(
                Text.translatable("ability.caveborn.solar_strike.cooldown", remainingSeconds)
                    .formatted(Formatting.RED),
                true
            );
            return false;
        }
        
        // Ищем ближайшую цель
        Optional<LivingEntity> target = TargetFinder.findNearestEntity(world, player, SEARCH_RADIUS);
        
        if (target.isEmpty()) {
            player.sendMessage(
                Text.translatable("ability.caveborn.solar_strike.no_target")
                    .formatted(Formatting.RED),
                true
            );
            return false;
        }
        
        LivingEntity targetEntity = target.get();
        
        // Запускаем серию молний
        spawnLightningStrike(serverWorld, targetEntity, 0);
        
        // Устанавливаем кулдаун
        stack.set(ModComponents.ABILITY_COOLDOWN, new CooldownComponent(currentTime));
        
        player.sendMessage(
            Text.translatable("ability.caveborn.solar_strike.activated")
                .formatted(Formatting.GOLD),
            true
        );
        
        return true;
    }
    
    private static void spawnLightningStrike(ServerWorld world, LivingEntity target, int count) {
        if (count >= LIGHTNING_COUNT || !target.isAlive()) {
            return;
        }
        
        // Спавним молнию
        LightningEntity lightning = EntityType.LIGHTNING_BOLT.create(world, SpawnReason.TRIGGERED);
        if (lightning != null) {
            lightning.refreshPositionAfterTeleport(target.getX(), target.getY(), target.getZ());
            world.spawnEntity(lightning);
        }
        
        // Планируем следующую молнию через LIGHTNING_DELAY тиков
        int nextCount = count + 1;
        world.getServer().execute(() -> {
            scheduleNextLightning(world, target, nextCount, LIGHTNING_DELAY);
        });
    }
    
    private static void scheduleNextLightning(ServerWorld world, LivingEntity target, int count, int delay) {
        if (delay > 0) {
            world.getServer().execute(() -> {
                scheduleNextLightning(world, target, count, delay - 1);
            });
        } else {
            spawnLightningStrike(world, target, count);
        }
    }
    
    @Override
    public String getAbilityName() {
        return "solar_strike";
    }
}
