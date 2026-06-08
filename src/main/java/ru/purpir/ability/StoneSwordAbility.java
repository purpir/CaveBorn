package ru.purpir.ability;

import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;
import ru.purpir.component.CooldownComponent;
import ru.purpir.component.ModComponents;
import ru.purpir.enchantment.SolarInfusionSystem;
import ru.purpir.util.WallManager;

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

        WallManager.createFromPlayerLook(player, world, Blocks.STONE.getDefaultState(), 5, 5, 2, true, 20, false);

        stack.set(ModComponents.ABILITY_COOLDOWN, new CooldownComponent(currentTime));
        player.sendMessage(Text.translatable("ability.caveborn.stone_sword").formatted(Formatting.GRAY), true);
        return true;
    }

    @Override
    public String getAbilityName() {
        return "stone_sword";
    }
}
