package ru.purpir.block.entity;

import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import ru.purpir.block.CrusherBlock;
import ru.purpir.block.CrusherPart;
import ru.purpir.block.ModBlocks;
import ru.purpir.item.ModItems;
import ru.purpir.screen.CrusherScreenHandler;

public class CrusherBlockEntity extends BlockEntity implements Inventory, ExtendedScreenHandlerFactory<BlockPos> {
    public static final int INPUT_START = 0;
    public static final int INPUT_END = 5;
    public static final int OUTPUT_START = 5;
    public static final int OUTPUT_END = 10;
    public static final int SLOT_COUNT = 10;
    public static final int DEFAULT_PROCESS_TIME = 200;

    private final DefaultedList<ItemStack> stacks = DefaultedList.ofSize(SLOT_COUNT, ItemStack.EMPTY);
    private int progress;
    private int maxProgress = DEFAULT_PROCESS_TIME;
    private int currentInputSlot = -1;
    @Nullable
    private Item currentInputItem;

    public final PropertyDelegate propertyDelegate = new PropertyDelegate() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> CrusherBlockEntity.this.progress;
                case 1 -> CrusherBlockEntity.this.maxProgress;
                case 2 -> CrusherBlockEntity.this.isPowered() ? 1 : 0;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0 -> CrusherBlockEntity.this.progress = value;
                case 1 -> CrusherBlockEntity.this.maxProgress = value;
                default -> {
                }
            }
        }

        @Override
        public int size() {
            return 3;
        }
    };

    public CrusherBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.CRUSHER, pos, state);
    }

    public static void tick(World world, BlockPos pos, BlockState state, CrusherBlockEntity blockEntity) {
        if (world.isClient()) {
            return;
        }

        if (!blockEntity.isPowered()) {
            return;
        }

        CrushingRecipe recipe = blockEntity.getActiveRecipe();
        if (recipe == null) {
            blockEntity.resetProgress();
            return;
        }

        if (!blockEntity.canFitOutput(recipe.output(), recipe.maxCount())) {
            return;
        }

        blockEntity.maxProgress = recipe.time();
        blockEntity.progress++;

        if (blockEntity.progress >= blockEntity.maxProgress) {
            int outputCount = recipe.rollCount(world.random);
            if (outputCount > 0) {
                blockEntity.insertOutput(new ItemStack(recipe.output(), outputCount));
            }

            blockEntity.removeStack(blockEntity.currentInputSlot, 1);
            blockEntity.resetProgress();
        }

        blockEntity.markDirty();
    }

    @Nullable
    private CrushingRecipe getActiveRecipe() {
        if (currentInputSlot >= INPUT_START && currentInputSlot < INPUT_END) {
            ItemStack stack = stacks.get(currentInputSlot);
            CrushingRecipe recipe = CrushingRecipe.of(stack);
            if (recipe != null && (currentInputItem == null || stack.isOf(currentInputItem))) {
                currentInputItem = recipe.input();
                return recipe;
            }

            currentInputSlot = -1;
            currentInputItem = null;
            resetProgress();
        }

        for (int slot = INPUT_START; slot < INPUT_END; slot++) {
            CrushingRecipe recipe = CrushingRecipe.of(stacks.get(slot));
            if (recipe != null) {
                if (currentInputSlot != slot || currentInputItem != recipe.input()) {
                    resetProgress();
                }
                currentInputSlot = slot;
                currentInputItem = recipe.input();
                return recipe;
            }
        }

        currentInputSlot = -1;
        currentInputItem = null;
        return null;
    }

    private boolean canFitOutput(net.minecraft.item.Item output, int count) {
        int remaining = count;
        for (int slot = OUTPUT_START; slot < OUTPUT_END; slot++) {
            ItemStack stack = stacks.get(slot);
            if (stack.isEmpty()) {
                remaining -= output.getMaxCount();
            } else if (stack.isOf(output)) {
                remaining -= stack.getMaxCount() - stack.getCount();
            }

            if (remaining <= 0) {
                return true;
            }
        }

        return false;
    }

    private void insertOutput(ItemStack output) {
        for (int slot = OUTPUT_START; slot < OUTPUT_END && !output.isEmpty(); slot++) {
            ItemStack stack = stacks.get(slot);
            if (!stack.isEmpty() && ItemStack.areItemsAndComponentsEqual(stack, output)) {
                int moved = Math.min(output.getCount(), stack.getMaxCount() - stack.getCount());
                stack.increment(moved);
                output.decrement(moved);
            }
        }

        for (int slot = OUTPUT_START; slot < OUTPUT_END && !output.isEmpty(); slot++) {
            if (stacks.get(slot).isEmpty()) {
                stacks.set(slot, output.copy());
                output.setCount(0);
            }
        }
    }

    private void resetProgress() {
        progress = 0;
        maxProgress = DEFAULT_PROCESS_TIME;
    }

    public boolean isPowered() {
        if (world == null) {
            return false;
        }

        BlockState state = getCachedState();
        if (!state.contains(CrusherBlock.FACING)) {
            return world.isReceivingRedstonePower(pos);
        }

        for (CrusherPart part : CrusherPart.values()) {
            if (world.isReceivingRedstonePower(CrusherBlock.getPartPos(pos, state.get(CrusherBlock.FACING), part))) {
                return true;
            }
        }

        return false;
    }

    @Override
    protected void readData(ReadView view) {
        super.readData(view);
        Inventories.readData(view, stacks);
        progress = view.getInt("progress", 0);
        maxProgress = view.getInt("max_progress", DEFAULT_PROCESS_TIME);
        currentInputSlot = view.getInt("current_input_slot", -1);
    }

    @Override
    protected void writeData(WriteView view) {
        super.writeData(view);
        Inventories.writeData(view, stacks);
        view.putInt("progress", progress);
        view.putInt("max_progress", maxProgress);
        view.putInt("current_input_slot", currentInputSlot);
    }

    @Override
    public BlockPos getScreenOpeningData(net.minecraft.server.network.ServerPlayerEntity player) {
        return pos;
    }

    @Override
    public Text getDisplayName() {
        return Text.translatable("block.caveborn.crusher");
    }

    @Override
    public @Nullable ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
        return new CrusherScreenHandler(syncId, playerInventory, this, propertyDelegate);
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
    public boolean isValid(int slot, ItemStack stack) {
        return slot >= INPUT_START && slot < INPUT_END;
    }

    @Override
    public void clear() {
        stacks.clear();
    }

    private record CrushingRecipe(net.minecraft.item.Item input, net.minecraft.item.Item output, int minCount, int maxCount, int time) {
        @Nullable
        static CrushingRecipe of(ItemStack stack) {
            if (stack.isOf(ModBlocks.ASHEN_LIMESTONE.asItem())) {
                return new CrushingRecipe(ModBlocks.ASHEN_LIMESTONE.asItem(), ModItems.LIMESTONE_DUST, 4, 6, DEFAULT_PROCESS_TIME);
            }
            if (stack.isOf(Items.GRAVEL)) {
                return new CrushingRecipe(Items.GRAVEL, Items.FLINT, 1, 3, DEFAULT_PROCESS_TIME);
            }
            if (stack.isOf(Items.SAND)) {
                return new CrushingRecipe(Items.SAND, Items.FLINT, 0, 1, DEFAULT_PROCESS_TIME);
            }
            return null;
        }

        int rollCount(net.minecraft.util.math.random.Random random) {
            return minCount + random.nextInt(maxCount - minCount + 1);
        }
    }
}
