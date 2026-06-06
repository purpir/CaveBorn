package ru.purpir.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCollisionHandler;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import ru.purpir.effect.ModStatusEffects;
import ru.purpir.enchantment.SolarInfusionSystem;

public class HogweedPasteBlock extends Block {
    public static final MapCodec<HogweedPasteBlock> CODEC = createCodec(HogweedPasteBlock::new);
    public static final EnumProperty<Direction> FACING = Properties.FACING;
    public static final BooleanProperty SOLAR = BooleanProperty.of("solar");

    private static final VoxelShape UP_SHAPE = Block.createCuboidShape(1.0, 0.0, 1.0, 15.0, 1.0, 15.0);
    private static final VoxelShape DOWN_SHAPE = Block.createCuboidShape(1.0, 15.0, 1.0, 15.0, 16.0, 15.0);
    private static final VoxelShape NORTH_SHAPE = Block.createCuboidShape(1.0, 1.0, 15.0, 15.0, 15.0, 16.0);
    private static final VoxelShape SOUTH_SHAPE = Block.createCuboidShape(1.0, 1.0, 0.0, 15.0, 15.0, 1.0);
    private static final VoxelShape EAST_SHAPE = Block.createCuboidShape(0.0, 1.0, 1.0, 1.0, 15.0, 15.0);
    private static final VoxelShape WEST_SHAPE = Block.createCuboidShape(15.0, 1.0, 1.0, 16.0, 15.0, 15.0);

    public HogweedPasteBlock(Settings settings) {
        super(settings);
        setDefaultState(getDefaultState().with(FACING, Direction.UP).with(SOLAR, false));
    }

    @Override
    protected MapCodec<? extends Block> getCodec() {
        return CODEC;
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING, SOLAR);
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext context) {
        Direction facing = context.getSide();
        BlockState state = getDefaultState()
            .with(FACING, facing)
            .with(SOLAR, SolarInfusionSystem.isInfused(context.getStack()));
        return canPlaceAt(state, context.getWorld(), context.getBlockPos()) ? state : null;
    }

    @Override
    protected boolean canReplace(BlockState state, ItemPlacementContext context) {
        return true;
    }

    @Override
    protected boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
        Direction facing = state.get(FACING);
        BlockPos supportPos = pos.offset(facing.getOpposite());
        return world.getBlockState(supportPos).isSideSolidFullSquare(world, supportPos, facing);
    }

    @Override
    protected BlockState getStateForNeighborUpdate(BlockState state, WorldView world, net.minecraft.world.tick.ScheduledTickView tickView,
                                                   BlockPos pos, Direction direction, BlockPos neighborPos, BlockState neighborState,
                                                   net.minecraft.util.math.random.Random random) {
        if (!canPlaceAt(state, world, pos) || isWaterTouching(world, pos)) {
            return Blocks.AIR.getDefaultState();
        }
        return state;
    }

    @Override
    protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return switch (state.get(FACING)) {
            case DOWN -> DOWN_SHAPE;
            case NORTH -> NORTH_SHAPE;
            case SOUTH -> SOUTH_SHAPE;
            case EAST -> EAST_SHAPE;
            case WEST -> WEST_SHAPE;
            default -> UP_SHAPE;
        };
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return VoxelShapes.empty();
    }

    @Override
    protected void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity, EntityCollisionHandler handler, boolean initialCollision) {
        if (!touchesPasteShape(state, world, pos, entity)) {
            return;
        }

        boolean solar = state.get(SOLAR);
        entity.slowMovement(state, solar ? new Vec3d(0.28D, 1.0D, 0.28D) : new Vec3d(0.42D, 1.0D, 0.42D));
        if (world instanceof ServerWorld && entity instanceof LivingEntity living && entity.age % 10 == 0) {
            living.addStatusEffect(new StatusEffectInstance(StatusEffects.POISON, 60, 4));
            living.addStatusEffect(new StatusEffectInstance(StatusEffects.NAUSEA, 100, 0));
            if (solar) {
                living.addStatusEffect(new StatusEffectInstance(ModStatusEffects.SOLAR_BURN, 200, 0, false, true, true));
                living.addStatusEffect(new StatusEffectInstance(StatusEffects.NAUSEA, 220, 0, false, false, false));
            }
        }
    }

    private static boolean touchesPasteShape(BlockState state, BlockView world, BlockPos pos, Entity entity) {
        Box pasteBox = state.getOutlineShape(world, pos).getBoundingBox().offset(pos).expand(0.001);
        return pasteBox.intersects(entity.getBoundingBox());
    }

    private static boolean isWaterTouching(WorldView world, BlockPos pos) {
        if (world.getFluidState(pos).isOf(Fluids.WATER)) {
            return true;
        }
        for (Direction direction : Direction.values()) {
            if (world.getFluidState(pos.offset(direction)).isOf(Fluids.WATER)) {
                return true;
            }
        }
        return false;
    }
}
