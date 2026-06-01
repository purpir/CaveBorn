package ru.purpir.screen;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ArrayPropertyDelegate;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.math.BlockPos;
import ru.purpir.block.entity.CrusherBlockEntity;

public class CrusherScreenHandler extends ScreenHandler {
    private static final int INVENTORY_SIZE = CrusherBlockEntity.SLOT_COUNT;
    private static final int PROPERTY_COUNT = 3;
    private final Inventory inventory;
    private final PropertyDelegate properties;
    private final BlockPos pos;

    public CrusherScreenHandler(int syncId, PlayerInventory playerInventory, BlockPos pos) {
        this(syncId, playerInventory, getInventory(playerInventory, pos), getProperties(playerInventory, pos), pos);
    }

    public CrusherScreenHandler(int syncId, PlayerInventory playerInventory, Inventory inventory, PropertyDelegate properties) {
        this(syncId, playerInventory, inventory, properties, BlockPos.ORIGIN);
    }

    private CrusherScreenHandler(int syncId, PlayerInventory playerInventory, Inventory inventory, PropertyDelegate properties, BlockPos pos) {
        super(ModScreenHandlers.CRUSHER_SCREEN_HANDLER, syncId);
        ScreenHandler.checkSize(inventory, INVENTORY_SIZE);
        ScreenHandler.checkDataCount(properties, PROPERTY_COUNT);
        this.inventory = inventory;
        this.properties = properties;
        this.pos = pos;
        this.inventory.onOpen(playerInventory.player);
        this.addProperties(properties);

        for (int slot = 0; slot < 5; slot++) {
            this.addSlot(new Slot(inventory, CrusherBlockEntity.INPUT_START + slot, 44 + slot * 18, 21));
        }

        for (int slot = 0; slot < 5; slot++) {
            this.addSlot(new OutputSlot(inventory, CrusherBlockEntity.OUTPUT_START + slot, 44 + slot * 18, 77));
        }

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 111 + row * 18));
            }
        }

        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInventory, col, 8 + col * 18, 169));
        }
    }

    private static Inventory getInventory(PlayerInventory playerInventory, BlockPos pos) {
        BlockEntity blockEntity = playerInventory.player.getEntityWorld().getBlockEntity(pos);
        return blockEntity instanceof CrusherBlockEntity crusher ? crusher : new SimpleInventory(INVENTORY_SIZE);
    }

    private static PropertyDelegate getProperties(PlayerInventory playerInventory, BlockPos pos) {
        BlockEntity blockEntity = playerInventory.player.getEntityWorld().getBlockEntity(pos);
        return blockEntity instanceof CrusherBlockEntity crusher ? crusher.propertyDelegate : new ArrayPropertyDelegate(PROPERTY_COUNT);
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int slotIndex) {
        ItemStack movedStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(slotIndex);

        if (slot.hasStack()) {
            ItemStack originalStack = slot.getStack();
            movedStack = originalStack.copy();

            if (slotIndex < CrusherBlockEntity.OUTPUT_END) {
                if (!this.insertItem(originalStack, CrusherBlockEntity.OUTPUT_END, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.insertItem(originalStack, CrusherBlockEntity.INPUT_START, CrusherBlockEntity.INPUT_END, false)) {
                return ItemStack.EMPTY;
            }

            if (originalStack.isEmpty()) {
                slot.setStack(ItemStack.EMPTY);
            } else {
                slot.markDirty();
            }
        }

        return movedStack;
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return inventory.canPlayerUse(player);
    }

    @Override
    public void onClosed(PlayerEntity player) {
        super.onClosed(player);
        inventory.onClose(player);
    }

    public boolean isPowered() {
        return properties.get(2) > 0;
    }

    public int getProgressScaled(int height) {
        int progress = properties.get(0);
        int maxProgress = properties.get(1);
        return maxProgress > 0 && progress > 0 ? progress * height / maxProgress : 0;
    }

    public BlockPos getPos() {
        return pos;
    }

    private static class OutputSlot extends Slot {
        OutputSlot(Inventory inventory, int index, int x, int y) {
            super(inventory, index, x, y);
        }

        @Override
        public boolean canInsert(ItemStack stack) {
            return false;
        }
    }
}
