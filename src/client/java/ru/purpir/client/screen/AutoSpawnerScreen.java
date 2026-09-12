package ru.purpir.client.screen;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import ru.purpir.screen.AutoSpawnerScreenHandler;
import java.util.List;

public class AutoSpawnerScreen extends HandledScreen<AutoSpawnerScreenHandler> {
    public AutoSpawnerScreen(AutoSpawnerScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        backgroundWidth = 176;
        backgroundHeight = 211;
        titleX = 0;
        titleY = 7;
        playerInventoryTitleY = 116;
    }
    @Override protected void init() {
        super.init();
        addDrawableChild(ButtonWidget.builder(Text.translatable("screen.caveborn.automatic_spawner.storage"), b -> client.interactionManager.clickButton(handler.syncId, 0)).dimensions(x + 8, y + 72, 160, 20).build());
    }
    @Override protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        int left = (width - backgroundWidth) / 2;
        int top = (height - backgroundHeight) / 2;
        context.fill(left, top, left + backgroundWidth, top + backgroundHeight, 0xffc6c0aa);
        context.fill(left + 3, top + 3, left + backgroundWidth - 3, top + backgroundHeight - 3, 0xffe6dec7);
        context.fill(left + 8, top + 17, left + 168, top + 64, 0xffd8cfb7);
        drawSlot(context, left + 44, top + 39);
        drawSlot(context, left + 108, top + 39);
        drawPlayerSlots(context, left + 8, top + 128);
    }
    private void drawPlayerSlots(DrawContext context, int x, int y) { for (int row = 0; row < 3; row++) drawSlotRow(context, x, y + row * 18, 9); drawSlotRow(context, x, y + 54, 9); }
    private void drawSlotRow(DrawContext context, int x, int y, int count) { for (int slot = 0; slot < count; slot++) drawSlot(context, x + slot * 18, y); }
    private void drawSlot(DrawContext context, int x, int y) { context.fill(x - 1, y - 1, x + 17, y + 17, 0xff6d685d); context.fill(x, y, x + 16, y + 16, 0xffeee7d1); context.fill(x + 1, y + 1, x + 15, y + 15, 0xffbdb49e); }
    @Override protected void drawForeground(DrawContext context, int mouseX, int mouseY) {
        int centeredTitleX = (backgroundWidth - textRenderer.getWidth(title)) / 2;
        context.drawText(textRenderer, title, centeredTitleX, titleY, 0xff000000, false);
        Text eggText = Text.translatable("screen.caveborn.automatic_spawner.egg");
        Text xpText = Text.translatable("screen.caveborn.automatic_spawner.xp_value", handler.getStoredExperience());
        context.drawText(textRenderer, eggText, 52 - textRenderer.getWidth(eggText) / 2, 25, 0xff000000, false);
        context.drawText(textRenderer, xpText, 116 - textRenderer.getWidth(xpText) / 2, 25, 0xff000000, false);
        context.drawText(textRenderer, playerInventoryTitle, playerInventoryTitleX, playerInventoryTitleY, 0xff000000, false);
    }
    @Override protected void drawMouseoverTooltip(DrawContext context, int mouseX, int mouseY) {
        boolean overExperienceSlot = mouseX >= x + 108 && mouseX < x + 124 && mouseY >= y + 39 && mouseY < y + 55;
        if (overExperienceSlot) context.drawTooltip(textRenderer, List.of(Text.translatable("screen.caveborn.automatic_spawner.collect")), mouseX, mouseY);
        else super.drawMouseoverTooltip(context, mouseX, mouseY);
    }
    @Override public void render(DrawContext context, int mouseX, int mouseY, float delta) { renderBackground(context, mouseX, mouseY, delta); super.render(context, mouseX, mouseY, delta); drawMouseoverTooltip(context, mouseX, mouseY); }
}
