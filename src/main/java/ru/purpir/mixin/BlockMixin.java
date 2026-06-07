package ru.purpir.mixin;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.purpir.block.ModBlocks;
import ru.purpir.multiblock.MultiblockManager;
import ru.purpir.multiblock.MultiblockStructure;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Mixin(Block.class)
public class BlockMixin {
    @Unique
    private static final ThreadLocal<Boolean> isMultiblockBreaking = ThreadLocal.withInitial(() -> false);
    @Unique
    private static final Map<BlockPos, Long> caveborn$suppressedDropPositions = new ConcurrentHashMap<>();

    @Inject(method = "onBreak", at = @At("HEAD"), cancellable = true)
    private void onBlockBreak(World world, BlockPos pos, BlockState state, PlayerEntity player,
                              CallbackInfoReturnable<BlockState> cir) {
        if (world.isClient()) {
            return;
        }

        MultiblockManager manager = MultiblockManager.getInstance();
        if (!manager.isPartOfStructure(pos) || isMultiblockBreaking.get()) {
            return;
        }

        isMultiblockBreaking.set(true);
        try {
            BlockPos origin = manager.getOriginPos(pos);
            if (origin == null) {
                return;
            }

            MultiblockStructure structure = manager.getStructureByOrigin(origin);
            if (structure == null) {
                return;
            }

            boolean eventAltar = ru.purpir.eventaltar.EventAltarHandler.isEventAltarOrigin(world, origin);
            if (eventAltar) {
                caveborn$suppressStructureDrops(world, structure);
            }

            for (BlockPos relativePos : structure.getBlocks().keySet()) {
                world.removeBlock(structure.getAbsolutePos(relativePos), false);
            }

            manager.unregisterStructure(origin);

            if (!player.isCreative()) {
                ItemStack itemStack = caveborn$getMultiblockDrop(world, origin, state);
                if (!itemStack.isEmpty()) {
                    ItemEntity itemEntity = new ItemEntity(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, itemStack);
                    world.spawnEntity(itemEntity);
                }
            }

            cir.setReturnValue(state);
        } finally {
            isMultiblockBreaking.set(false);
        }
    }

    @Unique
    private void caveborn$suppressStructureDrops(World world, MultiblockStructure structure) {
        long time = world.getTime();
        for (BlockPos relativePos : structure.getBlocks().keySet()) {
            caveborn$suppressedDropPositions.put(structure.getAbsolutePos(relativePos), time);
        }
    }

    @Unique
    private ItemStack caveborn$getMultiblockDrop(World world, BlockPos origin, BlockState state) {
        if (state.isOf(ModBlocks.CRUSHER)) {
            return new ItemStack(ModBlocks.CRUSHER);
        }

        if (ru.purpir.eventaltar.EventAltarHandler.isEventAltarOrigin(world, origin)) {
            return ItemStack.EMPTY;
        }

        return new ItemStack(ModBlocks.EXAMPLE_MULTIBLOCK);
    }

    @Inject(method = "dropStacks(Lnet/minecraft/block/BlockState;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/entity/BlockEntity;Lnet/minecraft/entity/Entity;Lnet/minecraft/item/ItemStack;)V", at = @At("HEAD"), cancellable = true)
    private static void cancelDropStacks(BlockState state, World world, BlockPos pos, BlockEntity blockEntity, Entity entity, ItemStack tool, CallbackInfo ci) {
        if (isMultiblockBreaking.get()) {
            ci.cancel();
            return;
        }

        Long suppressedAt = caveborn$suppressedDropPositions.remove(pos);
        if (suppressedAt != null && world.getTime() - suppressedAt <= 5) {
            ci.cancel();
        }
    }
}
