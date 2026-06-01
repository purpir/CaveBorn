package ru.purpir.world;

import com.mojang.serialization.Codec;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.feature.DefaultFeatureConfig;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.util.FeatureContext;
import ru.purpir.block.CrystalGrowthBlock;
import ru.purpir.block.ModBlocks;

public class CrystalGrowthFeature extends Feature<DefaultFeatureConfig> {
    public CrystalGrowthFeature(Codec<DefaultFeatureConfig> codec) {
        super(codec);
    }

    @Override
    public boolean generate(FeatureContext<DefaultFeatureConfig> context) {
        StructureWorldAccess world = context.getWorld();
        BlockPos origin = context.getOrigin();
        Random random = context.getRandom();
        boolean placed = false;

        for (int attempt = 0; attempt < 28; attempt++) {
            BlockPos pos = origin.add(
                random.nextBetween(-5, 5),
                random.nextBetween(-3, 3),
                random.nextBetween(-5, 5)
            );

            if (!world.isAir(pos)) {
                continue;
            }

            boolean tryCeilingFirst = random.nextBoolean();
            placed |= tryPlace(world, pos, tryCeilingFirst ? Direction.DOWN : Direction.UP);
            if (!placed || random.nextBoolean()) {
                placed |= tryPlace(world, pos, tryCeilingFirst ? Direction.UP : Direction.DOWN);
            }
        }

        return placed;
    }

    private boolean tryPlace(StructureWorldAccess world, BlockPos pos, Direction facing) {
        BlockState state = ModBlocks.CRYSTAL_GROWTH.getDefaultState().with(CrystalGrowthBlock.FACING, facing);
        if (state.canPlaceAt(world, pos)) {
            world.setBlockState(pos, state, 3);
            return true;
        }
        return false;
    }
}
