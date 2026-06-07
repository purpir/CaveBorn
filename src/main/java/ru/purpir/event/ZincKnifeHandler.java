package ru.purpir.event;

import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import ru.purpir.block.ModBlocks;
import ru.purpir.item.ModItems;

public final class ZincKnifeHandler {
    private ZincKnifeHandler() {
    }

    public static void register() {
        PlayerBlockBreakEvents.AFTER.register((world, player, pos, state, blockEntity) -> {
            if (!(world instanceof ServerWorld serverWorld) || !player.getMainHandStack().isOf(ModItems.ZINC_KNIFE)) {
                return;
            }
            if (!isPlantResourceBlock(state)) {
                return;
            }

            if (isFiberPlant(state) && serverWorld.random.nextFloat() < 0.35F) {
                drop(serverWorld, pos, new ItemStack(ModItems.FIBER));
            }
            if (canDropSeeds(state) && serverWorld.random.nextFloat() < 0.22F) {
                drop(serverWorld, pos, new ItemStack(Items.WHEAT_SEEDS));
            }
            player.getMainHandStack().damage(1, player);
        });
    }

    private static boolean isPlantResourceBlock(BlockState state) {
        return state.isIn(BlockTags.FLOWERS)
            || state.isIn(BlockTags.LEAVES)
            || state.isIn(BlockTags.CROPS)
            || state.isOf(Blocks.SHORT_GRASS)
            || state.isOf(Blocks.TALL_GRASS)
            || state.isOf(Blocks.FERN)
            || state.isOf(Blocks.LARGE_FERN)
            || state.isOf(Blocks.VINE)
            || state.isOf(Blocks.CAVE_VINES)
            || state.isOf(Blocks.CAVE_VINES_PLANT)
            || state.isOf(ModBlocks.WEED)
            || state.isOf(ModBlocks.WEED_TOP)
            || state.isOf(ModBlocks.HOGWEED)
            || state.isOf(ModBlocks.SOLAR_IRIS)
            || state.isOf(ModBlocks.VOID_EYE_PLANT);
    }

    private static boolean isFiberPlant(BlockState state) {
        return state.isOf(ModBlocks.WEED)
            || state.isOf(ModBlocks.WEED_TOP);
    }

    private static boolean canDropSeeds(BlockState state) {
        return state.isOf(Blocks.SHORT_GRASS)
            || state.isOf(Blocks.TALL_GRASS)
            || state.isOf(Blocks.FERN)
            || state.isOf(Blocks.LARGE_FERN)
            || state.isIn(BlockTags.FLOWERS)
            || state.isIn(BlockTags.CROPS);
    }

    private static void drop(ServerWorld world, BlockPos pos, ItemStack stack) {
        ItemEntity entity = new ItemEntity(world, pos.getX() + 0.5, pos.getY() + 0.35, pos.getZ() + 0.5, stack);
        entity.setVelocity(
            (world.random.nextDouble() - 0.5) * 0.12,
            0.12,
            (world.random.nextDouble() - 0.5) * 0.12
        );
        world.spawnEntity(entity);
    }
}
