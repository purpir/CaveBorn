package ru.purpir.eventaltar;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import ru.purpir.block.ModBlocks;
import ru.purpir.item.ModItems;

import java.util.ArrayList;
import java.util.List;

public final class EventAltarTasks {
    private static final Task[] TASKS = new Task[] {
        new Task(Items.GOLD_INGOT, 24,
            new Reward(Items.GOLDEN_APPLE, 5),
            new Reward(Items.EXPERIENCE_BOTTLE, 16)),
        new Task(ModItems.BRONZE_INGOT, 32,
            new Reward(Items.DIAMOND, 4),
            new Reward(Items.EXPERIENCE_BOTTLE, 18)),
        new Task(ModBlocks.BRONZE_BLOCK.asItem(), 4,
            new Reward(Items.DIAMOND, 8),
            new Reward(ModItems.SOLAR_CRYSTAL, 6)),
        new Task(ModItems.LIMESTONE_DUST, 48,
            new Reward(Items.EMERALD, 14),
            new Reward(Items.EXPERIENCE_BOTTLE, 12)),
        new Task(ModItems.CRYSTAL_DUST, 20,
            new Reward(ModItems.SOLAR_CRYSTAL, 8),
            new Reward(Items.AMETHYST_SHARD, 16)),
        new Task(Items.FLINT, 48,
            new Reward(Items.DIAMOND, 3),
            new Reward(Items.EXPERIENCE_BOTTLE, 20)),
        new Task(Items.AMETHYST_SHARD, 32,
            new Reward(ModItems.SOLAR_CRYSTAL, 8),
            new Reward(Items.EXPERIENCE_BOTTLE, 10)),
        new Task(Items.GOLD_BLOCK, 6,
            new Reward(Items.ENCHANTED_GOLDEN_APPLE, 1),
            new Reward(Items.GOLDEN_APPLE, 6)),
        new Task(Items.DIAMOND, 8,
            new Reward(Items.NETHERITE_SCRAP, 3),
            new Reward(Items.GOLDEN_APPLE, 4),
            new Reward(Items.EXPERIENCE_BOTTLE, 16)),
        new Task(ModBlocks.DEEP_GRANITE.asItem(), 64,
            new Reward(Items.DIAMOND, 7),
            new Reward(Items.EMERALD, 10)),
        new Task(ModBlocks.ASHEN_LIMESTONE.asItem(), 64,
            new Reward(ModItems.LIMESTONE_DUST, 64),
            new Reward(Items.EMERALD, 10)),
        new Task(ModItems.TITANIUM_INGOT, 4,
            new Reward(Items.NETHERITE_SCRAP, 4),
            new Reward(Items.DIAMOND, 6),
            new Reward(ModItems.SOLAR_CRYSTAL, 8)),
        new Task(ModItems.VACUUMITE_INGOT, 6,
            new Reward(ModBlocks.VACUUMITE_BLOCK.asItem(), 1),
            new Reward(ModItems.SOLAR_CRYSTAL, 8),
            new Reward(Items.EXPERIENCE_BOTTLE, 24)),
        new Task(ModBlocks.VACUUMITE_BLOCK.asItem(), 1,
            new Reward(Items.NETHERITE_INGOT, 2),
            new Reward(Items.ENCHANTED_GOLDEN_APPLE, 1),
            new Reward(ModItems.SOLAR_CRYSTAL, 12)),
        new Task(ModItems.NETHERITE_TITANIUM_INGOT, 1,
            new Reward(Items.NETHERITE_INGOT, 3),
            new Reward(Items.ENCHANTED_GOLDEN_APPLE, 2),
            new Reward(ModItems.SOLAR_CRYSTAL, 16)),
        new Task(Items.NETHER_STAR, 1,
            new Reward(Items.NETHERITE_INGOT, 4),
            new Reward(Items.ENCHANTED_GOLDEN_APPLE, 3),
            new Reward(ModItems.SOLAR_CRYSTAL, 24)),
        new Task(ModItems.SOLAR_CRYSTAL, 8,
            new Reward(Items.DIAMOND, 2),
            new Reward(Items.GOLDEN_APPLE, 4),
            new Reward(Items.EXPERIENCE_BOTTLE, 12)),
        new Task(ModBlocks.NETHERITE_TITANIUM_BLOCK.asItem(), 1,
            new Reward(Items.NETHERITE_INGOT, 8),
            new Reward(Items.ENCHANTED_GOLDEN_APPLE, 6),
            new Reward(ModItems.SOLAR_CRYSTAL, 32))
    };

    private EventAltarTasks() {
    }

    public static Task get(BlockPos origin, int level) {
        int offset = Math.floorMod(origin.getX() * 73428767 ^ origin.getY() * 912931 ^ origin.getZ() * 42317861, TASKS.length);
        Task base = TASKS[Math.floorMod(offset + level * 5, TASKS.length)];
        int cycle = Math.max(0, level / TASKS.length);
        return cycle == 0 ? base : base.scaled(cycle);
    }

    public static int count() {
        return TASKS.length;
    }

    public record Reward(Item item, int count) {
        private Reward scaled(int cycle) {
            return new Reward(item, Math.min(64, count + Math.max(1, count / 3) * cycle));
        }

        public ItemStack stack() {
            return new ItemStack(item, count);
        }

        public Text text() {
            return Text.literal(count + "x ").append(item.getName());
        }
    }

    public record Task(Item requiredItem, int requiredCount, Reward... rewards) {
        private Task scaled(int cycle) {
            Reward[] scaledRewards = new Reward[rewards.length];
            for (int i = 0; i < rewards.length; i++) {
                scaledRewards[i] = rewards[i].scaled(cycle);
            }
            return new Task(requiredItem, Math.min(999, requiredCount + Math.max(1, requiredCount / 2) * cycle), scaledRewards);
        }

        public Text requiredText() {
            return Text.literal(requiredCount + "x ").append(requiredItem.getName());
        }

        public Text rewardText() {
            MutableText text = Text.empty();
            for (int i = 0; i < rewards.length; i++) {
                if (i > 0) {
                    text.append(Text.literal(", "));
                }
                text.append(rewards[i].text());
            }
            return text;
        }

        public ItemStack primaryRewardStack() {
            return rewards.length == 0 ? ItemStack.EMPTY : rewards[0].stack();
        }

        public List<ItemStack> rewardStacks() {
            List<ItemStack> stacks = new ArrayList<>();
            for (Reward reward : rewards) {
                stacks.add(reward.stack());
            }
            return stacks;
        }
    }
}
