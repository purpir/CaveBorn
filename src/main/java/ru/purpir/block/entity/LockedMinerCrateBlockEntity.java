package ru.purpir.block.entity;

import net.minecraft.block.BlockState;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import org.jetbrains.annotations.Nullable;
import ru.purpir.component.ModComponents;
import ru.purpir.item.ModItems;

public class LockedMinerCrateBlockEntity extends net.minecraft.block.entity.BlockEntity implements Inventory, net.minecraft.screen.NamedScreenHandlerFactory {
    private static final int SIZE = 27;
    private final DefaultedList<ItemStack> stacks = DefaultedList.ofSize(SIZE, ItemStack.EMPTY);
    private boolean generated;

    public LockedMinerCrateBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.LOCKED_MINER_CRATE, pos, state);
    }

    public void fillRewardInventory() {
        if (generated || world == null || world.isClient()) {
            return;
        }

        Random random = world.random;
        clear();
        addRandom(new ItemStack(Items.DIAMOND, random.nextBetween(3, 8)), random);
        addRandom(new ItemStack(Items.GOLD_INGOT, random.nextBetween(10, 24)), random);
        addRandom(new ItemStack(Items.GOLDEN_APPLE, random.nextBetween(1, 3)), random);
        addRandom(new ItemStack(ModItems.SOLAR_CRYSTAL, random.nextBetween(1, 3)), random);

        if (random.nextFloat() < 0.45F) {
            addRandom(new ItemStack(Items.ENCHANTED_GOLDEN_APPLE), random);
        }
        int infusedRolls = 1 + random.nextInt(3);
        for (int i = 0; i < infusedRolls; i++) {
            addRandom(createSolarReward(random), random);
        }

        generated = true;
        markDirty();
    }

    private ItemStack createSolarReward(Random random) {
        Item item = switch (random.nextInt(15)) {
            case 0 -> Items.DIAMOND_SWORD;
            case 1 -> Items.NETHERITE_SWORD;
            case 2 -> Items.IRON_SWORD;
            case 3 -> Items.GOLDEN_SWORD;
            case 4 -> Items.BOW;
            case 5 -> Items.TRIDENT;
            case 6 -> Items.TOTEM_OF_UNDYING;
            case 7 -> ModItems.BRONZE_SWORD;
            case 8 -> ModItems.VACUUMITE_SWORD;
            case 9 -> Items.SHIELD;
            case 10 -> Items.ARROW;
            case 11 -> Items.SPECTRAL_ARROW;
            case 12 -> Items.WIND_CHARGE;
            case 13 -> ModItems.CRYSTAL_DUST;
            default -> Items.ENDER_PEARL;
        };

        ItemStack stack = new ItemStack(item);
        stack.set(ModComponents.SOLAR_INFUSED, true);
        stack.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        if (stack.isDamageable() && random.nextFloat() < 0.7F) {
            int min = stack.getMaxDamage() / 4;
            int max = Math.max(min, stack.getMaxDamage() - 8);
            stack.setDamage(random.nextBetween(min, max));
        }
        return stack;
    }

    private void addRandom(ItemStack stack, Random random) {
        for (int attempt = 0; attempt < 40; attempt++) {
            int slot = random.nextInt(size());
            if (getStack(slot).isEmpty()) {
                setStack(slot, stack);
                return;
            }
        }
    }

    @Override
    protected void readData(ReadView view) {
        super.readData(view);
        Inventories.readData(view, stacks);
        generated = view.getBoolean("generated", false);
    }

    @Override
    protected void writeData(WriteView view) {
        super.writeData(view);
        Inventories.writeData(view, stacks);
        view.putBoolean("generated", generated);
    }

    @Override
    public Text getDisplayName() {
        return Text.translatable("block.caveborn.locked_miner_crate");
    }

    @Override
    public @Nullable ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
        return GenericContainerScreenHandler.createGeneric9x3(syncId, playerInventory, this);
    }

    @Override
    public int size() {
        return stacks.size();
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack stack : stacks) {
            if (!stack.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public ItemStack getStack(int slot) {
        return stacks.get(slot);
    }

    @Override
    public ItemStack removeStack(int slot, int amount) {
        ItemStack result = Inventories.splitStack(stacks, slot, amount);
        if (!result.isEmpty()) {
            markDirty();
        }
        return result;
    }

    @Override
    public ItemStack removeStack(int slot) {
        ItemStack result = Inventories.removeStack(stacks, slot);
        markDirty();
        return result;
    }

    @Override
    public void setStack(int slot, ItemStack stack) {
        stacks.set(slot, stack);
        stack.capCount(getMaxCount(stack));
        markDirty();
    }

    @Override
    public boolean canPlayerUse(PlayerEntity player) {
        return Inventory.canPlayerUse(this, player);
    }

    @Override
    public void clear() {
        stacks.clear();
        markDirty();
    }
}
