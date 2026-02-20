package ru.purpir.multiblock;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

/**
 * Интерфейс для блоков, которые являются частью мультиструктуры.
 */
public interface IMultiblock {
    
    /**
     * Получить позицию главного блока (origin) мультиструктуры.
     * 
     * @param world Мир
     * @param pos Позиция текущего блока
     * @param state Состояние текущего блока
     * @return Позиция главного блока или null, если структура не сформирована
     */
    @Nullable
    BlockPos getOriginPos(World world, BlockPos pos, BlockState state);
    
    /**
     * Получить полную форму outline для всей мультиструктуры.
     * 
     * @param world Мир
     * @param pos Позиция главного блока
     * @param state Состояние главного блока
     * @return VoxelShape для всей структуры
     */
    VoxelShape getFullOutlineShape(World world, BlockPos pos, BlockState state);
    
    /**
     * Проверить, является ли этот блок главным в структуре.
     * 
     * @param world Мир
     * @param pos Позиция блока
     * @param state Состояние блока
     * @return true, если это главный блок
     */
    default boolean isOrigin(World world, BlockPos pos, BlockState state) {
        BlockPos origin = getOriginPos(world, pos, state);
        return origin != null && origin.equals(pos);
    }
}
