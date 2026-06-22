package ru.purpir.network;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import ru.purpir.Caveborn;

public class ModPackets {
    
    public static final Identifier OPEN_ALTAR_SCREEN = Identifier.of(Caveborn.MOD_ID, "open_altar_screen");
    public static final Identifier SUBMIT_ALTAR_TASK = Identifier.of(Caveborn.MOD_ID, "submit_altar_task");
    public static final Identifier ALTAR_ACTION = Identifier.of(Caveborn.MOD_ID, "altar_action");
    public static final Identifier ALTAR_SCENE = Identifier.of(Caveborn.MOD_ID, "altar_scene");
    public static final Identifier SOLAR_POINTS = Identifier.of(Caveborn.MOD_ID, "solar_points");
    public static final Identifier BRONZE_AXE_DOUBLE_JUMP = Identifier.of(Caveborn.MOD_ID, "bronze_axe_double_jump");
    public static final Identifier ROOT_BINDING_CHAINS = Identifier.of(Caveborn.MOD_ID, "root_binding_chains");
    
    public record OpenAltarScreenPayload(String data) implements CustomPayload {
        public static final Id<OpenAltarScreenPayload> ID = new Id<>(OPEN_ALTAR_SCREEN);
        public static final PacketCodec<RegistryByteBuf, OpenAltarScreenPayload> CODEC = PacketCodec.tuple(
            PacketCodecs.STRING, OpenAltarScreenPayload::data,
            OpenAltarScreenPayload::new
        );
        
        @Override
        public Id<? extends CustomPayload> getId() {
            return ID;
        }
    }

    public record SolarPointsPayload(int points) implements CustomPayload {
        public static final Id<SolarPointsPayload> ID = new Id<>(SOLAR_POINTS);
        public static final PacketCodec<RegistryByteBuf, SolarPointsPayload> CODEC = PacketCodec.tuple(
            PacketCodecs.INTEGER, SolarPointsPayload::points,
            SolarPointsPayload::new
        );

        @Override
        public Id<? extends CustomPayload> getId() {
            return ID;
        }
    }

    public record BronzeAxeDoubleJumpPayload() implements CustomPayload {
        public static final Id<BronzeAxeDoubleJumpPayload> ID = new Id<>(BRONZE_AXE_DOUBLE_JUMP);
        public static final PacketCodec<RegistryByteBuf, BronzeAxeDoubleJumpPayload> CODEC = PacketCodec.unit(new BronzeAxeDoubleJumpPayload());

        @Override
        public Id<? extends CustomPayload> getId() {
            return ID;
        }
    }

    public record RootBindingChainsPayload(java.util.List<Integer> entityLinks) implements CustomPayload {
        public static final Id<RootBindingChainsPayload> ID = new Id<>(ROOT_BINDING_CHAINS);
        public static final PacketCodec<RegistryByteBuf, RootBindingChainsPayload> CODEC = PacketCodec.tuple(
            PacketCodecs.collection(java.util.ArrayList::new, PacketCodecs.INTEGER), RootBindingChainsPayload::entityLinks,
            RootBindingChainsPayload::new
        );

        @Override
        public Id<? extends CustomPayload> getId() {
            return ID;
        }
    }

    public record SubmitAltarTaskPayload(int x, int y, int z) implements CustomPayload {
        public static final Id<SubmitAltarTaskPayload> ID = new Id<>(SUBMIT_ALTAR_TASK);
        public static final PacketCodec<RegistryByteBuf, SubmitAltarTaskPayload> CODEC = PacketCodec.tuple(
            PacketCodecs.INTEGER, SubmitAltarTaskPayload::x,
            PacketCodecs.INTEGER, SubmitAltarTaskPayload::y,
            PacketCodecs.INTEGER, SubmitAltarTaskPayload::z,
            SubmitAltarTaskPayload::new
        );

        @Override
        public Id<? extends CustomPayload> getId() {
            return ID;
        }
    }

    public record AltarActionPayload(String data) implements CustomPayload {
        public static final Id<AltarActionPayload> ID = new Id<>(ALTAR_ACTION);
        public static final PacketCodec<RegistryByteBuf, AltarActionPayload> CODEC = PacketCodec.tuple(
            PacketCodecs.STRING, AltarActionPayload::data,
            AltarActionPayload::new
        );

        @Override
        public Id<? extends CustomPayload> getId() {
            return ID;
        }
    }

    public record AltarScenePayload(String data) implements CustomPayload {
        public static final Id<AltarScenePayload> ID = new Id<>(ALTAR_SCENE);
        public static final PacketCodec<RegistryByteBuf, AltarScenePayload> CODEC = PacketCodec.tuple(
            PacketCodecs.STRING, AltarScenePayload::data,
            AltarScenePayload::new
        );

        @Override
        public Id<? extends CustomPayload> getId() {
            return ID;
        }
    }
    
    public static void registerServer() {
        PayloadTypeRegistry.playS2C().register(OpenAltarScreenPayload.ID, OpenAltarScreenPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(SubmitAltarTaskPayload.ID, SubmitAltarTaskPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(AltarActionPayload.ID, AltarActionPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(AltarScenePayload.ID, AltarScenePayload.CODEC);
        PayloadTypeRegistry.playS2C().register(SolarPointsPayload.ID, SolarPointsPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(RootBindingChainsPayload.ID, RootBindingChainsPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(BronzeAxeDoubleJumpPayload.ID, BronzeAxeDoubleJumpPayload.CODEC);
    }
}
