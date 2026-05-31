package ru.purpir.world;

import com.mojang.serialization.Codec;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.feature.DefaultFeatureConfig;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.util.FeatureContext;

public class AmethystSpikeFeature extends Feature<DefaultFeatureConfig> {
    
    public AmethystSpikeFeature(Codec<DefaultFeatureConfig> codec) {
        super(codec);
    }

    @Override
    public boolean generate(FeatureContext<DefaultFeatureConfig> context) {
        StructureWorldAccess world = context.getWorld();
        BlockPos pos = context.getOrigin();
        Random random = context.getRandom();
        
        // Высота столба от 15 до 35 блоков
        int height = 15 + random.nextInt(21);
        
        // Проверяем, можно ли разместить столб
        if (!world.getBlockState(pos.down()).isOpaque()) {
            return false;
        }
        
        // Генерируем столб с конусообразной формой
        for (int y = 0; y < height; y++) {
            float progress = (float) y / height;
            // Радиус уменьшается к вершине
            int radius = MathHelper.ceil((1.0f - progress) * 3.0f);
            
            for (int x = -radius; x <= radius; x++) {
                for (int z = -radius; z <= radius; z++) {
                    double distance = Math.sqrt(x * x + z * z);
                    if (distance <= radius) {
                        BlockPos placePos = pos.add(x, y, z);
                        BlockState state = world.getBlockState(placePos);
                        
                        // Заменяем только воздух и мягкие блоки
                        if (state.isAir() || !state.isOpaque()) {
                            world.setBlockState(placePos, Blocks.AMETHYST_BLOCK.getDefaultState(), 3);
                        }
                    }
                }
            }
        }
        
        return true;
    }
}
