package ru.purpir.world;

import com.mojang.serialization.Codec;
import net.minecraft.block.BarrelBlock;
import net.minecraft.block.BedBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CampfireBlock;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.block.LanternBlock;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.enums.BedPart;
import net.minecraft.block.enums.SlabType;
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

public class MinersCampFeature extends Feature<DefaultFeatureConfig> {
    private static final RegistryKey<LootTable> BARREL_LOOT_TABLE = RegistryKey.of(
        RegistryKeys.LOOT_TABLE,
        Identifier.of(Caveborn.MOD_ID, "chests/miners_camp_barrel")
    );

    public MinersCampFeature(Codec<DefaultFeatureConfig> codec) {
        super(codec);
    }

    @Override
    public boolean generate(FeatureContext<DefaultFeatureConfig> context) {
        StructureWorldAccess world = context.getWorld();
        Random random = context.getRandom();

        for (int attempt = 0; attempt < 18; attempt++) {
            BlockPos candidate = context.getOrigin().add(
                random.nextBetween(-6, 6),
                random.nextBetween(-5, 5),
                random.nextBetween(-6, 6)
            );

            for (int y = -6; y <= 6; y++) {
                BlockPos floor = candidate.up(y);
                if (canPlaceAt(world, floor)) {
                    Direction facing = Direction.Type.HORIZONTAL.random(random);
                    placeCamp(world, floor, facing, random);
                    return true;
                }
            }
        }

        return false;
    }

    private boolean canPlaceAt(StructureWorldAccess world, BlockPos floor) {
        if (!world.getBlockState(floor).isOf(Blocks.DEEPSLATE)) {
            return false;
        }

        int airBlocks = 0;
        for (int x = -3; x <= 3; x++) {
            for (int z = -2; z <= 2; z++) {
                BlockPos floorPos = floor.add(x, 0, z);
                if (!isDeepslateLike(world.getBlockState(floorPos))) {
                    return false;
                }

                for (int y = 1; y <= 3; y++) {
                    BlockState state = world.getBlockState(floorPos.up(y));
                    if (state.isAir()) {
                        airBlocks++;
                    } else if (!state.getFluidState().isEmpty()) {
                        return false;
                    }
                }
            }
        }

        return airBlocks >= 60;
    }

    private void placeCamp(StructureWorldAccess world, BlockPos origin, Direction facing, Random random) {
        clearRoom(world, origin, facing);
        placeFloor(world, origin, facing, random);
        placeSupports(world, origin, facing);
        placeFurniture(world, origin, facing, random);
    }

    private void clearRoom(StructureWorldAccess world, BlockPos origin, Direction facing) {
        for (int x = -3; x <= 3; x++) {
            for (int z = -2; z <= 2; z++) {
                for (int y = 1; y <= 3; y++) {
                    world.setBlockState(relative(origin, facing, x, y, z), Blocks.AIR.getDefaultState(), 3);
                }
            }
        }
    }

    private void placeFloor(StructureWorldAccess world, BlockPos origin, Direction facing, Random random) {
        for (int x = -3; x <= 3; x++) {
            for (int z = -2; z <= 2; z++) {
                BlockState state = random.nextInt(5) == 0
                    ? Blocks.DEEPSLATE_BRICKS.getDefaultState()
                    : Blocks.DEEPSLATE.getDefaultState();
                world.setBlockState(relative(origin, facing, x, 0, z), state, 3);
            }
        }
    }

    private void placeSupports(StructureWorldAccess world, BlockPos origin, Direction facing) {
        for (int x : new int[] {-3, 3}) {
            for (int z : new int[] {-2, 2}) {
                for (int y = 1; y <= 3; y++) {
                    BlockState state = y == 3 ? Blocks.SPRUCE_PLANKS.getDefaultState() : Blocks.SPRUCE_FENCE.getDefaultState();
                    world.setBlockState(relative(origin, facing, x, y, z), state, 3);
                }
            }
        }

        for (int x = -2; x <= 2; x++) {
            world.setBlockState(relative(origin, facing, x, 3, -2), Blocks.SPRUCE_SLAB.getDefaultState().with(SlabBlock.TYPE, SlabType.TOP), 3);
            world.setBlockState(relative(origin, facing, x, 3, 2), Blocks.SPRUCE_SLAB.getDefaultState().with(SlabBlock.TYPE, SlabType.TOP), 3);
        }

        placeHangingLantern(world, origin, facing);
    }

