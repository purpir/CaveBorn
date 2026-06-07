package ru.purpir.eventaltar;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
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
            case RARITY_HARD -> 45;
            case RARITY_HARDCORE -> 85;
            default -> 20;
        };
    }

    public static List<ItemStack> rewards(EventAltarSavedData.QuestState quest, int altarLevel) {
        int levelBonus = Math.max(0, altarLevel - 1);
        List<ItemStack> rewards = new ArrayList<>();
        switch (quest.rarity()) {
            case RARITY_HARD -> {
                rewards.add(new ItemStack(Items.DIAMOND, 3 + levelBonus));
                rewards.add(new ItemStack(ModItems.SOLAR_CRYSTAL, 6 + levelBonus));
                rewards.add(new ItemStack(Items.EXPERIENCE_BOTTLE, 18 + levelBonus * 2));
            }
            case RARITY_HARDCORE -> {
                rewards.add(new ItemStack(Items.NETHERITE_SCRAP, 2 + levelBonus / 2));
                rewards.add(new ItemStack(Items.ENCHANTED_GOLDEN_APPLE, 1 + levelBonus / 4));
                rewards.add(new ItemStack(ModItems.SOLAR_CRYSTAL, 12 + levelBonus * 2));
            }
            default -> {
                rewards.add(new ItemStack(Items.EMERALD, 6 + levelBonus));
                rewards.add(new ItemStack(ModItems.SOLAR_CRYSTAL, 3 + levelBonus / 2));
                rewards.add(new ItemStack(Items.EXPERIENCE_BOTTLE, 8 + levelBonus));
            }
        }
        return rewards;
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
