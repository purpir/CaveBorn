package ru.purpir.client.screen;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import ru.purpir.screen.CrusherScreenHandler;

public class CrusherScreen extends HandledScreen<CrusherScreenHandler> {
    public CrusherScreen(CrusherScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        this.backgroundWidth = 176;
        this.backgroundHeight = 193;
        this.titleX = 0;
        this.titleY = 7;
        this.playerInventoryTitleY = 100;
    }

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        int x = (this.width - this.backgroundWidth) / 2;
        int y = (this.height - this.backgroundHeight) / 2;

        context.fill(x, y, x + this.backgroundWidth, y + this.backgroundHeight, 0xffc6c0aa);
        context.fill(x + 3, y + 3, x + this.backgroundWidth - 3, y + this.backgroundHeight - 3, 0xffe6dec7);
        context.fill(x + 8, y + 17, x + 168, y + 97, 0xffd8cfb7);

        drawSlotRow(context, x + 44, y + 21, 5);
        drawSlotRow(context, x + 44, y + 77, 5);
        drawPlayerSlots(context, x + 8, y + 111);

        drawArrow(context, x + 79, y + 43);
        drawPowerIndicator(context, x + 149, y + 49);
    }

    private void drawSlotRow(DrawContext context, int x, int y, int count) {
        for (int slot = 0; slot < count; slot++) {
            drawSlot(context, x + slot * 18, y);
        }
    }

    private void drawPlayerSlots(DrawContext context, int x, int y) {
        for (int row = 0; row < 3; row++) {
            drawSlotRow(context, x, y + row * 18, 9);
        }
        drawSlotRow(context, x, y + 58, 9);
    }

    private void drawSlot(DrawContext context, int x, int y) {
        context.fill(x - 1, y - 1, x + 17, y + 17, 0xff6d685d);
        context.fill(x, y, x + 16, y + 16, 0xffeee7d1);
        context.fill(x + 1, y + 1, x + 15, y + 15, 0xffbdb49e);
    }

    private void drawArrow(DrawContext context, int x, int y) {
        drawArrowShape(context, x, y, y + 28, 0xff7a705f);

        int scaled = this.handler.getProgressScaled(28);
        if (scaled > 0) {
            drawArrowShape(context, x, y, y + scaled, 0xfff0b349);
        }
    }

    private void drawArrowShape(DrawContext context, int x, int y, int clipBottom, int color) {
        drawClipped(context, x + 7, y, x + 12, y + 16, clipBottom, color);
        drawClipped(context, x + 4, y + 16, x + 15, y + 20, clipBottom, color);
        drawClipped(context, x + 6, y + 20, x + 13, y + 24, clipBottom, color);
        drawClipped(context, x + 8, y + 24, x + 11, y + 28, clipBottom, color);
    }

    private void drawClipped(DrawContext context, int x1, int y1, int x2, int y2, int clipBottom, int color) {
        int clippedY2 = Math.min(y2, clipBottom);
        if (clippedY2 > y1) {
            context.fill(x1, y1, x2, clippedY2, color);
        }
    }

    private void drawPowerIndicator(DrawContext context, int x, int y) {
        int color = this.handler.isPowered() ? 0xffd94830 : 0xff4b473f;
        context.fill(x, y, x + 10, y + 10, 0xff312f2b);
        context.fill(x + 2, y + 2, x + 8, y + 8, color);
    }

    @Override
    protected void drawForeground(DrawContext context, int mouseX, int mouseY) {
        int centeredTitleX = (this.backgroundWidth - this.textRenderer.getWidth(this.title)) / 2;
        context.drawText(this.textRenderer, this.title, centeredTitleX, this.titleY, 0xff000000, false);
        context.drawText(this.textRenderer, this.playerInventoryTitle, this.playerInventoryTitleX, this.playerInventoryTitleY, 0xff000000, false);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);
        super.render(context, mouseX, mouseY, delta);
        this.drawMouseoverTooltip(context, mouseX, mouseY);
    }
}
