package ru.purpir.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import ru.purpir.block.entity.AutomaticSpawnerBlockEntity;
import ru.purpir.block.entity.ModBlockEntities;

public class AutomaticSpawnerBlock extends BlockWithEntity {
    public static final MapCodec<AutomaticSpawnerBlock> CODEC = createCodec(AutomaticSpawnerBlock::new);

    public AutomaticSpawnerBlock(Settings settings) { super(settings); }
    @Override protected MapCodec<? extends BlockWithEntity> getCodec() { return CODEC; }
    @Override protected BlockRenderType getRenderType(BlockState state) { return BlockRenderType.MODEL; }
    @Override public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new AutomaticSpawnerBlockEntity(pos, state);
    }
    @Override @Nullable public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return null; // Processing is done by the world-level saved data, including unloaded chunks.
    }
    @Override protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        if (!world.isClient() && world.getBlockEntity(pos) instanceof AutomaticSpawnerBlockEntity spawner) {
            player.openHandledScreen(spawner);
        }
        return ActionResult.SUCCESS;
    }
    @Override protected void onStateReplaced(BlockState state, ServerWorld world, BlockPos pos, boolean moved) {
        if (world.getBlockEntity(pos) instanceof AutomaticSpawnerBlockEntity spawner) {
            net.minecraft.util.ItemScatterer.spawn(world, pos, spawner);
            spawner.unregister();
        }
        super.onStateReplaced(state, world, pos, moved);
    }
}
