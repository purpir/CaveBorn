package ru.purpir.multiblock;

import net.minecraft.block.BlockState;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Менеджер для управления мультиблочными структурами.
 * Позволяет регистрировать, создавать и отслеживать мультиструктуры.
 */
public class MultiblockManager {
    private static final MultiblockManager INSTANCE = new MultiblockManager();
    
    // Карта: позиция блока -> структура, к которой он принадлежит
    private final Map<BlockPos, MultiblockStructure> blockToStructure = new ConcurrentHashMap<>();
    
    // Карта: origin позиция -> структура
    private final Map<BlockPos, MultiblockStructure> structures = new ConcurrentHashMap<>();
    
    @Nullable
    private ServerWorld currentWorld;
    
    private MultiblockManager() {}
    
    public static MultiblockManager getInstance() {
        return INSTANCE;
    }
    
    public void setWorld(ServerWorld world) {
        this.currentWorld = world;
    }
    
    private void markDirty() {
        if (currentWorld != null) {
            MultiblockSavedData data = MultiblockSavedData.get(currentWorld);
            data.saveStructures();
        }
    }
    
    /**
     * Зарегистрировать новую мультиструктуру.
     * 
     * @param structure Структура для регистрации
     */
    public void registerStructure(MultiblockStructure structure) {
        registerStructure(structure, true);
    }
    
    /**
     * Зарегистрировать новую мультиструктуру.
     * 
     * @param structure Структура для регистрации
     * @param markDirty Нужно ли помечать данные как изменённые
     */
    public void registerStructure(MultiblockStructure structure, boolean markDirty) {
        BlockPos origin = structure.getOrigin();
        structures.put(origin, structure);
        
        // Зарегистрировать все блоки структуры
        for (BlockPos relativePos : structure.getBlocks().keySet()) {
            BlockPos absolutePos = structure.getAbsolutePos(relativePos);
            blockToStructure.put(absolutePos, structure);
        }
        
        if (markDirty) {
            markDirty();
        }
    }
    
    /**
     * Удалить структуру из менеджера.
     * 
     * @param origin Позиция origin структуры
     */
    public void unregisterStructure(BlockPos origin) {
        MultiblockStructure structure = structures.remove(origin);
        if (structure != null) {
            // Удалить все блоки из карты
            for (BlockPos relativePos : structure.getBlocks().keySet()) {
                BlockPos absolutePos = structure.getAbsolutePos(relativePos);
                blockToStructure.remove(absolutePos);
            }
            
            markDirty();
        }
    }
    
    /**
     * Получить структуру по позиции любого её блока.
     * 
     * @param pos Позиция блока
     * @return Структура или null
     */
    @Nullable
    public MultiblockStructure getStructureAt(BlockPos pos) {
        return blockToStructure.get(pos);
    }
    
    /**
     * Получить структуру по origin позиции.
     * 
     * @param origin Origin позиция
     * @return Структура или null
     */
    @Nullable
    public MultiblockStructure getStructureByOrigin(BlockPos origin) {
        return structures.get(origin);
    }
    
    /**
     * Получить origin позицию для блока.
     * 
     * @param pos Позиция блока
     * @return Origin позиция или null
     */
    @Nullable
    public BlockPos getOriginPos(BlockPos pos) {
        MultiblockStructure structure = getStructureAt(pos);
        return structure != null ? structure.getOrigin() : null;
    }
    
    /**
     * Получить VoxelShape для структуры, содержащей данный блок.
     * 
     * @param pos Позиция блока
     * @return VoxelShape или null
     */
    @Nullable
    public VoxelShape getOutlineShape(BlockPos pos) {
        MultiblockStructure structure = getStructureAt(pos);
        return structure != null ? structure.getOutlineShape() : null;
    }
    
    /**
     * Проверить, является ли блок частью мультиструктуры.
     * 
     * @param pos Позиция блока
     * @return true, если блок является частью структуры
     */
    public boolean isPartOfStructure(BlockPos pos) {
        return blockToStructure.containsKey(pos);
    }
    
    /**
     * Проверить, является ли блок origin'ом структуры.
     * 
     * @param pos Позиция блока
     * @return true, если это origin
     */
    public boolean isOrigin(BlockPos pos) {
        return structures.containsKey(pos);
    }
    
    /**
     * Создать новую структуру.
     * 
     * @param origin Origin позиция
     * @return Новая структура
     */
    public MultiblockStructure createStructure(BlockPos origin) {
        return new MultiblockStructure(origin);
    }
    
    /**
     * Очистить все структуры (для очистки при выходе из мира).
     */
    public void clear() {
        blockToStructure.clear();
        structures.clear();
    }
    
    /**
     * Получить все зарегистрированные структуры.
     */
    public Collection<MultiblockStructure> getAllStructures() {
        return Collections.unmodifiableCollection(structures.values());
    }
}
