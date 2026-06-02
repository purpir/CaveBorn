package ru.purpir.world;

import com.mojang.serialization.Codec;
import net.minecraft.block.BarrelBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.inventory.LootableInventory;
import net.minecraft.loot.LootTable;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.feature.DefaultFeatureConfig;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.util.FeatureContext;
import ru.purpir.Caveborn;
import ru.purpir.block.LockedMinerCrateBlock;
import ru.purpir.block.ModBlocks;

public class HangingMinersCacheFeature extends Feature<DefaultFeatureConfig> {
    private static final RegistryKey<LootTable> BARREL_LOOT_TABLE = RegistryKey.of(
        RegistryKeys.LOOT_TABLE,
        Identifier.of(Caveborn.MOD_ID, "chests/hanging_miners_cache_barrel")
    );

    public HangingMinersCacheFeature(Codec<DefaultFeatureConfig> codec) {
        super(codec);
    }

    @Override
    public boolean generate(FeatureContext<DefaultFeatureConfig> context) {
        StructureWorldAccess world = context.getWorld();
        Random random = context.getRandom();

        for (int attempt = 0; attempt < 18; attempt++) {
            BlockPos origin = context.getOrigin().add(
                random.nextBetween(-6, 6),
                random.nextBetween(-4, 4),
                random.nextBetween(-6, 6)
            );

            BlockPos ceiling = findCeiling(world, origin);
            if (ceiling != null && canPlaceAt(world, origin, ceiling)) {
                placeCache(world, origin, ceiling, Direction.Type.HORIZONTAL.random(random), random);
                return true;
            }
        }

        return false;
    }

    private BlockPos findCeiling(StructureWorldAccess world, BlockPos origin) {
        for (int y = 5; y <= 14; y++) {
            BlockPos ceiling = origin.up(y);
            if (world.getBlockState(ceiling).isOpaque() && world.getBlockState(ceiling.down()).isAir()) {
                return ceiling;
            }
        }
        return null;
    }

    private boolean canPlaceAt(StructureWorldAccess world, BlockPos origin, BlockPos ceiling) {
        if (ceiling.getY() - origin.getY() < 5) {
            return false;
        }

        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                for (int y = -3; y <= 3; y++) {
                    BlockState state = world.getBlockState(origin.add(x, y, z));
                    if (!state.isAir() || !state.getFluidState().isEmpty()) {
                        return false;
                    }
                }
            }
        }

        return true;
    }

    private void placeCache(StructureWorldAccess world, BlockPos origin, BlockPos ceiling, Direction facing, Random random) {
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                world.setBlockState(origin.add(x, 0, z), Blocks.SPRUCE_PLANKS.getDefaultState(), 3);
            }
        }

        placeChain(world, origin.add(-1, 1, -1), ceiling);
        placeChain(world, origin.add(1, 1, -1), ceiling);
        placeChain(world, origin.add(-1, 1, 1), ceiling);
        placeChain(world, origin.add(1, 1, 1), ceiling);

        BlockPos barrelPos = origin.add(facing.rotateYCounterclockwise().getVector());
        world.setBlockState(barrelPos.up(), Blocks.BARREL.getDefaultState().with(BarrelBlock.FACING, facing), 3);
        LootableInventory.setLootTable(world, random, barrelPos.up(), BARREL_LOOT_TABLE);

        BlockPos cratePos = origin.add(facing.rotateYClockwise().getVector()).up();
        world.setBlockState(cratePos, ModBlocks.LOCKED_MINER_CRATE.getDefaultState()
            .with(LockedMinerCrateBlock.FACING, facing.getOpposite())
            .with(LockedMinerCrateBlock.LOCKED, true), 3);

        world.setBlockState(origin.up(), Blocks.LANTERN.getDefaultState(), 3);
    }

    private void placeChain(StructureWorldAccess world, BlockPos start, BlockPos ceiling) {
        for (BlockPos pos = start; pos.getY() < ceiling.getY(); pos = pos.up()) {
            if (world.getBlockState(pos).isAir()) {
                world.setBlockState(pos, Blocks.IRON_CHAIN.getDefaultState(), 3);
            }
        }
    }
}
