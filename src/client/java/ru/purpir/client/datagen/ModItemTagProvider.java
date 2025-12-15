package ru.purpir.client.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.tag.ItemTags;
import ru.purpir.item.ModItems;
import ru.purpir.item.ModTags;

import java.util.concurrent.CompletableFuture;

public class ModItemTagProvider extends FabricTagProvider.ItemTagProvider {
    public ModItemTagProvider(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
        super(output, registriesFuture);
    }

    @Override
    protected void configure(RegistryWrapper.WrapperLookup wrapperLookup) {
        valueLookupBuilder(ModTags.Items.BRONZE_INGOTS)
            .add(ModItems.BRONZE_INGOT);
        
        valueLookupBuilder(ModTags.Items.TITANIUM_INGOTS)
            .add(ModItems.TITANIUM_INGOT);

        valueLookupBuilder(ModTags.Items.BRONZE_REPAIR)
            .add(ModItems.BRONZE_INGOT);

        valueLookupBuilder(ModTags.Items.NETHERITE_TITANIUM_REPAIR)
            .add(ModItems.NETHERITE_TITANIUM_INGOT);

        // Armor trims support
        valueLookupBuilder(ItemTags.TRIMMABLE_ARMOR)
            .add(ModItems.BRONZE_HELMET)
            .add(ModItems.BRONZE_CHESTPLATE)
            .add(ModItems.BRONZE_LEGGINGS)
            .add(ModItems.BRONZE_BOOTS)
            .add(ModItems.NETHERITE_TITANIUM_HELMET)
            .add(ModItems.NETHERITE_TITANIUM_CHESTPLATE)
            .add(ModItems.NETHERITE_TITANIUM_LEGGINGS)
            .add(ModItems.NETHERITE_TITANIUM_BOOTS);

        // Hoes - чтобы работало вспахивание
        valueLookupBuilder(ItemTags.HOES)
            .add(ModItems.BRONZE_HOE)
            .add(ModItems.NETHERITE_TITANIUM_HOE);
    }
}
