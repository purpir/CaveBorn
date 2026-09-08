package ru.purpir.minecart;

import java.util.UUID;

public interface MinecartChainCarrier {
    float caveborn$getMinecartSpeed();

    void caveborn$setMinecartSpeed(float speed);

    UUID caveborn$getMinecartPrevUuid();

    void caveborn$setMinecartPrevUuid(UUID uuid);

    UUID caveborn$getMinecartNextUuid();

    void caveborn$setMinecartNextUuid(UUID uuid);
}
