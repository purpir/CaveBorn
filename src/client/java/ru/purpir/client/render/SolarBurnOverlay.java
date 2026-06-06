package ru.purpir.client.render;

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import ru.purpir.effect.ModStatusEffects;

public class SolarBurnOverlay {
    public static void register() {
        HudRenderCallback.EVENT.register((context, tickCounter) -> render(context));
    }

    private static void render(DrawContext context) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.options.hudHidden || !client.player.hasStatusEffect(ModStatusEffects.SOLAR_BURN)) {
            return;
        }

        int width = client.getWindow().getScaledWidth();
        int height = client.getWindow().getScaledHeight();

        context.fill(0, 0, width, height, 0x55ff8a00);
    }
}
