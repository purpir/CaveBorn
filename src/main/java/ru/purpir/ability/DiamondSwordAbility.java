package ru.purpir.ability;

import net.minecraft.block.Blocks;
import net.minecraft.entity.LivingEntity;
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

public class DiamondSwordAbility implements SwordAbility {
    
    @Override
    public boolean canUse(ItemStack stack) {
        return stack.isOf(Items.DIAMOND_SWORD) && SolarInfusionSystem.isInfused(stack);
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
        BlockPos centerPos = entity.getBlockPos();
        
        // Создаем куб 3x3x3 из паутины вокруг цели
        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -1; z <= 1; z++) {
                    BlockPos pos = centerPos.add(x, y, z);
                    if (world.getBlockState(pos).isReplaceable()) {
                        world.setBlockState(pos, Blocks.COBWEB.getDefaultState());
                    }
                }
            }
        }
        
        stack.set(ModComponents.ABILITY_COOLDOWN, new CooldownComponent(currentTime));
        player.sendMessage(Text.translatable("ability.caveborn.diamond_sword").formatted(Formatting.AQUA), true);
        
        return true;
    }
    
    @Override
    public String getAbilityName() {
        return "diamond_sword";
    }
}
