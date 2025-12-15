package ru.purpir.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.block.CropBlock;
import net.minecraft.block.FarmlandBlock;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.purpir.block.ModBlocks;

@Mixin(FarmlandBlock.class)
public class FarmlandBlockMixin {
    
    @Unique
    private static final int WEED_SPAWN_CHANCE_EMPTY = 500; // 1/500 шанс если грядка пустая
    @Unique
    private static final int WEED_SPAWN_CHANCE_CROP = 400; // 1/400 шанс если на грядке культура
    
    @Inject(method = "randomTick", at = @At("HEAD"))
    private void onRandomTick(BlockState state, ServerWorld world, BlockPos pos, Random random, CallbackInfo ci) {
        BlockPos above = pos.up();
        BlockState aboveState = world.getBlockState(above);
        
        // Если над грядкой пусто - шанс заспавнить сорняк
        if (aboveState.isAir()) {
            if (random.nextInt(WEED_SPAWN_CHANCE_EMPTY) == 0) {
                world.setBlockState(above, ModBlocks.WEED.getDefaultState());
            }
        }
        // Если над грядкой культура - шанс заменить её сорняком
        else if (aboveState.getBlock() instanceof CropBlock || aboveState.isIn(BlockTags.CROPS)) {
            if (random.nextInt(WEED_SPAWN_CHANCE_CROP) == 0) {
                // Сорняк заменяет культуру!
                world.setBlockState(above, ModBlocks.WEED.getDefaultState());
            }
        }
    }
}
