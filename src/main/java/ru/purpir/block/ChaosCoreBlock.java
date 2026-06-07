package ru.purpir.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import ru.purpir.block.entity.ChaosCoreBlockEntity;
import ru.purpir.block.entity.ModBlockEntities;

public class ChaosCoreBlock extends BlockWithEntity {
    public static final MapCodec<ChaosCoreBlock> CODEC = createCodec(ChaosCoreBlock::new);

    public ChaosCoreBlock(Settings settings) {
        super(settings);
    }

    @Override
    protected MapCodec<? extends BlockWithEntity> getCodec() {
        return CODEC;
    }

    @Override
    protected BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    @Nullable
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new ChaosCoreBlockEntity(pos, state);
    }

    @Override
    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        if (type != ModBlockEntities.CHAOS_CORE) {
            return null;
        }
        return (tickerWorld, pos, tickerState, blockEntity) ->
            ChaosCoreBlockEntity.tick(tickerWorld, pos, tickerState, (ChaosCoreBlockEntity) blockEntity);
    }
}
