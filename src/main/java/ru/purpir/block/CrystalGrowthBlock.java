package ru.purpir.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCollisionHandler;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;

public class CrystalGrowthBlock extends Block {
    public static final MapCodec<CrystalGrowthBlock> CODEC = createCodec(CrystalGrowthBlock::new);
    public static final EnumProperty<Direction> FACING = Properties.FACING;
    private static final VoxelShape FLOOR_SHAPE = Block.createCuboidShape(3.0, 0.0, 3.0, 13.0, 11.0, 13.0);
    private static final VoxelShape CEILING_SHAPE = Block.createCuboidShape(3.0, 5.0, 3.0, 13.0, 16.0, 13.0);

    public CrystalGrowthBlock(Settings settings) {
        super(settings);
        setDefaultState(getDefaultState().with(FACING, Direction.UP));
    }

    @Override
    protected MapCodec<? extends Block> getCodec() {
        return CODEC;
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext context) {
        Direction side = context.getSide();
        Direction facing = side == Direction.DOWN ? Direction.DOWN : Direction.UP;
        BlockState state = getDefaultState().with(FACING, facing);
        return canPlaceAt(state, context.getWorld(), context.getBlockPos()) ? state : null;
    }

    @Override
    protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return state.get(FACING) == Direction.DOWN ? CEILING_SHAPE : FLOOR_SHAPE;
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return VoxelShapes.empty();
    }

    @Override
    protected boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
        Direction facing = state.get(FACING);
        BlockPos supportPos = pos.offset(facing.getOpposite());
        return world.getBlockState(supportPos).isSideSolidFullSquare(world, supportPos, facing);
    }

    @Override
    protected void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity, EntityCollisionHandler handler, boolean initialCollision) {
        if (world instanceof ServerWorld serverWorld && entity.age % 20 == 0) {
            entity.damage(serverWorld, serverWorld.getDamageSources().cactus(), 1.0F);
        }
    }
}
