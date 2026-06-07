package ru.purpir.eventaltar;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import ru.purpir.api.SolarInfusionApi;
import ru.purpir.item.ModItems;

import java.util.ArrayList;
import java.util.List;

public final class EventAltarChallengeRewards {
    private EventAltarChallengeRewards() {
    }

    public static List<ItemStack> waveRewards(int level) {
        return waveRewards(level, null);
    }

    public static List<ItemStack> waveRewards(int level, net.minecraft.util.math.random.Random random) {
        int levelBonus = Math.max(0, level - 1);
        List<ItemStack> rewards = new ArrayList<>();
        rewards.add(new ItemStack(Items.DIAMOND, EventAltarQuestPool.amount(random, 9, 13) + levelBonus / 3));
        rewards.add(new ItemStack(ModItems.SOLAR_CRYSTAL, EventAltarQuestPool.amount(random, 12, 18) + levelBonus));
        rewards.add(new ItemStack(Items.EXPERIENCE_BOTTLE, EventAltarQuestPool.amount(random, 28, 42) + levelBonus * 2));
        rewards.add(new ItemStack(Items.GOLDEN_APPLE, 1));
        EventAltarQuestPool.addChance(rewards, random, 0.25F, new ItemStack(Items.NETHERITE_SCRAP, 1));
        EventAltarQuestPool.addInfusedChance(rewards, random, 0.18F, true);
        return rewards;
    }

    public static List<ItemStack> possibleWaveRewards(int level) {
        List<ItemStack> rewards = new ArrayList<>(waveRewards(level));
        rewards.add(new ItemStack(Items.NETHERITE_SCRAP, 1));
        addPossibleInfusedRewards(rewards);
        return rewards;
    }

    public static List<ItemStack> defendRewards(int level) {
        return defendRewards(level, null);
    }

    public static List<ItemStack> defendRewards(int level, net.minecraft.util.math.random.Random random) {
        int levelBonus = Math.max(0, level - 1);
        List<ItemStack> rewards = new ArrayList<>();
        rewards.add(new ItemStack(Items.DIAMOND, EventAltarQuestPool.amount(random, 11, 15) + levelBonus / 3));
        rewards.add(new ItemStack(ModItems.SOLAR_CRYSTAL, EventAltarQuestPool.amount(random, 15, 22) + levelBonus));
        rewards.add(new ItemStack(Items.EXPERIENCE_BOTTLE, EventAltarQuestPool.amount(random, 34, 48) + levelBonus * 2));
        rewards.add(new ItemStack(Items.GOLDEN_APPLE, EventAltarQuestPool.amount(random, 1, 2)));
        EventAltarQuestPool.addChance(rewards, random, 0.35F, new ItemStack(Items.NETHERITE_SCRAP, 1));
        EventAltarQuestPool.addInfusedChance(rewards, random, 0.25F, true);
        EventAltarQuestPool.addChance(rewards, random, 0.04F, new ItemStack(Items.ENCHANTED_GOLDEN_APPLE, 1));
        return rewards;
    }

    public static List<ItemStack> possibleDefendRewards(int level) {
        List<ItemStack> rewards = new ArrayList<>(defendRewards(level));
        rewards.add(new ItemStack(Items.NETHERITE_SCRAP, 1));
        rewards.add(new ItemStack(Items.ENCHANTED_GOLDEN_APPLE, 1));
        addPossibleInfusedRewards(rewards);
        return rewards;
    }

    private static void addPossibleInfusedRewards(List<ItemStack> rewards) {
        Item[] possible = new Item[] {
            Items.IRON_SWORD,
            Items.DIAMOND_SWORD,
            ModItems.BRONZE_SWORD,
            Items.BOW,
            Items.TRIDENT,
            Items.SHIELD,
            ModItems.CRYSTAL_DUST,
            Items.ENDER_PEARL
        };
        for (Item item : possible) {
            rewards.add(SolarInfusionApi.createInfusedCopy(new ItemStack(item)));
        }
    }
}
