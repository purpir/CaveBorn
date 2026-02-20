package ru.purpir.multiblock;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.block.Blocks;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateType;

import java.util.ArrayList;
import java.util.List;

public class MultiblockSavedData extends PersistentState {
    private static final String NAME = "caveborn_multiblocks";
    
    // Структура для сохранения одной мультиструктуры
    public record SavedStructure(BlockPos origin, List<BlockPos> relativePositions) {
        public static final Codec<SavedStructure> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                BlockPos.CODEC.fieldOf("origin").forGetter(SavedStructure::origin),
                BlockPos.CODEC.listOf().fieldOf("blocks").forGetter(SavedStructure::relativePositions)
            ).apply(instance, SavedStructure::new)
        );
    }
    
    private List<SavedStructure> structures = new ArrayList<>();
    
    public static final Codec<MultiblockSavedData> CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
            SavedStructure.CODEC.listOf().fieldOf("structures").forGetter(data -> data.structures)
        ).apply(instance, MultiblockSavedData::new)
    );
    
    public static final PersistentStateType<MultiblockSavedData> TYPE = new PersistentStateType<>(
        NAME,
        (context) -> new MultiblockSavedData(),
        (context) -> CODEC,
        null // DataFixTypes - null для новых данных без миграции
    );
    
    public MultiblockSavedData() {
        this.structures = new ArrayList<>();
    }
    
    public MultiblockSavedData(List<SavedStructure> structures) {
        this.structures = structures;
    }
    
    public static MultiblockSavedData get(ServerWorld world) {
        MultiblockSavedData data = world.getPersistentStateManager().getOrCreate(TYPE);
        data.loadStructures(world);
        return data;
    }
    
    public void saveStructures() {
        // Создаем новый список вместо очистки старого (может быть immutable после загрузки)
        structures = new ArrayList<>();
        
        // Собираем все структуры из менеджера
        for (MultiblockStructure structure : MultiblockManager.getInstance().getAllStructures()) {
            List<BlockPos> relativePositions = new ArrayList<>(structure.getBlocks().keySet());
            structures.add(new SavedStructure(structure.getOrigin(), relativePositions));
        }
        
        this.markDirty();
    }
    
    private void loadStructures(ServerWorld world) {
        MultiblockManager manager = MultiblockManager.getInstance();
        manager.setWorld(world);
        
        // Восстанавливаем структуры из сохранённых данных
        for (SavedStructure saved : structures) {
            MultiblockStructure structure = new MultiblockStructure(saved.origin());
            
            // Восстанавливаем блоки (используем дубовые блоки)
            for (BlockPos relativePos : saved.relativePositions()) {
                structure.addBlock(relativePos, Blocks.OAK_WOOD.getDefaultState());
            }
            
            // Не помечаем как dirty при загрузке, чтобы избежать бесконечной рекурсии
            manager.registerStructure(structure, false);
        }
    }
}
