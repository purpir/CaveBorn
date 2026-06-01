package ru.purpir.entity;

import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.SpawnLocationTypes;
import net.minecraft.entity.SpawnRestriction;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.world.Heightmap;
import ru.purpir.Caveborn;

public class ModEntities {
    public static final EntityType<CaveFireflyEntity> CAVE_FIREFLY = register("cave_firefly",
        EntityType.Builder.create(CaveFireflyEntity::new, SpawnGroup.AMBIENT)
            .dimensions(0.25f, 0.25f)
            .maxTrackingRange(8)
            .trackingTickInterval(3));

    private static <T extends net.minecraft.entity.Entity> EntityType<T> register(String name, EntityType.Builder<T> builder) {
        Identifier id = Identifier.of(Caveborn.MOD_ID, name);
        RegistryKey<EntityType<?>> key = RegistryKey.of(RegistryKeys.ENTITY_TYPE, id);
        return Registry.register(Registries.ENTITY_TYPE, id, builder.build(key));
    }

    public static void register() {
        FabricDefaultAttributeRegistry.register(CAVE_FIREFLY, CaveFireflyEntity.createAttributes());
        SpawnRestriction.register(
            CAVE_FIREFLY,
            SpawnLocationTypes.UNRESTRICTED,
            Heightmap.Type.MOTION_BLOCKING_NO_LEAVES,
            CaveFireflyEntity::canSpawn
        );
        BiomeModifications.addSpawn(
            BiomeSelectors.foundInOverworld(),
            SpawnGroup.AMBIENT,
            CAVE_FIREFLY,
            55,
            3,
            7
        );

        Caveborn.LOGGER.info("Registering Entities for " + Caveborn.MOD_ID);
    }
}
