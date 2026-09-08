package ru.purpir.client.render;

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import ru.purpir.minecart.MinecartChainCarrier;

import java.util.Locale;

public final class MinecartSpeedHud {
    private static final int WIDTH = 82;
    private static final int HEIGHT = 16;

    private MinecartSpeedHud() {
    }

    public static void register() {
        HudRenderCallback.EVENT.register((context, tickCounter) -> render(context));
    }

    private static void render(DrawContext context) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.options.hudHidden) {
            return;
        }

        Entity vehicle = client.player.getVehicle();
        if (!(vehicle instanceof AbstractMinecartEntity cart) || !(cart instanceof MinecartChainCarrier carrier)) {
            return;
        }

        int x = client.getWindow().getScaledWidth() / 2 - WIDTH / 2;
        int y = client.getWindow().getScaledHeight() - 32;
        float speed = Math.max(0.0F, Math.min(36.0F, carrier.caveborn$getMinecartSpeed()));
        int fillWidth = (int) ((WIDTH - 4) * speed / 36.0F);

        context.fill(x, y, x + WIDTH, y + HEIGHT, 0xaa081114);
        context.fill(x + 1, y + 1, x + WIDTH - 1, y + HEIGHT - 1, 0xff1c2027);
        context.fill(x + 2, y + 2, x + WIDTH - 2, y + HEIGHT - 2, 0xff2f3944);
        context.fill(x + 2, y + 2, x + 2 + fillWidth, y + HEIGHT - 2, 0xff86d8ff);
        context.fill(x + 2, y + 2, x + 2 + fillWidth, y + 6, 0xffffffff);

        String text = String.format(Locale.ROOT, "%.1f / 36", speed);
        int textX = x + (WIDTH - client.textRenderer.getWidth(text)) / 2;
        int textY = y + 4;
        context.drawText(client.textRenderer, text, textX + 1, textY + 1, 0xaa000000, false);
        context.drawText(client.textRenderer, text, textX, textY, 0xffffffff, false);
    }
}
