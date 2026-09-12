package ru.purpir.screen;

import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.resource.featuretoggle.FeatureFlags;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import ru.purpir.Caveborn;

public class ModScreenHandlers {
    public static final ScreenHandlerType<BagScreenHandler> BAG_SCREEN_HANDLER = 
        Registry.register(Registries.SCREEN_HANDLER, Identifier.of(Caveborn.MOD_ID, "bag"),
            new ScreenHandlerType<>((syncId, playerInventory) -> 
                new BagScreenHandler(syncId, playerInventory, playerInventory.player.getMainHandStack()), 
                FeatureFlags.VANILLA_FEATURES));

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static final ExtendedScreenHandlerType<CrusherScreenHandler, BlockPos> CRUSHER_SCREEN_HANDLER =
        Registry.register(Registries.SCREEN_HANDLER, Identifier.of(Caveborn.MOD_ID, "crusher"),
            new ExtendedScreenHandlerType<CrusherScreenHandler, BlockPos>(
                (syncId, playerInventory, pos) -> new CrusherScreenHandler(syncId, playerInventory, pos),
                (PacketCodec) BlockPos.PACKET_CODEC));

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static final ExtendedScreenHandlerType<AutoSpawnerScreenHandler, BlockPos> AUTO_SPAWNER_SCREEN_HANDLER =
        Registry.register(Registries.SCREEN_HANDLER, Identifier.of(Caveborn.MOD_ID, "automatic_spawner"),
            new ExtendedScreenHandlerType<AutoSpawnerScreenHandler, BlockPos>((syncId, inv, pos) -> new AutoSpawnerScreenHandler(syncId, inv, pos), (PacketCodec) BlockPos.PACKET_CODEC));
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static final ExtendedScreenHandlerType<AutoSpawnerStorageScreenHandler, BlockPos> AUTO_SPAWNER_STORAGE_SCREEN_HANDLER =
        Registry.register(Registries.SCREEN_HANDLER, Identifier.of(Caveborn.MOD_ID, "automatic_spawner_storage"),
            new ExtendedScreenHandlerType<AutoSpawnerStorageScreenHandler, BlockPos>((syncId, inv, pos) -> new AutoSpawnerStorageScreenHandler(syncId, inv, pos), (PacketCodec) BlockPos.PACKET_CODEC));

    public static void register() {
        Caveborn.LOGGER.info("Registering Screen Handlers for " + Caveborn.MOD_ID);
    }
}
