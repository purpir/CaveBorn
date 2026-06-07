package ru.purpir.client.util;

import net.minecraft.client.MinecraftClient;

public final class SceneFadeOverlay {
    private SceneFadeOverlay() {
    }

    public static void register() {
    }

    public static void start(String data) {
        MinecraftClient.getInstance().inGameHud.clearTitle();
    }
}
