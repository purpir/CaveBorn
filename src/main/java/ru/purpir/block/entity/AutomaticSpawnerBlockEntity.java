package ru.purpir.block.entity;

import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.Nullable;
import ru.purpir.screen.AutoSpawnerScreenHandler;

public class AutomaticSpawnerBlockEntity extends BlockEntity implements SidedInventory, ExtendedScreenHandlerFactory<BlockPos> {
    private static final int[] HOPPER_SLOTS = java.util.stream.IntStream.range(1, AutoSpawnerSavedData.INVENTORY_SIZE + 1).toArray();
    private final AutoSpawnerSavedData.SpawnerData clientData = new AutoSpawnerSavedData.SpawnerData();
    public AutomaticSpawnerBlockEntity(BlockPos pos, BlockState state) { super(ModBlockEntities.AUTOMATIC_SPAWNER, pos, state); }
    public AutoSpawnerSavedData.SpawnerData data() {
        return world instanceof net.minecraft.server.world.ServerWorld sw ? AutoSpawnerSavedData.get(sw).getOrCreate(pos) : clientData;
    }
    public void unregister() { if (world instanceof net.minecraft.server.world.ServerWorld sw) AutoSpawnerSavedData.get(sw).remove(pos); }
    @Override public BlockPos getScreenOpeningData(ServerPlayerEntity player) { return pos; }
    @Override public Text getDisplayName() { return Text.translatable("block.caveborn.automatic_spawner"); }
    @Override @Nullable public ScreenHandler createMenu(int syncId, PlayerInventory inv, PlayerEntity player) {
        return new AutoSpawnerScreenHandler(syncId, inv, pos);
    }
    @Override public int size() { return AutoSpawnerSavedData.INVENTORY_SIZE + 1; }
    @Override public boolean isEmpty() { return data().isEmpty(); }
    @Override public ItemStack getStack(int slot) { return data().getStack(slot); }
    @Override public ItemStack removeStack(int slot, int amount) { ItemStack s=data().removeStack(slot, amount); markDirty(); return s; }
    @Override public ItemStack removeStack(int slot) { ItemStack s=data().removeStack(slot); markDirty(); return s; }
    @Override public void setStack(int slot, ItemStack stack) {
        data().setStack(slot, stack);
        markDirty();
        if (world != null && !world.isClient()) {
            world.updateListeners(pos, getCachedState(), getCachedState(), Block.NOTIFY_LISTENERS);
        }
    }
    @Override public boolean canPlayerUse(PlayerEntity player) { return Inventory.canPlayerUse(this, player); }
    @Override public boolean isValid(int slot, ItemStack stack) { return slot == 0 && data().isValidEgg(stack); }
    @Override public int[] getAvailableSlots(Direction side) { return HOPPER_SLOTS; }
    @Override public boolean canInsert(int slot, ItemStack stack, Direction side) { return false; }
    @Override public boolean canExtract(int slot, ItemStack stack, Direction side) { return slot != 0; }
    @Override protected void readData(ReadView view) {
        super.readData(view);
        view.read("egg", ItemStack.OPTIONAL_CODEC).ifPresent(stack -> clientData.setStack(0, stack));
    }
    @Override protected void writeData(WriteView view) {
        super.writeData(view);
        view.put("egg", ItemStack.OPTIONAL_CODEC, data().getStack(0));
    }
    @Override public net.minecraft.nbt.NbtCompound toInitialChunkDataNbt(net.minecraft.registry.RegistryWrapper.WrapperLookup registries) {
        return createNbt(registries);
    }
    @Override public void clear() { data().clear(); markDirty(); }
    @Override public void markDirty() { super.markDirty(); if (world instanceof net.minecraft.server.world.ServerWorld sw) AutoSpawnerSavedData.get(sw).markDirty(); }
}
