package ru.purpir.eventaltar;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import ru.purpir.item.ModItems;

import java.util.ArrayList;
import java.util.List;

public final class EventAltarChallengeRewards {
    private EventAltarChallengeRewards() {
    }

    public static List<ItemStack> waveRewards(int level) {
        List<ItemStack> rewards = new ArrayList<>();
        rewards.add(new ItemStack(Items.DIAMOND, 2 + level));
        rewards.add(new ItemStack(ModItems.SOLAR_CRYSTAL, 4 + level));
        rewards.add(new ItemStack(Items.EXPERIENCE_BOTTLE, 12 + level * 2));
        if (level >= 5) {
            rewards.add(new ItemStack(Items.NETHERITE_SCRAP, 1 + level / 4));
        }
        return rewards;
    }

    public static List<ItemStack> defendRewards(int level) {
        List<ItemStack> rewards = new ArrayList<>();
        rewards.add(new ItemStack(Items.GOLDEN_APPLE, 2 + level / 2));
        rewards.add(new ItemStack(ModItems.SOLAR_CRYSTAL, 5 + level));
        rewards.add(new ItemStack(Items.DIAMOND, 1 + level));
        if (level >= 7) {
            rewards.add(new ItemStack(Items.ENCHANTED_GOLDEN_APPLE, 1));
        }
        return rewards;
    }
}