    private void placeHangingLantern(StructureWorldAccess world, BlockPos origin, Direction facing) {
        for (int[] offset : new int[][] {{0, 0}, {-1, 0}, {1, 0}, {0, -1}, {0, 1}}) {
            BlockPos floor = relative(origin, facing, offset[0], 0, offset[1]);
            for (int y = 10; y >= 4; y--) {
                BlockPos ceiling = floor.up(y);
                if (world.getBlockState(ceiling).isOpaque() && world.getBlockState(ceiling.down()).isAir()) {
                    BlockPos lanternPos = ceiling.down(Math.min(3, y - 2));
                    if (!world.getBlockState(lanternPos).isAir()) {
                        continue;
                    }

                    for (BlockPos chainPos = ceiling.down(); chainPos.getY() > lanternPos.getY(); chainPos = chainPos.down()) {
                        if (world.getBlockState(chainPos).isAir()) {
                            world.setBlockState(chainPos, Blocks.IRON_CHAIN.getDefaultState(), 3);
                        }
                    }
                    world.setBlockState(lanternPos, Blocks.LANTERN.getDefaultState().with(LanternBlock.HANGING, true), 3);
                    return;
                }
            }
        }
    }

    private void placeFurniture(StructureWorldAccess world, BlockPos origin, Direction facing, Random random) {
        Direction inward = facing.getOpposite();
        placeBed(world, relative(origin, facing, -2, 1, -1), facing);

        placeLootBarrel(world, relative(origin, facing, 2, 1, -1), inward, random);
        placeLootBarrel(world, relative(origin, facing, 2, 1, 0), inward, random);

        world.setBlockState(relative(origin, facing, -2, 1, 1), Blocks.CRAFTING_TABLE.getDefaultState(), 3);
        world.setBlockState(relative(origin, facing, -1, 1, 1), Blocks.FURNACE.getDefaultState().with(HorizontalFacingBlock.FACING, inward), 3);
        world.setBlockState(relative(origin, facing, 0, 1, -1), Blocks.CAMPFIRE.getDefaultState().with(CampfireBlock.LIT, false), 3);
        world.setBlockState(relative(origin, facing, 1, 1, 1), Blocks.TORCH.getDefaultState(), 3);
        world.setBlockState(relative(origin, facing, 0, 1, 1), Blocks.RAIL.getDefaultState(), 3);
        world.setBlockState(relative(origin, facing, 1, 1, -1), Blocks.STONECUTTER.getDefaultState().with(HorizontalFacingBlock.FACING, inward), 3);
    }

    private void placeBed(StructureWorldAccess world, BlockPos foot, Direction facing) {
        world.setBlockState(foot, Blocks.BROWN_BED.getDefaultState()
            .with(BedBlock.FACING, facing)
            .with(BedBlock.PART, BedPart.FOOT), 3);
        world.setBlockState(foot.offset(facing), Blocks.BROWN_BED.getDefaultState()
            .with(BedBlock.FACING, facing)
            .with(BedBlock.PART, BedPart.HEAD), 3);
    }

    private void placeLootBarrel(StructureWorldAccess world, BlockPos pos, Direction facing, Random random) {
        world.setBlockState(pos, Blocks.BARREL.getDefaultState().with(BarrelBlock.FACING, facing), 3);
        LootableInventory.setLootTable(world, random, pos, BARREL_LOOT_TABLE);
    }

    private boolean isDeepslateLike(BlockState state) {
        return state.isOf(Blocks.DEEPSLATE) || state.isOf(Blocks.COBBLED_DEEPSLATE) || state.isOf(Blocks.DEEPSLATE_BRICKS);
    }

    private BlockPos relative(BlockPos origin, Direction facing, int x, int y, int z) {
        Direction right = facing.rotateYClockwise();
        return origin.offset(right, x).offset(facing, z).up(y);
    }
}
