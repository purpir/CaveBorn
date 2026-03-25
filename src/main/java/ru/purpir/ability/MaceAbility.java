package ru.purpir.ability;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.WindChargeEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import ru.purpir.component.CooldownComponent;
import ru.purpir.component.ModComponents;
import ru.purpir.enchantment.SolarInfusionSystem;

public class MaceAbility implements SwordAbility {
    
    private static final long MACE_COOLDOWN_TICKS = 20; // 1 секунда
    
    @Override
    public boolean canUse(ItemStack stack) {
        return stack.isOf(Items.MACE) && SolarInfusionSystem.isInfused(stack);
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
        
        if (cooldown.isOnCooldown(currentTime, MACE_COOLDOWN_TICKS)) {
            long remainingTicks = cooldown.getRemainingCooldown(currentTime, MACE_COOLDOWN_TICKS);
            double remainingSeconds = remainingTicks / 20.0;
            player.sendMessage(Text.translatable("ability.caveborn.cooldown", String.format("%.1f", remainingSeconds)).formatted(Formatting.RED), true);
            return false;
        }
        
        // Получаем направление взгляда игрока
        Vec3d lookVec = player.getRotationVec(1.0F);
        
        // Создаем заряд ветра
        WindChargeEntity windCharge = new WindChargeEntity(EntityType.WIND_CHARGE, world);
        windCharge.setOwner(player);
        
        // Позиция спавна - перед игроком на уровне глаз
        Vec3d spawnPos = player.getEyePos().add(lookVec.multiply(0.5));
        windCharge.setPosition(spawnPos);
        
        // Устанавливаем скорость заряда ветра
        windCharge.setVelocity(lookVec.multiply(1.5));
        
        // Спавним заряд
        world.spawnEntity(windCharge);
        
        // Звук выстрела
        world.playSound(null, player.getBlockPos(),
            SoundEvents.ENTITY_BREEZE_SHOOT,
            SoundCategory.PLAYERS, 1.0f, 1.0f);
        
        // Устанавливаем кулдаун
        stack.set(ModComponents.ABILITY_COOLDOWN, new CooldownComponent(currentTime));
        
        return true;
    }
    
    @Override
    public String getAbilityName() {
        return "mace";
    }
}
