package ru.purpir.block.entity;

import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import ru.purpir.Caveborn;
import ru.purpir.block.ModBlocks;

public class ModBlockEntities {
    public static final BlockEntityType<CrusherBlockEntity> CRUSHER = Registry.register(
        Registries.BLOCK_ENTITY_TYPE,
        Identifier.of(Caveborn.MOD_ID, "crusher"),
        FabricBlockEntityTypeBuilder.create(CrusherBlockEntity::new, ModBlocks.CRUSHER).build()
    );

    public static final BlockEntityType<LockedMinerCrateBlockEntity> LOCKED_MINER_CRATE = Registry.register(
        Registries.BLOCK_ENTITY_TYPE,
        Identifier.of(Caveborn.MOD_ID, "locked_miner_crate"),
        FabricBlockEntityTypeBuilder.create(LockedMinerCrateBlockEntity::new, ModBlocks.LOCKED_MINER_CRATE).build()
    );

    public static void register() {
        Caveborn.LOGGER.info("Registering Block Entities for " + Caveborn.MOD_ID);
    }
}
