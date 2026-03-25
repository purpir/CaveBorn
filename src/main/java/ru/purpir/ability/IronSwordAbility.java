package ru.purpir.ability;

import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import ru.purpir.component.CooldownComponent;
import ru.purpir.component.ModComponents;
import ru.purpir.enchantment.SolarInfusionSystem;
import ru.purpir.util.TargetFinder;

import java.util.Optional;

public class IronSwordAbility implements SwordAbility {
    
    @Override
    public boolean canUse(ItemStack stack) {
        return stack.isOf(Items.IRON_SWORD) && SolarInfusionSystem.isInfused(stack);
    }
    
    @Override
    public boolean tryUse(PlayerEntity player, World world, ItemStack stack) {
        if (!canUse(stack) || world.isClient()) {
            return false;
        }
        
        // Проверка конфигурации
        if (!ru.purpir.config.SolarAbilityConfig.getInstance().isAbilityEnabled(getAbilityName())) {
            player.sendMessage(Text.translatable("ability.caveborn.disabled").formatted(Formatting.RED), true);
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
        ServerWorld serverWorld = (ServerWorld) world;
        
        // Ставим 10 наковален блоками над целью
        for (int i = 0; i < 10; i++) {
            BlockPos anvilPos = new BlockPos(
                (int) (entity.getX() + (world.random.nextDouble() - 0.5) * 2),
                (int) entity.getY() + 10 + i,
                (int) (entity.getZ() + (world.random.nextDouble() - 0.5) * 2)
            );
            if (world.getBlockState(anvilPos).isReplaceable()) {
                world.setBlockState(anvilPos,Blocks.ANVIL.getDefaultState());
            }
        }
        
        stack.set(ModComponents.ABILITY_COOLDOWN, new CooldownComponent(currentTime));
        player.sendMessage(Text.translatable("ability.caveborn.iron_sword").formatted(Formatting.WHITE), true);
        
        return true;
    }
    
    @Override
    public String getAbilityName() {
        return "iron_sword";
    }
}
