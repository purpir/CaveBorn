package ru.purpir.world;

import com.mojang.serialization.Codec;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.feature.DefaultFeatureConfig;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.util.FeatureContext;
import ru.purpir.block.HogweedBlock;
import ru.purpir.block.ModBlocks;

/**
 * Генерирует кучки борщевика высотой 2-8 блоков
 */
public class HogweedFeature extends Feature<DefaultFeatureConfig> {
    
    public HogweedFeature(Codec<DefaultFeatureConfig> configCodec) {
        super(configCodec);
    }
    
    @Override
    public boolean generate(FeatureContext<DefaultFeatureConfig> context) {
        StructureWorldAccess world = context.getWorld();
        BlockPos origin = context.getOrigin();
        Random random = context.getRandom();
        
        int placed = 0;
        
        // Генерируем кучку борщевика (15-30 растений)
        int count = 15 + random.nextInt(16);
        
        for (int i = 0; i < count; i++) {
            // Случайная позиция в радиусе 6 блоков
            int x = origin.getX() + random.nextInt(13) - 6;
            int z = origin.getZ() + random.nextInt(13) - 6;
            
            // Ищем поверхность
            BlockPos.Mutable pos = new BlockPos.Mutable(x, origin.getY() + 10, z);
            for (int y = 0; y < 20; y++) {
                pos.setY(origin.getY() + 10 - y);
                BlockState ground = world.getBlockState(pos);
                BlockState above = world.getBlockState(pos.up());
                
                if (isValidGround(ground) && above.isAir()) {
                    // Нашли место - генерируем борщевик
                    int height = 2 + random.nextInt(7); // 2-8 блоков
                    if (placeHogweed(world, pos.up(), height)) {
                        placed++;
                    }
                    break;
                }
            }
        }
        
        return placed > 0;
    }
    
    private boolean isValidGround(BlockState state) {
        return state.isOf(Blocks.GRASS_BLOCK) || state.isOf(Blocks.DIRT) || 
               state.isOf(Blocks.PODZOL) || state.isOf(Blocks.COARSE_DIRT);
    }
    
    private boolean placeHogweed(StructureWorldAccess world, BlockPos basePos, int height) {
        // Проверяем что есть место для всей высоты
        for (int i = 0; i < height; i++) {
            if (!world.getBlockState(basePos.up(i)).isAir()) {
                height = i; // Уменьшаем высоту
                break;
            }
        }
        
        if (height < 2) return false; // Минимум 2 блока
        
        // Ставим стебли (все кроме верхнего)
        for (int i = 0; i < height - 1; i++) {
            world.setBlockState(basePos.up(i), 
                ModBlocks.HOGWEED.getDefaultState().with(HogweedBlock.TOP, false), 2);
        }
        
        // Ставим верхушку
        world.setBlockState(basePos.up(height - 1), 
            ModBlocks.HOGWEED.getDefaultState().with(HogweedBlock.TOP, true), 2);
        
        return true;
    }
}
