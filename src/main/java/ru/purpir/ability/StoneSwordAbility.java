package ru.purpir.ability;

import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import ru.purpir.component.CooldownComponent;
import ru.purpir.component.ModComponents;
import ru.purpir.enchantment.SolarInfusionSystem;

public class StoneSwordAbility implements SwordAbility {
    
    @Override
    public boolean canUse(ItemStack stack) {
        return stack.isOf(Items.STONE_SWORD) && SolarInfusionSystem.isInfused(stack);
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
        
        // Получаем направление взгляда игрока
        Direction facing = player.getHorizontalFacing();
        BlockPos startPos = player.getBlockPos().offset(facing, 2);
        
        // Создаем стену 5x5x2 (ширина x высота x глубина)
        for (int width = -2; width <= 2; width++) {
            for (int height = 0; height < 5; height++) {
                for (int depth = 0; depth < 2; depth++) {
                    BlockPos pos;
                    // Определяем позицию в зависимости от направления
                    if (facing == Direction.NORTH || facing == Direction.SOUTH) {
                        pos = startPos.add(width, height, depth * facing.getOffsetZ());
                    } else {
                        pos = startPos.add(depth * facing.getOffsetX(), height, width);
                    }
                    
                    if (world.getBlockState(pos).isReplaceable()) {
                        world.setBlockState(pos, Blocks.STONE.getDefaultState());
                    }
                }
            }
        }
        
        stack.set(ModComponents.ABILITY_COOLDOWN, new CooldownComponent(currentTime));
        player.sendMessage(Text.translatable("ability.caveborn.stone_sword").formatted(Formatting.GRAY), true);
        
        return true;
    }
    
    @Override
    public String getAbilityName() {
        return "stone_sword";
    }
}
