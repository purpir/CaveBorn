package ru.purpir.ability;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.WindChargeEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import ru.purpir.component.CooldownComponent;
import ru.purpir.component.ModComponents;
import ru.purpir.enchantment.SolarInfusionSystem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class MaceAbility implements SwordAbility {
    
    private static final long MACE_COOLDOWN_TICKS = 20; // 1 секунда
    private static final long SECOND_SHOT_DELAY_TICKS = 20; // 1 секунда
    private static final Map<ServerWorld, List<ScheduledShot>> SCHEDULED_SHOTS = new HashMap<>();

    public static void registerTicker() {
        ServerTickEvents.END_WORLD_TICK.register(MaceAbility::tickWorld);
    }
    
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
        
        boolean hasInfusedWindCharges = hasInfusedWindCharge(player);
        fireWindCharge(player, world);

        // При солнечном заряде ветра во второй руке второй выстрел не расходует предмет.
        if (hasInfusedWindCharges && world instanceof ServerWorld serverWorld) {
            SCHEDULED_SHOTS.computeIfAbsent(serverWorld, ignored -> new ArrayList<>())
                .add(new ScheduledShot(player, serverWorld.getTime() + SECOND_SHOT_DELAY_TICKS));
        }
        
        // Устанавливаем кулдаун
        stack.set(ModComponents.ABILITY_COOLDOWN, new CooldownComponent(currentTime));
        
        return true;
    }

    public static boolean hasInfusedWindCharge(PlayerEntity player) {
        ItemStack stack = player.getOffHandStack();
        return stack.isOf(Items.WIND_CHARGE) && SolarInfusionSystem.isInfused(stack);
    }

    private static void fireWindCharge(PlayerEntity player, World world) {
        Vec3d lookVec = player.getRotationVec(1.0F);
        Vec3d spawnPos = player.getEyePos().add(lookVec.multiply(0.5));

        // Конструктор с игроком сохраняет владельца и позволяет солнечной инфузии
        // заряда корректно обработать взрыв.
        WindChargeEntity windCharge = new WindChargeEntity(
            player, world, spawnPos.x, spawnPos.y, spawnPos.z
        );
        windCharge.setVelocity(lookVec.multiply(1.5));
        world.spawnEntity(windCharge);
        world.playSound(null, player.getBlockPos(), SoundEvents.ENTITY_BREEZE_SHOOT,
            SoundCategory.PLAYERS, 1.0f, 1.0f);
    }

    private static void tickWorld(ServerWorld world) {
        List<ScheduledShot> shots = SCHEDULED_SHOTS.get(world);
        if (shots == null) {
            return;
        }

        Iterator<ScheduledShot> iterator = shots.iterator();
        while (iterator.hasNext()) {
            ScheduledShot shot = iterator.next();
            if (world.getTime() < shot.fireAt()) {
                continue;
            }
            if (shot.player().isAlive() && shot.player().getEntityWorld() == world) {
                fireWindCharge(shot.player(), world);
            }
            iterator.remove();
        }

        if (shots.isEmpty()) {
            SCHEDULED_SHOTS.remove(world);
        }
    }

    private record ScheduledShot(PlayerEntity player, long fireAt) {
    }
    
    @Override
    public String getAbilityName() {
        return "mace";
    }
}
