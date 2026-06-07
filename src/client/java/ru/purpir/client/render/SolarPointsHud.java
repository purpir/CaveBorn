package ru.purpir.client.render;

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import ru.purpir.client.SolarPointsClientState;
import ru.purpir.enchantment.SolarInfusionSystem;
import ru.purpir.item.ModItems;

public class SolarPointsHud {
    private static final int WIDTH = 82;
    private static final int HEIGHT = 16;

    public static void register() {
        HudRenderCallback.EVENT.register((context, tickCounter) -> render(context));
    }

    private static void render(DrawContext context) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.options.hudHidden || !isHoldingSolarIndicatorItem()) {
            return;
        }

        int x = 2;
        int y = client.getWindow().getScaledHeight() - HEIGHT - 2;
        int points = SolarPointsClientState.getPoints();
        int fillWidth = (WIDTH - 4) * points / 100;

        context.fill(x, y, x + WIDTH, y + HEIGHT, 0xaa08090d);
        context.fill(x + 1, y + 1, x + WIDTH - 1, y + HEIGHT - 1, 0xff1a1d25);
        context.fill(x + 2, y + 2, x + WIDTH - 2, y + HEIGHT - 2, 0xff30333b);
        context.fill(x + 2, y + 2, x + 2 + fillWidth, y + HEIGHT - 2, 0xfff4d45f);
        context.fill(x + 2, y + 2, x + 2 + fillWidth, y + 6, 0xfffff0a8);

        String text = points + "/100";
        int textX = x + (WIDTH - client.textRenderer.getWidth(text)) / 2;
        int textY = y + 4;
        context.drawText(client.textRenderer, text, textX + 1, textY + 1, 0xaa000000, false);
        context.drawText(client.textRenderer, text, textX, textY, 0xffffffff, false);
    }

    private static boolean isHoldingSolarIndicatorItem() {
        MinecraftClient client = MinecraftClient.getInstance();
        return hasSolarIndicator(client.player.getStackInHand(Hand.MAIN_HAND)) ||
            hasSolarIndicator(client.player.getStackInHand(Hand.OFF_HAND));
    }

    private static boolean hasSolarIndicator(ItemStack stack) {
        return (stack.isOf(ModItems.VACUUMITE_SWORD) || stack.isOf(ModItems.BRONZE_AXE)) && SolarInfusionSystem.isInfused(stack);
    }
}
