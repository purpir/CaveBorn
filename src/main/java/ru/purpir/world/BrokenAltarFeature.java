package ru.purpir.world;

import com.mojang.serialization.Codec;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.feature.DefaultFeatureConfig;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.util.FeatureContext;
import ru.purpir.block.ModBlocks;

public class BrokenAltarFeature extends Feature<DefaultFeatureConfig> {
    public BrokenAltarFeature(Codec<DefaultFeatureConfig> codec) {
        super(codec);
    }

    @Override
    public boolean generate(FeatureContext<DefaultFeatureConfig> context) {
        StructureWorldAccess world = context.getWorld();
        BlockPos origin = context.getOrigin();
        Random random = context.getRandom();
        BlockPos ground = origin.down();

        if (!canPlaceAt(world, ground)) {
            return false;
        }

        placeBrokenAltar(world, origin, random);
        return true;
    }

    private boolean canPlaceAt(StructureWorldAccess world, BlockPos ground) {
        for (int x = -2; x <= 2; x++) {
            for (int z = -2; z <= 2; z++) {
                BlockPos floor = ground.add(x, 0, z);
                if (!world.getBlockState(floor).isSolidBlock(world, floor)) {
                    return false;
                }
                for (int y = 1; y <= 5; y++) {
                    if (!world.getBlockState(floor.up(y)).isReplaceable()) {
                        return false;
                    }
                }
            }
        }
        return !world.getBlockState(ground).isIn(BlockTags.LEAVES);
    }

    private void placeBrokenAltar(StructureWorldAccess world, BlockPos origin, Random random) {
        clearSpace(world, origin);
        placeLayer(world, origin, 0, random, 2);
        placeColumn(world, origin);
        placeLayer(world, origin, 4, random, 4);
        placeDebris(world, origin, random);
    }

    private void clearSpace(StructureWorldAccess world, BlockPos origin) {
        for (int x = -2; x <= 2; x++) {
            for (int z = -2; z <= 2; z++) {
                for (int y = 0; y <= 5; y++) {
                    BlockPos pos = origin.add(x, y, z);
                    if (world.getBlockState(pos).isReplaceable()) {
                        world.setBlockState(pos, Blocks.AIR.getDefaultState(), 3);
                    }
                }
            }
        }
    }

    private void placeLayer(StructureWorldAccess world, BlockPos origin, int y, Random random, int missing) {
        int skipped = 0;
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                if ((x != 0 || z != 0) && skipped < missing && random.nextInt(4) == 0) {
                    skipped++;
                    continue;
                }

                BlockState state = Math.abs(x) + Math.abs(z) == 1
                    ? Blocks.GOLD_BLOCK.getDefaultState()
                    : ModBlocks.BRONZE_BLOCK.getDefaultState();
                world.setBlockState(origin.add(x, y, z), state, 3);
            }
        }
    }

    private void placeColumn(StructureWorldAccess world, BlockPos origin) {
        world.setBlockState(origin.up(), Blocks.CRIMSON_STEM.getDefaultState(), 3);
        world.setBlockState(origin.up(2), ModBlocks.CHAOS_CORE.getDefaultState(), 3);
        world.setBlockState(origin.up(3), Blocks.CRIMSON_STEM.getDefaultState(), 3);
    }

    private void placeDebris(StructureWorldAccess world, BlockPos origin, Random random) {
        BlockState[] debris = new BlockState[] {
            ModBlocks.BRONZE_BLOCK.getDefaultState(),
            Blocks.GOLD_BLOCK.getDefaultState(),
            Blocks.CRIMSON_STEM.getDefaultState(),
            Blocks.CRYING_OBSIDIAN.getDefaultState()
        };

        for (int attempt = 0; attempt < 9; attempt++) {
            int x = random.nextBetween(-3, 3);
            int z = random.nextBetween(-3, 3);
            if (Math.abs(x) <= 1 && Math.abs(z) <= 1) {
                continue;
            }

            BlockPos pos = origin.add(x, 0, z);
            if (world.getBlockState(pos).isReplaceable() && world.getBlockState(pos.down()).isSolidBlock(world, pos.down())) {
                world.setBlockState(pos, debris[random.nextInt(debris.length)], 3);
            }
        }
    }
}
