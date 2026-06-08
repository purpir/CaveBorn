package ru.purpir.util;

import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public final class WallManager {
    private static final float HORIZONTAL_PITCH_THRESHOLD = 55.0F;
    private static final Map<WallBlockKey, WallBlock> WALL_BLOCKS = new HashMap<>();

    private WallManager() {
    }

    public static void register() {
        ServerTickEvents.END_WORLD_TICK.register(WallManager::tickWorld);
        PlayerBlockBreakEvents.BEFORE.register((world, player, pos, state, blockEntity) -> {
            WallBlock wallBlock = WALL_BLOCKS.get(new WallBlockKey(world.getRegistryKey(), pos));
            return wallBlock == null || wallBlock.breakable();
        });
    }

    public static void createFromPlayerLook(PlayerEntity player, World world, BlockState wallState, int width, int height, int depth,
                                            boolean temporary, int secondsToDisappear, boolean breakable) {
        if (world.isClient()) {
            return;
        }

        float pitch = player.getPitch();
        if (pitch <= -HORIZONTAL_PITCH_THRESHOLD) {
            createHorizontal(player, world, wallState, width, height, depth, true, temporary, secondsToDisappear, breakable);
            return;
        }
        if (pitch >= HORIZONTAL_PITCH_THRESHOLD) {
            createHorizontal(player, world, wallState, width, height, depth, false, temporary, secondsToDisappear, breakable);
            return;
        }

        createVertical(player, world, wallState, width, height, depth, temporary, secondsToDisappear, breakable);
    }

    public static boolean canBreak(World world, BlockPos pos) {
        WallBlock wallBlock = WALL_BLOCKS.get(new WallBlockKey(world.getRegistryKey(), pos));
        return wallBlock == null || wallBlock.breakable();
    }

    private static void createVertical(PlayerEntity player, World world, BlockState wallState, int width, int height, int depth,
                                       boolean temporary, int secondsToDisappear, boolean breakable) {
        Direction facing = player.getHorizontalFacing();
        BlockPos startPos = player.getBlockPos().offset(facing, 2);
        int halfWidth = width / 2;

        for (int side = -halfWidth; side <= halfWidth; side++) {
            for (int y = 0; y < height; y++) {
                for (int layer = 0; layer < depth; layer++) {
                    BlockPos pos;
                    if (facing == Direction.NORTH || facing == Direction.SOUTH) {
                        pos = startPos.add(side, y, layer * facing.getOffsetZ());
                    } else {
                        pos = startPos.add(layer * facing.getOffsetX(), y, side);
                    }
                    placeWallBlock(world, pos, wallState, temporary, secondsToDisappear, breakable);
                }
            }
        }
    }

    private static void createHorizontal(PlayerEntity player, World world, BlockState wallState, int width, int height, int depth,
                                         boolean above, boolean temporary, int secondsToDisappear, boolean breakable) {
        Direction facing = player.getHorizontalFacing();
        Direction right = facing.rotateYClockwise();
        BlockPos center = player.getBlockPos().offset(facing, 2).offset(above ? Direction.UP : Direction.DOWN, 2);
        int halfWidth = width / 2;
        int halfHeight = height / 2;
        int yDirection = above ? 1 : -1;

        for (int x = -halfWidth; x <= halfWidth; x++) {
            for (int z = -halfHeight; z <= halfHeight; z++) {
                for (int layer = 0; layer < depth; layer++) {
                    BlockPos pos = center
                        .offset(right, x)
                        .offset(facing, z)
                        .offset(Direction.UP, layer * yDirection);
                    placeWallBlock(world, pos, wallState, temporary, secondsToDisappear, breakable);
                }
            }
        }
    }

    private static void placeWallBlock(World world, BlockPos pos, BlockState wallState, boolean temporary, int secondsToDisappear, boolean breakable) {
        BlockState previousState = world.getBlockState(pos);
        if (!previousState.isReplaceable()) {
            return;
        }

        world.setBlockState(pos, wallState);
        long removeAt = temporary ? world.getTime() + Math.max(1, secondsToDisappear) * 20L : -1L;
        WALL_BLOCKS.put(new WallBlockKey(world.getRegistryKey(), pos.toImmutable()), new WallBlock(wallState, previousState, removeAt, breakable));
    }

    private static void tickWorld(ServerWorld world) {
        long time = world.getTime();
        RegistryKey<World> worldKey = world.getRegistryKey();
        Iterator<Map.Entry<WallBlockKey, WallBlock>> iterator = WALL_BLOCKS.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<WallBlockKey, WallBlock> entry = iterator.next();
            WallBlockKey key = entry.getKey();
            WallBlock wallBlock = entry.getValue();
            if (!key.world().equals(worldKey) || wallBlock.removeAt() < 0 || wallBlock.removeAt() > time) {
                continue;
            }

            if (world.getBlockState(key.pos()).isOf(wallBlock.wallState().getBlock())) {
                world.setBlockState(key.pos(), wallBlock.previousState().isAir() ? Blocks.AIR.getDefaultState() : wallBlock.previousState());
            }
            iterator.remove();
        }
    }

    private record WallBlockKey(RegistryKey<World> world, BlockPos pos) {
    }

    private record WallBlock(BlockState wallState, BlockState previousState, long removeAt, boolean breakable) {
    }
}
