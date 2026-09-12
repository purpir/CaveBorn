package ru.purpir.screen;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.math.BlockPos;
import ru.purpir.block.entity.AutomaticSpawnerBlockEntity;
import ru.purpir.block.entity.AutoSpawnerSavedData;

public class AutoSpawnerScreenHandler extends ScreenHandler {
    private final BlockPos pos;
    private final AutomaticSpawnerBlockEntity spawner;
    private final AutoSpawnerSavedData.SpawnerData data;
    private int syncedExperience;
    private final PropertyDelegate properties = new PropertyDelegate() {
        public int get(int index) {
            if (spawner != null && spawner.getWorld() != null && !spawner.getWorld().isClient()) {
                return (int) Math.min(Integer.MAX_VALUE, data.experience());
            }
            return syncedExperience;
        }
        public void set(int index, int value) { syncedExperience = Math.max(0, value); }
        public int size() { return 1; }
    };
    public AutoSpawnerScreenHandler(int syncId, PlayerInventory inv, BlockPos pos) {
        super(ModScreenHandlers.AUTO_SPAWNER_SCREEN_HANDLER, syncId);
        this.pos = pos;
        this.spawner = inv.player.getEntityWorld().getBlockEntity(pos) instanceof AutomaticSpawnerBlockEntity s ? s : null;
        this.data = spawner != null ? spawner.data() : new AutoSpawnerSavedData.SpawnerData();
        addProperties(properties);
        addSlot(new Slot(spawner == null ? new SimpleInventory(1) : spawner, 0, 44, 39) {
            @Override public boolean canInsert(ItemStack stack) { return data.isValidEgg(stack); }
            @Override public int getMaxItemCount() { return 1; }
        });
        addSlot(new Slot(new SimpleInventory(1), 0, 108, 39) {
            @Override public boolean canInsert(ItemStack stack) { return false; }
        });
        for (int r=0;r<3;r++) for (int c=0;c<9;c++) addSlot(new Slot(inv,c+r*9+9,8+c*18,128+r*18));
        for (int c=0;c<9;c++) addSlot(new Slot(inv,c,8+c*18,182));
    }
    @Override public boolean onButtonClick(PlayerEntity player, int id) {
        if (!(player instanceof net.minecraft.server.network.ServerPlayerEntity server) || spawner == null) return false;
        if (id == 0) {
            server.openHandledScreen(new net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory<BlockPos>() {
                public net.minecraft.text.Text getDisplayName() { return spawner.getDisplayName(); }
                public ScreenHandler createMenu(int sync, PlayerInventory inv, PlayerEntity p) { return new AutoSpawnerStorageScreenHandler(sync, inv, pos); }
                public BlockPos getScreenOpeningData(net.minecraft.server.network.ServerPlayerEntity player) { return pos; }
            });
            return true;
        }
        return false;
    }
    @Override public void onSlotClick(int slotIndex, int button, SlotActionType actionType, PlayerEntity player) {
        if (slotIndex == 1 && actionType == SlotActionType.PICKUP && data.experience() > 0) {
            data.collectExperience((net.minecraft.server.world.ServerWorld) spawner.getWorld(), player);
            ru.purpir.block.entity.AutoSpawnerSavedData.get((net.minecraft.server.world.ServerWorld) spawner.getWorld()).markDirty();
            sendContentUpdates();
            return;
        }
        super.onSlotClick(slotIndex, button, actionType, player);
    }
    @Override public ItemStack quickMove(PlayerEntity player, int index) {
        if (index == 0) { ItemStack copy=slots.get(0).getStack().copy(); if(!copy.isEmpty() && insertItem(slots.get(0).getStack(),2,slots.size(),true)) return copy; }
        else if (index >= 2 && insertItem(slots.get(index).getStack(),0,1,false)) return slots.get(index).getStack().copy();
        return ItemStack.EMPTY;
    }
    @Override public boolean canUse(PlayerEntity player) { return spawner != null && spawner.canPlayerUse(player); }
    public BlockPos getPos() { return pos; }
    public long getStoredExperience() { return properties.get(0); }
}
