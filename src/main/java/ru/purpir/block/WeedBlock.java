package ru.purpir.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.block.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.IntProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;

import java.util.ArrayList;
import java.util.List;

public class WeedBlock extends PlantBlock {
    public static final MapCodec<WeedBlock> CODEC = createCodec(WeedBlock::new);
    public static final IntProperty AGE = IntProperty.of("age", 0, 1); // 0 = маленький, 1 = большой (2 блока)
    
    private static final VoxelShape SMALL_SHAPE = Block.createCuboidShape(2, 0, 2, 14, 12, 14);
    private static final VoxelShape TALL_SHAPE = Block.createCuboidShape(2, 0, 2, 14, 16, 14);
    
    // Шанс появления сорняка на грядке (1/15 за проверку)
    private static final int SPAWN_CHANCE = 15;
    // Шанс роста сорняка (1/20 за random tick)
    private static final int GROW_CHANCE = 20;
    // Порог для распространения на обычную землю
    private static final int SPREAD_TO_DIRT_THRESHOLD = 20;
    // Шанс распространения на обычную землю (1/300 за тик)
    private static final int SPREAD_TO_DIRT_CHANCE = 300;
    
    public WeedBlock(Settings settings) {
        super(settings);
        setDefaultState(getStateManager().getDefaultState().with(AGE, 0));
    }
    
    @Override
    protected MapCodec<? extends PlantBlock> getCodec() {
        return CODEC;
    }
    
    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(AGE);
    }
    
    @Override
    protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return state.get(AGE) == 0 ? SMALL_SHAPE : TALL_SHAPE;
    }
    
    @Override
    protected boolean canPlantOnTop(BlockState floor, BlockView world, BlockPos pos) {
        return floor.isOf(Blocks.FARMLAND) || floor.isOf(Blocks.DIRT) || floor.isOf(Blocks.GRASS_BLOCK);
    }
    
    @Override
    public boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
        BlockPos below = pos.down();
        return canPlantOnTop(world.getBlockState(below), world, below);
    }

    @Override
    protected boolean hasRandomTicks(BlockState state) {
        return true;
    }
    
    @Override
    protected void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        int age = state.get(AGE);
        
        if (age == 0) {
            // Маленький сорняк - шанс вырасти
            if (random.nextInt(GROW_CHANCE) == 0) {
                // Проверяем есть ли место сверху
                BlockPos above = pos.up();
                if (world.isAir(above)) {
                    world.setBlockState(pos, state.with(AGE, 1));
                    world.setBlockState(above, ModBlocks.WEED_TOP.getDefaultState());
                }
            }
        } else {
            // Большой сорняк (2 блока) - заражает соседние грядки
            spreadToNeighbors(world, pos, random);
            
            // Проверяем количество сорняков рядом для распространения на обычную землю
            int nearbyWeeds = countNearbyWeeds(world, pos);
            if (nearbyWeeds >= SPREAD_TO_DIRT_THRESHOLD) {
                if (random.nextInt(SPREAD_TO_DIRT_CHANCE) == 0) {
                    spreadToDirt(world, pos, random);
                }
            }
        }
    }
    
    private void spreadToNeighbors(ServerWorld world, BlockPos pos, Random random) {
        // Выбираем 2 случайные стороны из 4
        List<Direction> horizontalDirs = new ArrayList<>();
        horizontalDirs.add(Direction.NORTH);
        horizontalDirs.add(Direction.SOUTH);
        horizontalDirs.add(Direction.EAST);
        horizontalDirs.add(Direction.WEST);
        shuffleDirections(horizontalDirs, random);
        
        int spreadCount = 0;
        for (Direction dir : horizontalDirs) {
            if (spreadCount >= 2) break;
            
            BlockPos targetPos = pos.offset(dir);
            BlockPos belowTarget = targetPos.down();
            BlockState belowState = world.getBlockState(belowTarget);
            
            // Заражаем только грядки (farmland)
            if (belowState.isOf(Blocks.FARMLAND) && world.isAir(targetPos)) {
                if (random.nextInt(50) == 0) { // Дополнительный шанс
                    world.setBlockState(targetPos, ModBlocks.WEED.getDefaultState());
                    spreadCount++;
                }
            }
        }
    }
    
    private static void shuffleDirections(List<Direction> list, Random random) {
        for (int i = list.size() - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            Direction temp = list.get(i);
            list.set(i, list.get(j));
            list.set(j, temp);
        }
    }
    
    private void spreadToDirt(ServerWorld world, BlockPos pos, Random random) {
        List<Direction> horizontalDirs = new ArrayList<>();
        horizontalDirs.add(Direction.NORTH);
        horizontalDirs.add(Direction.SOUTH);
        horizontalDirs.add(Direction.EAST);
        horizontalDirs.add(Direction.WEST);
        shuffleDirections(horizontalDirs, random);
        
        for (Direction dir : horizontalDirs) {
            BlockPos targetPos = pos.offset(dir);
            BlockPos belowTarget = targetPos.down();
            BlockState belowState = world.getBlockState(belowTarget);
            
            // Распространяемся на обычную землю или траву
            if ((belowState.isOf(Blocks.DIRT) || belowState.isOf(Blocks.GRASS_BLOCK)) 
                    && world.isAir(targetPos)) {
                world.setBlockState(targetPos, ModBlocks.WEED.getDefaultState());
                break;
            }
        }
    }
    
    private int countNearbyWeeds(ServerWorld world, BlockPos center) {
        int count = 0;
        for (int x = -3; x <= 3; x++) {
            for (int z = -3; z <= 3; z++) {
                for (int y = -1; y <= 2; y++) {
                    BlockPos checkPos = center.add(x, y, z);
                    BlockState state = world.getBlockState(checkPos);
                    if (state.isOf(ModBlocks.WEED) || state.isOf(ModBlocks.WEED_TOP)) {
                        count++;
                    }
                }
            }
        }
        return count;
    }
    
    @Override
    public BlockState onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        // Если сорняк большой, убираем верхнюю часть
        if (state.get(AGE) == 1) {
            BlockPos above = pos.up();
            if (world.getBlockState(above).isOf(ModBlocks.WEED_TOP)) {
                world.breakBlock(above, false);
            }
        }
        return super.onBreak(world, pos, state, player);
    }
    
    // Статический метод для спавна сорняков на грядках
    public static void trySpawnOnFarmland(ServerWorld world, BlockPos farmlandPos, Random random) {
        if (random.nextInt(SPAWN_CHANCE) == 0) {
            BlockPos above = farmlandPos.up();
            BlockState aboveState = world.getBlockState(above);
            
            // Спавним если там пусто
            if (world.isAir(above)) {
                world.setBlockState(above, ModBlocks.WEED.getDefaultState());
                return;
            }
            
            // Если там культура - ищем свободное место рядом на грядке
            if (aboveState.isIn(BlockTags.CROPS)) {
                for (Direction dir : Direction.Type.HORIZONTAL) {
                    BlockPos sidePos = above.offset(dir);
                    BlockPos belowSide = sidePos.down();
                    if (world.isAir(sidePos) && world.getBlockState(belowSide).isOf(Blocks.FARMLAND)) {
                        world.setBlockState(sidePos, ModBlocks.WEED.getDefaultState());
                        return;
                    }
                }
            }
        }
    }
}
