package ru.purpir.screen;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ContainerComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.collection.DefaultedList;
import ru.purpir.item.ModItems;

public class BagScreenHandler extends ScreenHandler {
    private final SimpleInventory inventory;
    private final ItemStack bagStack;
    private final int bagSlotIndex;
    public static final int BAG_SLOTS = 18;

    public BagScreenHandler(int syncId, PlayerInventory playerInventory, ItemStack bagStack) {
        super(ModScreenHandlers.BAG_SCREEN_HANDLER, syncId);
        this.bagStack = bagStack;
        this.bagSlotIndex = playerInventory.getSelectedSlot();
        this.inventory = new SimpleInventory(BAG_SLOTS) {
            @Override
            public void markDirty() {
                super.markDirty();
                saveToContainer();
            }
        };
        
        loadFromContainer();
        
        for (int row = 0; row < 2; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new BagSlot(inventory, col + row * 9, 8 + col * 18, 18 + row * 18));
            }
        }

        // Player inventory
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 66 + row * 18));
            }
        }

        // Player hotbar
        for (int col = 0; col < 9; col++) {
            final int slotIndex = col;
            this.addSlot(new Slot(playerInventory, col, 8 + col * 18, 124) {
                @Override
                public boolean canTakeItems(PlayerEntity player) {
                    return slotIndex != bagSlotIndex;
                }
                
                @Override
                public boolean canInsert(ItemStack stack) {
                    return slotIndex != bagSlotIndex;
                }
            });
        }
    }

    private void loadFromContainer() {
        ContainerComponent container = bagStack.getOrDefault(DataComponentTypes.CONTAINER, ContainerComponent.DEFAULT);
        container.copyTo(inventory.getHeldStacks());
    }

    private void saveToContainer() {
        bagStack.set(DataComponentTypes.CONTAINER, ContainerComponent.fromStacks(inventory.getHeldStacks()));
    }

    @Override
    public void onClosed(PlayerEntity player) {
        super.onClosed(player);
        saveToContainer();
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int slotIndex) {
        ItemStack newStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(slotIndex);
        
        if (slot.hasStack()) {
            ItemStack originalStack = slot.getStack();
            newStack = originalStack.copy();
            
            if (slotIndex < BAG_SLOTS) {
                if (!this.insertItem(originalStack, BAG_SLOTS, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                if (!this.insertItem(originalStack, 0, BAG_SLOTS, false)) {
                    return ItemStack.EMPTY;
                }
            }
            
            if (originalStack.isEmpty()) {
                slot.setStack(ItemStack.EMPTY);
            } else {
                slot.markDirty();
            }
        }
        
        return newStack;
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return true;
    }

    private class BagSlot extends Slot {
        public BagSlot(SimpleInventory inventory, int index, int x, int y) {
            super(inventory, index, x, y);
        }

        @Override
        public boolean canInsert(ItemStack stack) {
            return !stack.isOf(ModItems.BAG);
        }
    }
}
