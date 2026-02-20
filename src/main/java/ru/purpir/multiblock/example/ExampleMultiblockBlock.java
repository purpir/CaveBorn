package ru.purpir.multiblock.example;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import ru.purpir.multiblock.IMultiblock;
import ru.purpir.multiblock.MultiblockManager;
import ru.purpir.multiblock.MultiblockStructure;
import net.minecraft.util.shape.VoxelShape;
import org.jetbrains.annotations.Nullable;

/**
 * Пример блока, который создаёт мультиструктуру 2x2x2 в виде большого бревна.
 * Внешние блоки - кора дуба, внутренние - древесина дуба.
 */
public class ExampleMultiblockBlock extends Block implements IMultiblock {
    
    public ExampleMultiblockBlock(Settings settings) {
        super(settings);
    }
    
    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack) {
        super.onPlaced(world, pos, state, placer, itemStack);
        
        if (!world.isClient()) {
            // Создаём новую структуру 2x2x2
            MultiblockStructure structure = MultiblockManager.getInstance().createStructure(pos);
            
            // Добавляем блоки в структуру (относительные координаты)
            for (int x = 0; x < 2; x++) {
                for (int y = 0; y < 2; y++) {
                    for (int z = 0; z < 2; z++) {
                        BlockPos relativePos = new BlockPos(x, y, z);
                        
                        // Определяем, является ли блок внешним (кора) или внутренним (древесина)
                        boolean isOuter = x == 0 || x == 1 || z == 0 || z == 1;
                        
                        BlockState blockState;
                        if (isOuter) {
                            // Внешние блоки - кора дуба (oak_wood)
                            blockState = net.minecraft.block.Blocks.OAK_WOOD.getDefaultState();
                        } else {
                            // Внутренние блоки - древесина дуба (stripped_oak_wood)
                            blockState = net.minecraft.block.Blocks.STRIPPED_OAK_WOOD.getDefaultState();
                        }
                        
                        structure.addBlock(relativePos, blockState);
                    }
                }
            }
            
            // Размещаем структуру в мире
            structure.place(world);
            
            // Регистрируем структуру в менеджере
            MultiblockManager.getInstance().registerStructure(structure);
            
            if (placer instanceof PlayerEntity player) {
                player.sendMessage(Text.literal("Большое бревно 2x2x2 создано!"), true);
            }
        }
    }
    
    @Override
    @Nullable
    public BlockPos getOriginPos(World world, BlockPos pos, BlockState state) {
        return MultiblockManager.getInstance().getOriginPos(pos);
    }
    
    @Override
    public VoxelShape getFullOutlineShape(World world, BlockPos pos, BlockState state) {
        MultiblockStructure structure = MultiblockManager.getInstance().getStructureByOrigin(pos);
        if (structure != null) {
            return structure.getOutlineShape();
        }
        return state.getOutlineShape(world, pos);
    }
}
