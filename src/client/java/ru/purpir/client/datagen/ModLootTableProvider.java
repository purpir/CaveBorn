package ru.purpir.client.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricBlockLootTableProvider;
import net.minecraft.loot.LootPool;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.condition.RandomChanceLootCondition;
import net.minecraft.loot.entry.ItemEntry;
import net.minecraft.loot.provider.number.ConstantLootNumberProvider;
import net.minecraft.registry.RegistryWrapper;
import ru.purpir.block.ModBlocks;
import ru.purpir.item.ModItems;

import java.util.concurrent.CompletableFuture;

public class ModLootTableProvider extends FabricBlockLootTableProvider {
    public ModLootTableProvider(FabricDataOutput dataOutput, CompletableFuture<RegistryWrapper.WrapperLookup> registryLookup) {
        super(dataOutput, registryLookup);
    }

    @Override
    public void generate() {
        // Bronze ore drops raw bronze
        addDrop(ModBlocks.BRONZE_ORE, oreDrops(ModBlocks.BRONZE_ORE, ModItems.RAW_BRONZE));
        addDrop(ModBlocks.BRONZE_BLOCK);

        // Titanium ore drops raw titanium
        addDrop(ModBlocks.TITANIUM_ORE, oreDrops(ModBlocks.TITANIUM_ORE, ModItems.RAW_TITANIUM));
        addDrop(ModBlocks.DEEPSLATE_TITANIUM_ORE, oreDrops(ModBlocks.DEEPSLATE_TITANIUM_ORE, ModItems.RAW_TITANIUM));
        addDrop(ModBlocks.TITANIUM_BLOCK);

        addDrop(ModBlocks.NETHERITE_TITANIUM_BLOCK);

        // Vacuumite ore drops raw vacuumite
        addDrop(ModBlocks.VACUUMITE_ORE, oreDrops(ModBlocks.VACUUMITE_ORE, ModItems.RAW_VACUUMITE));
        addDrop(ModBlocks.VACUUMITE_BLOCK);
        
        // Weed loot tables are defined manually in resources/data/caveborn/loot_table/blocks/
    }
}
