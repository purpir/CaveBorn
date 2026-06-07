package ru.purpir.client.screen.widget;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

import java.util.ArrayList;
import java.util.List;

public class ScrollableTextPanel {
    private static final int LINE_HEIGHT = 10;
    private static final int SCROLLBAR_WIDTH = 4;
    private static final int WHEEL_STEP = 22;

    private final TextRenderer textRenderer;
    private final List<OrderedText> lines = new ArrayList<>();
    private int x;
    private int y;
    private int width;
    private int height;
    private int scrollY;
    private boolean draggingScrollbar;

    public ScrollableTextPanel(TextRenderer textRenderer) {
        this.textRenderer = textRenderer;
    }

    public void setBounds(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        clampScroll();
    }

    public void setText(String text) {
        this.lines.clear();
        for (String paragraph : text.split("\\\\n|\\n")) {
            if (paragraph.isBlank()) {
                this.lines.add(OrderedText.EMPTY);
                continue;
            }

            this.lines.addAll(this.textRenderer.wrapLines(Text.literal(paragraph), Math.max(8, this.width - 8)));
            this.lines.add(OrderedText.EMPTY);
        }
        clampScroll();
    }

    public void resetScroll() {
        this.scrollY = 0;
        this.draggingScrollbar = false;
    }

    public void render(DrawContext context, int color) {
        context.enableScissor(this.x, this.y, this.x + this.width, this.y + this.height);
        int lineY = this.y - this.scrollY;
        for (OrderedText line : this.lines) {
            if (lineY > this.y + this.height) {
                break;
            }
            if (lineY + LINE_HEIGHT >= this.y) {
                context.drawText(this.textRenderer, line, this.x, lineY, color, false);
            }
            lineY += LINE_HEIGHT;
        }
        context.disableScissor();
        renderScrollbar(context);
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        if (!isMouseOver(mouseX, mouseY) || getMaxScroll() <= 0) {
            return false;
        }

        this.scrollY = MathHelper.clamp(this.scrollY - (int) Math.round(amount * WHEEL_STEP), 0, getMaxScroll());
        return true;
    }

    public boolean mouseClicked(Click click) {
        if (click.button() != 0 || getMaxScroll() <= 0 || !isMouseOverScrollbar(click.x(), click.y())) {
            return false;
        }

        this.draggingScrollbar = true;
        updateScrollFromMouse(click.y());
        return true;
    }

    public boolean mouseDragged(Click click, double offsetY) {
        if (!this.draggingScrollbar) {
            return false;
        }

        updateScrollFromMouse(click.y() + offsetY);
        return true;
    }

    public boolean mouseReleased(Click click) {
        if (!this.draggingScrollbar) {
            return false;
        }

        this.draggingScrollbar = false;
        return true;
    }

    private void renderScrollbar(DrawContext context) {
        int maxScroll = getMaxScroll();
        if (maxScroll <= 0) {
            return;
        }

        int barX = this.x + this.width - SCROLLBAR_WIDTH;
        context.fill(barX, this.y, barX + SCROLLBAR_WIDTH, this.y + this.height, 0x553b372d);

        int thumbHeight = Math.max(18, this.height * this.height / getContentHeight());
        int travel = this.height - thumbHeight;
        int thumbY = this.y + (travel <= 0 ? 0 : this.scrollY * travel / maxScroll);
        context.fill(barX, thumbY, barX + SCROLLBAR_WIDTH, thumbY + thumbHeight, 0xff8a7652);
        context.fill(barX + 1, thumbY + 1, barX + SCROLLBAR_WIDTH - 1, thumbY + thumbHeight - 1, 0xffc3a66d);
    }

    private void updateScrollFromMouse(double mouseY) {
        int maxScroll = getMaxScroll();
        if (maxScroll <= 0) {
            this.scrollY = 0;
            return;
        }

        int thumbHeight = Math.max(18, this.height * this.height / getContentHeight());
        int travel = Math.max(1, this.height - thumbHeight);
        double localY = mouseY - this.y - thumbHeight / 2.0;
        this.scrollY = MathHelper.clamp((int) Math.round(localY * maxScroll / travel), 0, maxScroll);
    }

    private boolean isMouseOver(double mouseX, double mouseY) {
        return mouseX >= this.x && mouseX < this.x + this.width && mouseY >= this.y && mouseY < this.y + this.height;
    }

    private boolean isMouseOverScrollbar(double mouseX, double mouseY) {
        return mouseX >= this.x + this.width - SCROLLBAR_WIDTH - 2 && mouseX < this.x + this.width + 2 &&
            mouseY >= this.y && mouseY < this.y + this.height;
    }

    private int getContentHeight() {
        return this.lines.size() * LINE_HEIGHT;
    }

    private int getMaxScroll() {
        return Math.max(0, getContentHeight() - this.height);
    }

    private void clampScroll() {
        this.scrollY = MathHelper.clamp(this.scrollY, 0, getMaxScroll());
    }
}
