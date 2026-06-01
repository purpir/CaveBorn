package ru.purpir.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import ru.purpir.block.entity.CrusherBlockEntity;
import ru.purpir.block.entity.ModBlockEntities;
import ru.purpir.multiblock.IMultiblock;
import ru.purpir.multiblock.MultiblockManager;
import ru.purpir.multiblock.MultiblockStructure;

public class CrusherBlock extends BlockWithEntity implements IMultiblock {
    public static final MapCodec<CrusherBlock> CODEC = createCodec(CrusherBlock::new);
    public static final EnumProperty<Direction> FACING = Properties.HORIZONTAL_FACING;
    public static final EnumProperty<CrusherPart> PART = EnumProperty.of("part", CrusherPart.class);
    private static boolean removingStructure;

    public CrusherBlock(Settings settings) {
        super(settings);
        setDefaultState(getDefaultState()
            .with(FACING, Direction.NORTH)
            .with(PART, CrusherPart.BOTTOM_LEFT));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING, PART);
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
        return state.get(PART) == CrusherPart.BOTTOM_LEFT ? new CrusherBlockEntity(pos, state) : null;
    }

    @Override
    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        if (state.get(PART) != CrusherPart.BOTTOM_LEFT || type != ModBlockEntities.CRUSHER) {
            return null;
        }
        return (tickerWorld, pos, tickerState, blockEntity) ->
            CrusherBlockEntity.tick(tickerWorld, pos, tickerState, (CrusherBlockEntity) blockEntity);
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext context) {
        Direction facing = context.getHorizontalPlayerFacing().getOpposite();
        BlockPos origin = context.getBlockPos();
        World world = context.getWorld();

        for (CrusherPart part : CrusherPart.values()) {
            BlockPos partPos = getPartPos(origin, facing, part);
            if (!world.getBlockState(partPos).canReplace(context)) {
                return null;
            }
        }

        return getDefaultState().with(FACING, facing).with(PART, CrusherPart.BOTTOM_LEFT);
    }

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack) {
        super.onPlaced(world, pos, state, placer, itemStack);
        if (world.isClient() || state.get(PART) != CrusherPart.BOTTOM_LEFT) {
            return;
        }

        Direction facing = state.get(FACING);
        MultiblockStructure structure = MultiblockManager.getInstance().createStructure(pos);
        for (CrusherPart part : CrusherPart.values()) {
            BlockPos relativePos = getPartPos(BlockPos.ORIGIN, facing, part);
            BlockState partState = getDefaultState().with(FACING, facing).with(PART, part);
            structure.addBlock(relativePos, partState);
            world.setBlockState(pos.add(relativePos), partState, Block.NOTIFY_ALL);
        }

        MultiblockManager.getInstance().registerStructure(structure);
    }

    @Override
    public BlockState onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        if (!world.isClient() && !removingStructure) {
            BlockPos origin = getOriginFromState(pos, state);
            BlockEntity blockEntity = world.getBlockEntity(origin);
            if (blockEntity instanceof Inventory inventory) {
                ItemScatterer.spawn(world, origin, inventory);
                world.updateComparators(origin, this);
            }

            MultiblockManager.getInstance().unregisterStructure(origin);
            removingStructure = true;
            try {
                for (CrusherPart part : CrusherPart.values()) {
                    BlockPos partPos = getPartPos(origin, state.get(FACING), part);
                    if (!partPos.equals(pos) && world.getBlockState(partPos).isOf(this)) {
                        world.breakBlock(partPos, false, player);
                    }
                }
            } finally {
                removingStructure = false;
            }
        }

        return super.onBreak(world, pos, state, player);
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        if (!world.isClient()) {
            BlockPos origin = getOriginFromState(pos, state);
            BlockEntity blockEntity = world.getBlockEntity(origin);
            if (blockEntity instanceof CrusherBlockEntity crusher) {
                player.openHandledScreen(crusher);
            }
        }

        return ActionResult.SUCCESS;
    }

    @Override
    @Nullable
    public BlockPos getOriginPos(World world, BlockPos pos, BlockState state) {
        BlockPos registered = MultiblockManager.getInstance().getOriginPos(pos);
        return registered != null ? registered : getOriginFromState(pos, state);
    }

    @Override
    public VoxelShape getFullOutlineShape(World world, BlockPos pos, BlockState state) {
        BlockPos origin = getOriginPos(world, pos, state);
        if (origin != null) {
            MultiblockStructure structure = MultiblockManager.getInstance().getStructureByOrigin(origin);
            if (structure != null) {
                return structure.getOutlineShape();
            }
        }

        return state.getOutlineShape(world, pos);
    }

    public static BlockPos getOriginFromState(BlockPos pos, BlockState state) {
        CrusherPart part = state.get(PART);
        Direction right = state.get(FACING).rotateYCounterclockwise();
        return pos.subtract(right.getVector().multiply(part.x())).down(part.y());
    }

    public static BlockPos getPartPos(BlockPos origin, Direction facing, CrusherPart part) {
        Direction right = facing.rotateYCounterclockwise();
        return origin.add(right.getVector().multiply(part.x())).up(part.y());
    }
}
