package ru.purpir.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import ru.purpir.block.entity.LockedMinerCrateBlockEntity;
import ru.purpir.item.ModItems;

public class LockedMinerCrateBlock extends BlockWithEntity {
    public static final MapCodec<LockedMinerCrateBlock> CODEC = createCodec(LockedMinerCrateBlock::new);
    public static final EnumProperty<Direction> FACING = Properties.FACING;
    public static final BooleanProperty LOCKED = BooleanProperty.of("locked");

    public LockedMinerCrateBlock(Settings settings) {
        super(settings);
        setDefaultState(getDefaultState().with(FACING, Direction.NORTH).with(LOCKED, true));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING, LOCKED);
    }

    @Override
    protected MapCodec<? extends BlockWithEntity> getCodec() {
        return CODEC;
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext context) {
        return getDefaultState()
            .with(FACING, context.getSide().getOpposite())
            .with(LOCKED, true);
    }

    @Override
    protected BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    @Nullable
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new LockedMinerCrateBlockEntity(pos, state);
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (!(blockEntity instanceof LockedMinerCrateBlockEntity crate)) {
            return ActionResult.PASS;
        }

        if (!state.get(LOCKED)) {
            openCrate(player, crate, world);
            return ActionResult.SUCCESS;
        }

        ItemStack keyStack = findKey(player);
        if (keyStack.isEmpty()) {
            if (!world.isClient()) {
                player.sendMessage(Text.translatable("message.caveborn.locked_miner_crate.needs_key"), true);
                world.playSound(null, pos, SoundEvents.BLOCK_CHAIN_HIT, SoundCategory.BLOCKS, 0.65F, 0.75F);
            }
            return ActionResult.SUCCESS;
        }

        if (!world.isClient()) {
            if (!player.isCreative()) {
                keyStack.decrement(1);
            }
            world.setBlockState(pos, state.with(LOCKED, false), Block.NOTIFY_ALL);
            crate.fillRewardInventory();
            world.playSound(null, pos, SoundEvents.BLOCK_IRON_DOOR_OPEN, SoundCategory.BLOCKS, 0.9F, 1.15F);
            openCrate(player, crate, world);
        }

        return ActionResult.SUCCESS;
    }

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack) {
        super.onPlaced(world, pos, state, placer, itemStack);
        if (!world.isClient() && !state.get(LOCKED) && world.getBlockEntity(pos) instanceof LockedMinerCrateBlockEntity crate) {
            crate.fillRewardInventory();
        }
    }

    @Override
    public BlockState onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        if (!world.isClient() && world.getBlockEntity(pos) instanceof Inventory inventory) {
            ItemScatterer.spawn(world, pos, inventory);
            world.updateComparators(pos, this);
        }
        return super.onBreak(world, pos, state, player);
    }

    private ItemStack findKey(PlayerEntity player) {
        ItemStack mainHand = player.getMainHandStack();
        if (mainHand.isOf(ModItems.RUSTED_MINER_KEY)) {
            return mainHand;
        }

        ItemStack offHand = player.getOffHandStack();
        if (offHand.isOf(ModItems.RUSTED_MINER_KEY)) {
            return offHand;
        }

        return ItemStack.EMPTY;
    }

    private void openCrate(PlayerEntity player, LockedMinerCrateBlockEntity crate, World world) {
        if (!world.isClient() && crate instanceof NamedScreenHandlerFactory factory) {
            player.openHandledScreen(factory);
        }
    }
}
