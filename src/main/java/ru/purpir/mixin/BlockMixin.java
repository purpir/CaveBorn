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

@Mixin(Block.class)
public class BlockMixin {
    
    @Unique
    private static final ThreadLocal<Boolean> isMultiblockBreaking = ThreadLocal.withInitial(() -> false);
    
    @Inject(method = "onBreak", at = @At("HEAD"))
    private void onBlockBreak(World world, BlockPos pos, BlockState state, PlayerEntity player, 
                              CallbackInfoReturnable<BlockState> cir) {
        if (!world.isClient()) {
            MultiblockManager manager = MultiblockManager.getInstance();
            
            // Проверяем, является ли этот блок частью мультиструктуры
            if (manager.isPartOfStructure(pos) && !isMultiblockBreaking.get()) {
                isMultiblockBreaking.set(true);
                
                BlockPos origin = manager.getOriginPos(pos);
                if (origin != null) {
                    MultiblockStructure structure = manager.getStructureByOrigin(origin);
                    if (structure != null) {
                        // Удаляем все блоки структуры (включая текущий)
                        for (BlockPos relativePos : structure.getBlocks().keySet()) {
                            BlockPos absolutePos = structure.getAbsolutePos(relativePos);
                            world.removeBlock(absolutePos, false);
                        }
                        
                        // Удаляем из менеджера ПОСЛЕ удаления блоков
                        manager.unregisterStructure(origin);
                        
                        // Спавним мультиблок
                        if (!player.isCreative()) {
                            ItemStack itemStack = new ItemStack(ModBlocks.EXAMPLE_MULTIBLOCK);
                            ItemEntity itemEntity = new ItemEntity(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, itemStack);
                            world.spawnEntity(itemEntity);
                        }
                        
                        isMultiblockBreaking.set(false);
                    }
                }
            }
        }
    }
    
    @Inject(method = "dropStacks(Lnet/minecraft/block/BlockState;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/entity/BlockEntity;Lnet/minecraft/entity/Entity;Lnet/minecraft/item/ItemStack;)V", at = @At("HEAD"), cancellable = true)
    private static void cancelDropStacks(BlockState state, World world, BlockPos pos, BlockEntity blockEntity, Entity entity, ItemStack tool, CallbackInfo ci) {
        // Отменяем дроп для всех блоков мультиструктуры
        if (isMultiblockBreaking.get()) {
            ci.cancel();
        }
    }
}
