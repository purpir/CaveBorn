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
    private static final BlockState AMETHYST = Blocks.AMETHYST_BLOCK.getDefaultState();

    public AmethystSpikeFeature(Codec<DefaultFeatureConfig> codec) {
        super(codec);
    }

    @Override
    public boolean generate(FeatureContext<DefaultFeatureConfig> context) {
        StructureWorldAccess world = context.getWorld();
        BlockPos origin = context.getOrigin();
        Random random = context.getRandom();

        if (!world.getBlockState(origin.down()).isOpaque()) {
            return false;
        }

        int height = 9 + random.nextInt(9);
        int radius = 2 + random.nextInt(2);
        if (random.nextInt(12) == 0) {
            height += 14 + random.nextInt(18);
            radius += 1;
        }

        for (int y = 0; y < height; y++) {
            float layerRadius = (1.0F - (float) y / (float) height) * radius;
            int blockRadius = MathHelper.ceil(layerRadius);

            for (int x = -blockRadius; x <= blockRadius; x++) {
                float xDistance = MathHelper.abs(x) - 0.25F;
                for (int z = -blockRadius; z <= blockRadius; z++) {
                    float zDistance = MathHelper.abs(z) - 0.25F;
                    if ((x == 0 && z == 0) || xDistance * xDistance + zDistance * zDistance <= layerRadius * layerRadius) {
                        placeSpikeBlock(world, origin.add(x, y, z));
                    }
                }
            }
        }

        int rootDepth = 4 + random.nextInt(7);
        for (int y = -1; y >= -rootDepth; y--) {
            float layerRadius = Math.max(1.0F, radius - MathHelper.abs(y) * 0.35F);
            int blockRadius = MathHelper.floor(layerRadius);

            for (int x = -blockRadius; x <= blockRadius; x++) {
                for (int z = -blockRadius; z <= blockRadius; z++) {
                    if (x * x + z * z <= layerRadius * layerRadius + 0.5F) {
                        world.setBlockState(origin.add(x, y, z), AMETHYST, 3);
                    }
                }
            }
        }

        return true;
    }

    private static void placeSpikeBlock(StructureWorldAccess world, BlockPos pos) {
        BlockState state = world.getBlockState(pos);
        if (state.isAir() || !state.isOpaque()) {
            world.setBlockState(pos, AMETHYST, 3);
        }
    }
}
