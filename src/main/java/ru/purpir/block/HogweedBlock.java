package ru.purpir.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.block.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCollisionHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import ru.purpir.item.ModItems;

/**
 * Борщевик - высокое растение от 2 до 8 блоков
 * Спавнится сразу высоким, НЕ растёт
 * Нижние и средние блоки - стебель
 * Верхний блок - зонтик
 */
public class HogweedBlock extends PlantBlock {
    public static final MapCodec<HogweedBlock> CODEC = createCodec(HogweedBlock::new);
    
    // true = это верхушка (зонтик), false = стебель
    public static final BooleanProperty TOP = BooleanProperty.of("top");
    public static final BooleanProperty DRAINED = BooleanProperty.of("drained");
    
    private static final VoxelShape SHAPE = Block.createCuboidShape(4, 0, 4, 12, 16, 12);
    
    public HogweedBlock(Settings settings) {
        super(settings);
        setDefaultState(getStateManager().getDefaultState().with(TOP, true).with(DRAINED, false));
    }
    
    @Override
    protected MapCodec<? extends PlantBlock> getCodec() {
        return CODEC;
    }
    
    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(TOP, DRAINED);
    }
    
    @Override
    protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPE;
    }

    @Override
    protected void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity, EntityCollisionHandler handler, boolean initialCollision) {
        if (entity instanceof PlayerEntity) {
            entity.slowMovement(state, new Vec3d(0.75D, 1.0D, 0.75D));
        }
    }

    @Override
    protected ActionResult onUseWithItem(ItemStack stack, BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (!stack.isOf(Items.GLASS_BOTTLE) || state.get(DRAINED)) {
            return ActionResult.PASS;
        }

        if (!world.isClient()) {
            ItemStack juice = new ItemStack(ModItems.HOGWEED_JUICE);
            if (!player.isCreative()) {
                stack.decrement(1);
            }

            if (!player.getInventory().insertStack(juice)) {
                player.dropItem(juice, false);
            }

            world.setBlockState(pos, state.with(DRAINED, true), Block.NOTIFY_ALL);
            world.playSound(null, pos, SoundEvents.ITEM_BOTTLE_FILL, SoundCategory.BLOCKS, 0.8F, 0.9F);
        }

        return ActionResult.SUCCESS;
    }
    
    @Override
    protected boolean canPlantOnTop(BlockState floor, BlockView world, BlockPos pos) {
        return floor.isOf(Blocks.GRASS_BLOCK) || floor.isOf(Blocks.DIRT) || 
               floor.isOf(Blocks.PODZOL) || floor.isOf(Blocks.COARSE_DIRT) ||
               floor.isOf(ModBlocks.HOGWEED); // Может стоять на себе (стебель)
    }
    
    @Override
    public boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
        BlockPos below = pos.down();
        BlockState belowState = world.getBlockState(below);
        return canPlantOnTop(belowState, world, below);
    }
    
    @Override
    public BlockState onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        // Когда ломаем любую часть - всё что выше падает
        if (!world.isClient()) {
            BlockPos above = pos.up();
            BlockState aboveState = world.getBlockState(above);
            if (aboveState.isOf(ModBlocks.HOGWEED)) {
                world.breakBlock(above, true);
            }
        }
        return super.onBreak(world, pos, state, player);
    }
}
