package ru.purpir.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.block.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;

public class WeedTopBlock extends Block {
    public static final MapCodec<WeedTopBlock> CODEC = createCodec(WeedTopBlock::new);
    private static final VoxelShape SHAPE = Block.createCuboidShape(2, 0, 2, 14, 14, 14);
    
    public WeedTopBlock(Settings settings) {
        super(settings);
    }
    
    @Override
    protected MapCodec<? extends Block> getCodec() {
        return CODEC;
    }
    
    @Override
    protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPE;
    }
    
    @Override
    public boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
        BlockPos below = pos.down();
        BlockState belowState = world.getBlockState(below);
        return belowState.isOf(ModBlocks.WEED) && belowState.get(WeedBlock.AGE) == 1;
    }
    
    @Override
    public BlockState onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        // Когда ломаем верх, нижняя часть становится маленькой
        BlockPos below = pos.down();
        BlockState belowState = world.getBlockState(below);
        if (belowState.isOf(ModBlocks.WEED)) {
            world.setBlockState(below, belowState.with(WeedBlock.AGE, 0));
        }
        return super.onBreak(world, pos, state, player);
    }
}
