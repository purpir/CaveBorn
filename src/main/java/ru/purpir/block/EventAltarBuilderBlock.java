package ru.purpir.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import ru.purpir.eventaltar.EventAltarHandler;

public class EventAltarBuilderBlock extends Block {
    public EventAltarBuilderBlock(Settings settings) {
        super(settings);
    }

    @Override
    protected BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.INVISIBLE;
    }

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack) {
        super.onPlaced(world, pos, state, placer, itemStack);
        if (!(world instanceof ServerWorld serverWorld)) {
            return;
        }

        BlockPos anchorPos = pos.up(2);
        buildAltar(serverWorld, anchorPos);
        EventAltarHandler.activateAltar(serverWorld, anchorPos);
        serverWorld.playSound(null, anchorPos, SoundEvents.BLOCK_RESPAWN_ANCHOR_CHARGE, SoundCategory.BLOCKS, 1.0F, 1.1F);
    }

    private void buildAltar(ServerWorld world, BlockPos anchorPos) {
        placeLayer(world, anchorPos, -2);
        placeLayer(world, anchorPos, 2);
        world.setBlockState(anchorPos.down(), Blocks.CRIMSON_STEM.getDefaultState(), Block.NOTIFY_ALL);
        world.setBlockState(anchorPos, Blocks.RESPAWN_ANCHOR.getDefaultState(), Block.NOTIFY_ALL);
        world.setBlockState(anchorPos.up(), Blocks.CRIMSON_STEM.getDefaultState(), Block.NOTIFY_ALL);
    }

    private void placeLayer(ServerWorld world, BlockPos origin, int y) {
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                boolean gold = Math.abs(x) + Math.abs(z) == 1;
                BlockState state = gold ? Blocks.GOLD_BLOCK.getDefaultState() : ModBlocks.BRONZE_BLOCK.getDefaultState();
                world.setBlockState(origin.add(x, y, z), state, Block.NOTIFY_ALL);
            }
        }
    }
}
