package ru.purpir.network;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import ru.purpir.Caveborn;

public class ModPackets {
    
    public static final Identifier OPEN_ALTAR_SCREEN = Identifier.of(Caveborn.MOD_ID, "open_altar_screen");
    
    public record OpenAltarScreenPayload(int questLevel, int totalCompleted) implements CustomPayload {
        public static final Id<OpenAltarScreenPayload> ID = new Id<>(OPEN_ALTAR_SCREEN);
        public static final PacketCodec<RegistryByteBuf, OpenAltarScreenPayload> CODEC = PacketCodec.tuple(
            PacketCodecs.INTEGER, OpenAltarScreenPayload::questLevel,
            PacketCodecs.INTEGER, OpenAltarScreenPayload::totalCompleted,
            OpenAltarScreenPayload::new
        );
        
        @Override
        public Id<? extends CustomPayload> getId() {
            return ID;
        }
    }
    
    public static void registerServer() {
        PayloadTypeRegistry.playS2C().register(OpenAltarScreenPayload.ID, OpenAltarScreenPayload.CODEC);
    }
}
