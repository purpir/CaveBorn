package ru.purpir.client.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.tag.BlockTags;
import ru.purpir.block.ModBlocks;

import java.util.concurrent.CompletableFuture;

public class ModBlockTagProvider extends FabricTagProvider.BlockTagProvider {
    public ModBlockTagProvider(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
        super(output, registriesFuture);
    }

    @Override
    protected void configure(RegistryWrapper.WrapperLookup wrapperLookup) {
        valueLookupBuilder(BlockTags.PICKAXE_MINEABLE)
            .add(ModBlocks.BRONZE_ORE)
            .add(ModBlocks.BRONZE_BLOCK)
            .add(ModBlocks.TITANIUM_ORE)
            .add(ModBlocks.DEEPSLATE_TITANIUM_ORE)
            .add(ModBlocks.TITANIUM_BLOCK)
            .add(ModBlocks.NETHERITE_TITANIUM_BLOCK)
            .add(ModBlocks.VACUUMITE_ORE)
            .add(ModBlocks.VACUUMITE_BLOCK)
            .add(ModBlocks.TITANIUM_GRATE)
            .add(ModBlocks.TITANIUM_STAIRS)
            .add(ModBlocks.TITANIUM_SLAB)
            .add(ModBlocks.TITANIUM_BARS)
            .add(ModBlocks.TITANIUM_DOOR)
            .add(ModBlocks.TITANIUM_TRAPDOOR);

        valueLookupBuilder(BlockTags.INCORRECT_FOR_WOODEN_TOOL)
            .add(ModBlocks.BRONZE_ORE)
            .add(ModBlocks.BRONZE_BLOCK)
            .add(ModBlocks.TITANIUM_ORE)
            .add(ModBlocks.DEEPSLATE_TITANIUM_ORE)
            .add(ModBlocks.TITANIUM_BLOCK)
            .add(ModBlocks.NETHERITE_TITANIUM_BLOCK)
            .add(ModBlocks.TITANIUM_GRATE)
            .add(ModBlocks.TITANIUM_STAIRS)
            .add(ModBlocks.TITANIUM_SLAB)
            .add(ModBlocks.TITANIUM_BARS)
            .add(ModBlocks.TITANIUM_DOOR)
            .add(ModBlocks.TITANIUM_TRAPDOOR);

        valueLookupBuilder(BlockTags.INCORRECT_FOR_STONE_TOOL)
            .add(ModBlocks.BRONZE_ORE)
            .add(ModBlocks.BRONZE_BLOCK)
            .add(ModBlocks.TITANIUM_ORE)
            .add(ModBlocks.DEEPSLATE_TITANIUM_ORE)
            .add(ModBlocks.TITANIUM_BLOCK)
            .add(ModBlocks.NETHERITE_TITANIUM_BLOCK)
            .add(ModBlocks.TITANIUM_GRATE)
            .add(ModBlocks.TITANIUM_STAIRS)
            .add(ModBlocks.TITANIUM_SLAB)
            .add(ModBlocks.TITANIUM_BARS)
            .add(ModBlocks.TITANIUM_DOOR)
            .add(ModBlocks.TITANIUM_TRAPDOOR);

        valueLookupBuilder(BlockTags.INCORRECT_FOR_GOLD_TOOL)
            .add(ModBlocks.BRONZE_ORE)
            .add(ModBlocks.BRONZE_BLOCK)
            .add(ModBlocks.TITANIUM_ORE)
            .add(ModBlocks.DEEPSLATE_TITANIUM_ORE)
            .add(ModBlocks.TITANIUM_BLOCK)
            .add(ModBlocks.NETHERITE_TITANIUM_BLOCK)
            .add(ModBlocks.TITANIUM_GRATE)
            .add(ModBlocks.TITANIUM_STAIRS)
            .add(ModBlocks.TITANIUM_SLAB)
            .add(ModBlocks.TITANIUM_BARS)
            .add(ModBlocks.TITANIUM_DOOR)
            .add(ModBlocks.TITANIUM_TRAPDOOR);

        valueLookupBuilder(BlockTags.INCORRECT_FOR_IRON_TOOL)
            .add(ModBlocks.TITANIUM_ORE)
            .add(ModBlocks.DEEPSLATE_TITANIUM_ORE)
            .add(ModBlocks.TITANIUM_BLOCK)
            .add(ModBlocks.NETHERITE_TITANIUM_BLOCK)
            .add(ModBlocks.TITANIUM_GRATE)
            .add(ModBlocks.TITANIUM_STAIRS)
            .add(ModBlocks.TITANIUM_SLAB)
            .add(ModBlocks.TITANIUM_BARS)
            .add(ModBlocks.TITANIUM_DOOR)
            .add(ModBlocks.TITANIUM_TRAPDOOR);

        valueLookupBuilder(BlockTags.INCORRECT_FOR_DIAMOND_TOOL)
            .add(ModBlocks.TITANIUM_ORE)
            .add(ModBlocks.DEEPSLATE_TITANIUM_ORE)
            .add(ModBlocks.TITANIUM_BLOCK)
            .add(ModBlocks.NETHERITE_TITANIUM_BLOCK)
            .add(ModBlocks.TITANIUM_GRATE)
            .add(ModBlocks.TITANIUM_STAIRS)
            .add(ModBlocks.TITANIUM_SLAB)
            .add(ModBlocks.TITANIUM_BARS)
            .add(ModBlocks.TITANIUM_DOOR)
            .add(ModBlocks.TITANIUM_TRAPDOOR);

        // Door and trapdoor tags
        valueLookupBuilder(BlockTags.DOORS)
            .add(ModBlocks.TITANIUM_DOOR);
        
        valueLookupBuilder(BlockTags.TRAPDOORS)
            .add(ModBlocks.TITANIUM_TRAPDOOR);

        valueLookupBuilder(BlockTags.SLABS)
            .add(ModBlocks.TITANIUM_SLAB);

        valueLookupBuilder(BlockTags.STAIRS)
            .add(ModBlocks.TITANIUM_STAIRS);
    }
}
