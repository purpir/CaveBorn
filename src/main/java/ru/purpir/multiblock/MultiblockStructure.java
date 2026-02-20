package ru.purpir.multiblock;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.World;

import java.util.*;

/**
 * Класс для описания мультиблочной структуры.
 */
public class MultiblockStructure {
    private final BlockPos origin;
    private final Map<BlockPos, BlockState> blocks;
    private final Box boundingBox;
    private VoxelShape cachedOutlineShape;
    
    public MultiblockStructure(BlockPos origin) {
        this.origin = origin;
        this.blocks = new HashMap<>();
        this.boundingBox = new Box(origin);
    }
    
    /**
     * Добавить блок в структуру.
     * 
     * @param relativePos Относительная позиция от origin
     * @param state Состояние блока
     */
    public void addBlock(BlockPos relativePos, BlockState state) {
        blocks.put(relativePos, state);
        cachedOutlineShape = null; // Сбросить кэш
    }
    
    /**
     * Получить абсолютную позицию блока в мире.
     */
    public BlockPos getAbsolutePos(BlockPos relativePos) {
        return origin.add(relativePos);
    }
    
    /**
     * Получить относительную позицию от origin.
     */
    public BlockPos getRelativePos(BlockPos absolutePos) {
        return absolutePos.subtract(origin);
    }
    
    /**
     * Проверить, содержит ли структура блок на данной позиции.
     */
    public boolean containsBlock(BlockPos absolutePos) {
        BlockPos relative = getRelativePos(absolutePos);
        return blocks.containsKey(relative);
    }
    
    /**
     * Получить состояние блока по относительной позиции.
     */
    public BlockState getBlockState(BlockPos relativePos) {
        return blocks.get(relativePos);
    }
    
    /**
     * Получить все блоки структуры.
     */
    public Map<BlockPos, BlockState> getBlocks() {
        return Collections.unmodifiableMap(blocks);
    }
    
    /**
     * Получить позицию origin.
     */
    public BlockPos getOrigin() {
        return origin;
    }
    
    /**
     * Получить VoxelShape для всей структуры (для outline).
     */
    public VoxelShape getOutlineShape() {
        if (cachedOutlineShape == null) {
            cachedOutlineShape = calculateOutlineShape();
        }
        return cachedOutlineShape;
    }
    
    private VoxelShape calculateOutlineShape() {
        if (blocks.isEmpty()) {
            return VoxelShapes.empty();
        }
        
        // Найти границы структуры
        int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE, minZ = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE, maxY = Integer.MIN_VALUE, maxZ = Integer.MIN_VALUE;
        
        for (BlockPos pos : blocks.keySet()) {
            minX = Math.min(minX, pos.getX());
            minY = Math.min(minY, pos.getY());
            minZ = Math.min(minZ, pos.getZ());
            maxX = Math.max(maxX, pos.getX() + 1);
            maxY = Math.max(maxY, pos.getY() + 1);
            maxZ = Math.max(maxZ, pos.getZ() + 1);
        }
        
        // Создать box для всей структуры
        return VoxelShapes.cuboid(minX, minY, minZ, maxX, maxY, maxZ);
    }
    
    /**
     * Разместить структуру в мире.
     */
    public void place(World world) {
        for (Map.Entry<BlockPos, BlockState> entry : blocks.entrySet()) {
            BlockPos absolutePos = getAbsolutePos(entry.getKey());
            world.setBlockState(absolutePos, entry.getValue());
        }
    }
    
    /**
     * Удалить структуру из мира с дропом предметов.
     */
    public void remove(World world, boolean dropItems) {
        for (BlockPos relativePos : blocks.keySet()) {
            BlockPos absolutePos = getAbsolutePos(relativePos);
            world.breakBlock(absolutePos, dropItems);
        }
    }
}
