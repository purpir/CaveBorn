package ru.purpir.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CropBlock;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.purpir.block.ModBlocks;

@Mixin(CropBlock.class)
public class CropBlockMixin {
    
    @Unique
    private static final int WEED_SPREAD_CHANCE = 300; // 1/300 шанс проверить распространение сорняка
    
    @Inject(method = "randomTick", at = @At("HEAD"))
    private void onCropRandomTick(BlockState state, ServerWorld world, BlockPos pos, Random random, CallbackInfo ci) {
        // Культуры не спавнят сорняки сами, это делает FarmlandBlockMixin
        // Но если рядом есть сорняк - он может распространиться
        if (random.nextInt(WEED_SPREAD_CHANCE) == 0) {
            checkForNearbyWeedsAndSpread(world, pos, random);
        }
    }
    
    @Unique
    private void checkForNearbyWeedsAndSpread(ServerWorld world, BlockPos cropPos, Random random) {
        // Проверяем есть ли сорняки рядом
        for (Direction dir : Direction.Type.HORIZONTAL) {
            BlockPos neighborPos = cropPos.offset(dir);
            BlockState neighborState = world.getBlockState(neighborPos);
            
            // Если рядом сорняк - он может заразить эту культуру
            if (neighborState.isOf(ModBlocks.WEED)) {
                if (random.nextInt(5) == 0) { // 1/5 шанс
                    world.setBlockState(cropPos, ModBlocks.WEED.getDefaultState());
                }
                break;
            }
        }
    }
}
