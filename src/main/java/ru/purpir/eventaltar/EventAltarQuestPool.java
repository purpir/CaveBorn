package ru.purpir.eventaltar;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import ru.purpir.api.SolarInfusionApi;
import ru.purpir.item.ModItems;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public final class EventAltarQuestPool {
    public static final int RARITY_COMMON = 0;
    public static final int RARITY_HARD = 1;
    public static final int RARITY_HARDCORE = 2;

    public static final int TYPE_KILL_HOSTILE = 0;
    public static final int TYPE_KILL_UNDEAD = 1;
    public static final int TYPE_BREAK_STONE = 2;
    public static final int TYPE_BREAK_DEEPSLATE = 3;
    public static final int TYPE_BREAK_AMETHYST = 4;
    public static final int TYPE_USE_SOLAR_ITEM = 5;

    private EventAltarQuestPool() {
    }

    public static List<EventAltarSavedData.QuestState> generate(long hour, int altarLevel) {
        Random random = new Random(hour * 31L + altarLevel * 17L);
        List<EventAltarSavedData.QuestState> quests = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            quests.add(create(i, RARITY_COMMON, random, altarLevel));
        }
        quests.add(create(3, RARITY_HARD, random, altarLevel));
        quests.add(create(4, RARITY_HARDCORE, random, altarLevel));
        return quests;
    }

    private static EventAltarSavedData.QuestState create(int id, int rarity, Random random, int altarLevel) {
        int[] pool = switch (rarity) {
            case RARITY_HARD -> new int[] { TYPE_KILL_UNDEAD, TYPE_BREAK_DEEPSLATE, TYPE_BREAK_AMETHYST, TYPE_USE_SOLAR_ITEM };
            case RARITY_HARDCORE -> new int[] { TYPE_KILL_HOSTILE, TYPE_KILL_UNDEAD, TYPE_USE_SOLAR_ITEM };
            default -> new int[] { TYPE_KILL_HOSTILE, TYPE_BREAK_STONE, TYPE_BREAK_DEEPSLATE, TYPE_BREAK_AMETHYST };
        };
        int type = pool[random.nextInt(pool.length)];
        int base = switch (type) {
            case TYPE_BREAK_STONE, TYPE_BREAK_DEEPSLATE -> 32;
            case TYPE_BREAK_AMETHYST -> 12;
            case TYPE_USE_SOLAR_ITEM -> 5;
            default -> 8;
        };
        int multiplier = switch (rarity) {
            case RARITY_HARD -> 2;
            case RARITY_HARDCORE -> 3;
            default -> 1;
        };
        int target = base * multiplier + Math.max(0, altarLevel - 1) * multiplier * 2;
        return new EventAltarSavedData.QuestState(id, type, rarity, target, 0, "", false, false);
    }

    public static int xp(EventAltarSavedData.QuestState quest) {
        return switch (quest.rarity()) {
            case RARITY_HARD -> 55;
            case RARITY_HARDCORE -> 95;
            default -> 25;
        };
    }

    public static List<ItemStack> rewards(EventAltarSavedData.QuestState quest, int altarLevel) {
        return rewards(quest, altarLevel, null);
    }

    public static List<ItemStack> rewards(EventAltarSavedData.QuestState quest, int altarLevel, net.minecraft.util.math.random.Random random) {
        int levelBonus = Math.max(0, altarLevel - 1);
        List<ItemStack> rewards = new ArrayList<>();
        switch (quest.rarity()) {
            case RARITY_HARD -> {
                rewards.add(new ItemStack(Items.DIAMOND, amount(random, 7, 10) + levelBonus / 2));
                rewards.add(new ItemStack(ModItems.SOLAR_CRYSTAL, amount(random, 10, 15) + levelBonus));
                rewards.add(new ItemStack(Items.EXPERIENCE_BOTTLE, amount(random, 24, 36) + levelBonus * 2));
                rewards.add(new ItemStack(Items.GOLDEN_APPLE, 1));
                addChance(rewards, random, 0.20F, new ItemStack(Items.NETHERITE_SCRAP, 1));
                addInfusedChance(rewards, random, 0.12F, false);
            }
            case RARITY_HARDCORE -> {
                rewards.add(new ItemStack(Items.DIAMOND, amount(random, 12, 16) + levelBonus / 2));
                rewards.add(new ItemStack(ModItems.SOLAR_CRYSTAL, amount(random, 16, 24) + levelBonus));
                rewards.add(new ItemStack(Items.EXPERIENCE_BOTTLE, amount(random, 36, 52) + levelBonus * 2));
                rewards.add(new ItemStack(Items.GOLDEN_APPLE, amount(random, 1, 2)));
                rewards.add(new ItemStack(Items.NETHERITE_SCRAP, 1));
                addChance(rewards, random, 0.25F, new ItemStack(Items.NETHERITE_SCRAP, 1));
                addInfusedChance(rewards, random, 0.18F, true);
                addChance(rewards, random, 0.05F, new ItemStack(Items.ENCHANTED_GOLDEN_APPLE, 1));
            }
            default -> {
                rewards.add(new ItemStack(Items.DIAMOND, amount(random, 3, 5) + levelBonus / 3));
                rewards.add(new ItemStack(ModItems.SOLAR_CRYSTAL, amount(random, 5, 8) + levelBonus / 2));
                rewards.add(new ItemStack(Items.EXPERIENCE_BOTTLE, amount(random, 14, 22) + levelBonus));
                rewards.add(new ItemStack(random != null && random.nextBoolean() ? Items.GOLD_INGOT : Items.IRON_INGOT, amount(random, 8, 16) + levelBonus));
                addChance(rewards, random, 0.10F, new ItemStack(Items.GOLDEN_APPLE, 1));
            }
        }
        return rewards;
    }

    static int amount(net.minecraft.util.math.random.Random random, int min, int max) {
        if (random == null) {
            return (min + max) / 2;
        }
        return random.nextBetween(min, max);
    }

    static void addChance(List<ItemStack> rewards, net.minecraft.util.math.random.Random random, float chance, ItemStack stack) {
        if (random != null && random.nextFloat() < chance) {
            rewards.add(stack);
        }
    }

    static void addInfusedChance(List<ItemStack> rewards, net.minecraft.util.math.random.Random random, float chance, boolean goodPool) {
        if (random == null || random.nextFloat() >= chance) {
            return;
        }

        Item item = pickInfusedItem(random, goodPool);
        ItemStack stack = SolarInfusionApi.createInfusedCopy(new ItemStack(item));
        if (stack.isDamageable() && random != null) {
            stack.setDamage(random.nextBetween(stack.getMaxDamage() / 4, stack.getMaxDamage() * 3 / 4));
        }
        rewards.add(stack);
    }

    private static Item pickInfusedItem(net.minecraft.util.math.random.Random random, boolean goodPool) {
        Item[] simple = new Item[] {
            Items.WOODEN_SWORD,
            Items.STONE_SWORD,
            Items.GOLDEN_SWORD,
            Items.IRON_SWORD,
            Items.BOW,
            Items.ARROW,
            Items.SPECTRAL_ARROW,
            Items.WIND_CHARGE
        };
        Item[] good = new Item[] {
            Items.IRON_SWORD,
            Items.DIAMOND_SWORD,
            ModItems.BRONZE_SWORD,
            Items.BOW,
            Items.TRIDENT,
            Items.SHIELD,
            ModItems.CRYSTAL_DUST,
            Items.ENDER_PEARL
        };
        Item[] pool = goodPool ? good : simple;
        int index = random == null ? 0 : random.nextInt(pool.length);
        return pool[index];
    }

    public static Text progressText(EventAltarSavedData.QuestState quest) {
        String key = switch (quest.type()) {
            case TYPE_KILL_HOSTILE, TYPE_KILL_UNDEAD -> "event_altar.caveborn.progress.killed";
            case TYPE_BREAK_STONE, TYPE_BREAK_DEEPSLATE, TYPE_BREAK_AMETHYST -> "event_altar.caveborn.progress.broken";
            case TYPE_USE_SOLAR_ITEM -> "event_altar.caveborn.progress.used";
            default -> "event_altar.caveborn.progress";
        };
        return Text.translatable(key, quest.progress(), quest.target());
    }

    public static Item rewardPreviewItem(EventAltarSavedData.QuestState quest) {
        return switch (quest.rarity()) {
            case RARITY_HARD -> Items.DIAMOND;
            case RARITY_HARDCORE -> Items.NETHERITE_SCRAP;
            default -> Items.EMERALD;
        };
    }

    public static int rewardPreviewCount(EventAltarSavedData.QuestState quest, int altarLevel) {
        return rewards(quest, altarLevel).get(0).getCount();
    }

    public static Text title(EventAltarSavedData.QuestState quest) {
        return Text.translatable("event_altar.caveborn.quest." + quest.type() + ".title");
    }

    public static Text description(EventAltarSavedData.QuestState quest) {
        return Text.translatable("event_altar.caveborn.quest." + quest.type() + ".description", quest.target());
    }

    public static Text rarityName(int rarity) {
        return Text.translatable("event_altar.caveborn.rarity." + rarity);
    }
}
