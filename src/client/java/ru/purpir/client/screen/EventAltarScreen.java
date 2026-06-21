package ru.purpir.client.screen;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.item.ItemStack;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import ru.purpir.eventaltar.EventAltarChallengeRewards;
import ru.purpir.eventaltar.EventAltarQuestPool;
import ru.purpir.eventaltar.EventAltarSavedData;
import ru.purpir.network.ModPackets;

import java.util.ArrayList;
import java.util.List;

public class EventAltarScreen extends Screen {
    private static final int PANEL_WIDTH = 460;
    private static final int PANEL_HEIGHT = 268;
    private static final int SIDEBAR_WIDTH = 128;

    private final State state;
    private final List<ClickArea> clickAreas = new ArrayList<>();
    private final long openedAtMillis = System.currentTimeMillis();
    private ItemStack hoveredReward = ItemStack.EMPTY;
    private int tab;
    private QuestView selectedQuest;

    public EventAltarScreen(ModPackets.OpenAltarScreenPayload payload) {
        super(Text.translatable("event_altar.caveborn.title"));
        this.state = State.parse(payload.data());
        this.selectedQuest = getPlayerQuest();
    }

    private QuestView getPlayerQuest() {
        for (QuestView quest : state.quests) {
            if (quest.mine && !quest.completed) {
                return quest;
            }
        }
        return null;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        clickAreas.clear();
        hoveredReward = ItemStack.EMPTY;
        context.fill(0, 0, width, height, 0xb5000000);
        int x = (width - PANEL_WIDTH) / 2;
        int y = (height - PANEL_HEIGHT) / 2;

        drawFrame(context, x, y);
        drawSidebar(context, x, y, mouseX, mouseY);
        drawHeader(context, x, y);

        if (tab == 0) {
            if (selectedQuest == null) {
                drawQuestList(context, x, y, mouseX, mouseY);
            } else {
                drawQuestDetails(context, x, y, selectedQuest, mouseX, mouseY);
            }
        } else {
            drawChallenges(context, x, y, mouseX, mouseY);
        }

        super.render(context, mouseX, mouseY, delta);
        if (!hoveredReward.isEmpty()) {
            context.drawItemTooltip(textRenderer, hoveredReward, mouseX, mouseY);
        }
    }

    private void drawFrame(DrawContext context, int x, int y) {
        context.fill(x, y, x + PANEL_WIDTH, y + PANEL_HEIGHT, 0xff14100b);
        context.fill(x + 2, y + 2, x + PANEL_WIDTH - 2, y + PANEL_HEIGHT - 2, 0xffa8732f);
        context.fill(x + 5, y + 5, x + PANEL_WIDTH - 5, y + PANEL_HEIGHT - 5, 0xffead8ad);
        context.fill(x + 12, y + 44, x + SIDEBAR_WIDTH, y + PANEL_HEIGHT - 12, 0xffc3aa75);
        context.fill(x + SIDEBAR_WIDTH + 8, y + 44, x + PANEL_WIDTH - 12, y + PANEL_HEIGHT - 12, 0xfff6ebcf);
        context.fill(x + SIDEBAR_WIDTH + 2, y + 44, x + SIDEBAR_WIDTH + 6, y + PANEL_HEIGHT - 12, 0xff816237);
    }

    private void drawHeader(DrawContext context, int x, int y) {
        context.drawCenteredTextWithShadow(textRenderer, title, x + PANEL_WIDTH / 2, y + 14, 0xffffe7a6);
        context.drawText(textRenderer, Text.translatable("event_altar.caveborn.level", state.level), x + 146, y + 51, 0xffffffff, true);

        int barX = x + 216;
        int barY = y + 52;
        int barW = 188;
        context.fill(barX, barY, barX + barW, barY + 12, 0xff49371f);
        int filled = state.xpNeed <= 0 ? barW : Math.min(barW, state.xp * barW / state.xpNeed);
        context.fill(barX + 1, barY + 1, barX + 1 + filled, barY + 11, 0xffffb638);
        context.drawCenteredTextWithShadow(textRenderer, Text.literal(state.xp + "/" + state.xpNeed), barX + barW / 2, barY + 2, 0xffffffff);
    }

    private void drawSidebar(DrawContext context, int x, int y, int mouseX, int mouseY) {
        drawNavButton(context, x + 24, y + 62, 84, 24, Text.translatable("event_altar.caveborn.tab.quests"), tab == 0, mouseX, mouseY,
            () -> {
                tab = 0;
                selectedQuest = getPlayerQuest();
            });
        drawNavButton(context, x + 24, y + 94, 84, 24, Text.translatable("event_altar.caveborn.tab.challenges"), tab == 1, mouseX, mouseY,
            () -> {
                tab = 1;
                selectedQuest = null;
            });
        Text completed = Text.translatable("event_altar.caveborn.completed_short");
        drawCenteredPlain(context, completed, x + 70, y + PANEL_HEIGHT - 42, 0xff3d2e1a);
        drawCenteredPlain(context, Text.literal(String.valueOf(state.totalCompleted)), x + 70, y + PANEL_HEIGHT - 30, 0xff3d2e1a);
    }

    private void drawQuestList(DrawContext context, int x, int y, int mouseX, int mouseY) {
        context.drawText(textRenderer, Text.translatable("event_altar.caveborn.available_quests"), x + 146, y + 74, 0xff2f2415, false);
        context.drawText(textRenderer, Text.translatable("event_altar.caveborn.refresh_in", formatDuration(currentSeconds(state.boardRefreshSeconds))), x + 300, y + 74, 0xff5d4528, false);
        int row = 0;
        for (QuestView quest : state.quests) {
            if (quest.completed) {
                continue;
            }
            int cardX = x + 146;
            int cardY = y + 94 + row * 31;
            drawQuestCard(context, cardX, cardY, quest, mouseX, mouseY);
            row++;
        }
    }

    private void drawQuestCard(DrawContext context, int x, int y, QuestView quest, int mouseX, int mouseY) {
        boolean hovered = in(mouseX, mouseY, x, y, 282, 27);
        context.fill(x, y, x + 282, y + 27, hovered ? 0xffead197 : 0xffdcc28a);
        context.fill(x, y, x + 6, y + 27, rarityColor(quest.rarity));
        context.drawText(textRenderer, EventAltarQuestPool.title(quest.toState()), x + 12, y + 4, 0xff2f2415, false);
        context.drawText(textRenderer, trimToWidth(quest.listStatusText(), 154), x + 12, y + 15, 0xff5d4528, false);
        drawRewardIcons(context, quest, x + 174, y + 4, 5, 18, mouseX, mouseY);
        clickAreas.add(new ClickArea(x, y, 282, 27, () -> {
            if (!quest.claimed || quest.mine) {
                selectedQuest = quest;
            }
        }));
    }

    private void drawQuestDetails(DrawContext context, int x, int y, QuestView quest, int mouseX, int mouseY) {
        EventAltarSavedData.QuestState questState = quest.toState();
        context.drawText(textRenderer, EventAltarQuestPool.title(questState), x + 146, y + 76, 0xff2f2415, false);
        context.drawText(textRenderer, EventAltarQuestPool.rarityName(quest.rarity), x + 146, y + 91, rarityColor(quest.rarity), false);
        drawWrapped(context, EventAltarQuestPool.description(questState), x + 146, y + 112, 164, 0xff4a3821, 4);

        context.fill(x + 146, y + 158, x + 398, y + 170, 0xff4c3920);
        int filled = quest.target <= 0 ? 252 : Math.min(252, quest.progress * 252 / quest.target);
        context.fill(x + 147, y + 159, x + 147 + filled, y + 169, quest.rewardReady ? 0xff7bc96f : 0xffffba3b);
        context.drawText(textRenderer, EventAltarQuestPool.progressText(questState), x + 146, y + 176, 0xff2f2415, false);
        context.drawText(textRenderer, Text.translatable("event_altar.caveborn.reward_xp", EventAltarQuestPool.xp(questState)), x + 146, y + 190, 0xff2f2415, false);

        context.drawText(textRenderer, Text.translatable("event_altar.caveborn.reward"), x + 334, y + 82, 0xff2f2415, false);
        drawRewardIcons(context, quest, x + 320, y + 102, 4, 18, mouseX, mouseY);

        boolean lockedToMine = quest.mine;
        if (!lockedToMine) {
            drawActionButton(context, x + 220, y + 226, 74, 22, Text.translatable("gui.caveborn.previous"), mouseX, mouseY, () -> selectedQuest = null);
        }

        String action = quest.rewardReady ? "finish" : quest.mine ? "cancel" : "claim";
        Text label = quest.rewardReady
            ? Text.translatable("event_altar.caveborn.finish")
            : quest.mine ? Text.translatable("event_altar.caveborn.cancel") : Text.translatable("event_altar.caveborn.claim");
        drawActionButton(context, x + 304, y + 226, 96, 22, label, mouseX, mouseY, () -> send(action, quest.id));
    }

    private void drawChallenges(DrawContext context, int x, int y, int mouseX, int mouseY) {
        drawChallengeCard(context, x + 146, y + 78, Text.translatable("event_altar.caveborn.challenge.wave"),
            Text.translatable("event_altar.caveborn.challenge.wave.desc"), "wave", EventAltarChallengeRewards.possibleWaveRewards(state.level),
            currentSeconds(state.waveCooldownSeconds), mouseX, mouseY);
        drawChallengeCard(context, x + 146, y + 166, Text.translatable("event_altar.caveborn.challenge.defend"),
            Text.translatable("event_altar.caveborn.challenge.defend.desc"), "defend", EventAltarChallengeRewards.possibleDefendRewards(state.level),
            currentSeconds(state.defendCooldownSeconds), mouseX, mouseY);
    }

    private void drawChallengeCard(DrawContext context, int x, int y, Text title, Text desc, String action, List<ItemStack> rewards, int cooldownSeconds, int mouseX, int mouseY) {
        boolean locked = state.altarChallengeActive || cooldownSeconds > 0;
        context.fill(x, y, x + 282, y + 82, 0xffdcc28a);
        context.fill(x, y, x + 6, y + 82, 0xff8b5a2b);
        context.drawText(textRenderer, title, x + 14, y + 8, 0xff2f2415, false);
        drawWrapped(context, desc, x + 14, y + 23, 158, 0xff4a3821, 2);
        context.drawText(textRenderer, Text.translatable("event_altar.caveborn.possible_rewards"), x + 14, y + 45, 0xff2f2415, false);
        drawItemStackIcons(context, rewards, x + 142, y + 43, 7, 18, mouseX, mouseY);
        drawActionButton(context, x + 190, y + 12, 74, 22, Text.translatable("event_altar.caveborn.start"), mouseX, mouseY, () -> {
            if (!locked) {
                send(action, 0);
            }
        });
        if (locked) {
            context.fill(x, y, x + 282, y + 82, 0x99000000);
            Text label = state.altarChallengeActive
                ? Text.translatable("event_altar.caveborn.challenge.active_short")
                : Text.translatable("event_altar.caveborn.challenge.cooldown_short", formatDuration(cooldownSeconds));
            drawCenteredPlain(context, label, x + 141, y + 37, 0xffffffff);
        }
    }

    private void drawRewardIcons(DrawContext context, QuestView quest, int x, int y, int columns, int spacing, int mouseX, int mouseY) {
        List<ItemStack> rewards = EventAltarQuestPool.rewards(quest.toState(), state.level);
        drawItemStackIcons(context, rewards, x, y, columns, spacing, mouseX, mouseY);
    }

    private void drawItemStackIcons(DrawContext context, List<ItemStack> rewards, int x, int y, int columns, int spacing, int mouseX, int mouseY) {
        for (int i = 0; i < rewards.size(); i++) {
            ItemStack reward = rewards.get(i);
            int iconX = x + (i % columns) * spacing;
            int iconY = y + (i / columns) * spacing;
            context.fill(iconX - 2, iconY - 2, iconX + 18, iconY + 18, 0xff8a6d3b);
            context.fill(iconX - 1, iconY - 1, iconX + 17, iconY + 17, 0xfff4e2b7);
            context.drawItem(reward, iconX, iconY);
            context.drawStackOverlay(textRenderer, reward, iconX, iconY);
            if (in(mouseX, mouseY, iconX - 2, iconY - 2, 20, 20)) {
                hoveredReward = reward;
            }
        }
    }

    private void drawRewardText(DrawContext context, QuestView quest, int x, int y) {
        List<ItemStack> rewards = EventAltarQuestPool.rewards(quest.toState(), state.level);
        for (int i = 0; i < rewards.size() && i < 4; i++) {
            ItemStack stack = rewards.get(i);
            context.drawText(textRenderer, Text.literal(stack.getCount() + "x ").append(stack.getName()), x, y + i * 11, 0xff4a3821, false);
        }
    }

    private void drawNavButton(DrawContext context, int x, int y, int w, int h, Text text, boolean active, int mouseX, int mouseY, Runnable action) {
        int color = active ? 0xfff1d285 : in(mouseX, mouseY, x, y, w, h) ? 0xffd9ba75 : 0xffb8975c;
        context.fill(x, y, x + w, y + h, 0xff675033);
        context.fill(x + 2, y + 2, x + w - 2, y + h - 2, color);
        drawCenteredPlain(context, text, x + w / 2, y + 7, 0xff1f160c);
        clickAreas.add(new ClickArea(x, y, w, h, action));
    }

    private void drawActionButton(DrawContext context, int x, int y, int w, int h, Text text, int mouseX, int mouseY, Runnable action) {
        boolean hovered = in(mouseX, mouseY, x, y, w, h);
        context.fill(x, y, x + w, y + h, 0xff3b2a19);
        context.fill(x + 2, y + 2, x + w - 2, y + h - 2, hovered ? 0xffffcf6d : 0xffd99e45);
        drawCenteredPlain(context, text, x + w / 2, y + 7, 0xff1f160c);
        clickAreas.add(new ClickArea(x, y, w, h, action));
    }

    private void drawCenteredPlain(DrawContext context, Text text, int centerX, int y, int color) {
        context.drawText(textRenderer, text, centerX - textRenderer.getWidth(text) / 2, y, color, false);
    }

    private Text trimToWidth(Text text, int maxWidth) {
        String value = text.getString();
        if (textRenderer.getWidth(value) <= maxWidth) {
            return text;
        }

        String suffix = "...";
        int suffixWidth = textRenderer.getWidth(suffix);
        StringBuilder trimmed = new StringBuilder();
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (textRenderer.getWidth(trimmed.toString() + c) + suffixWidth > maxWidth) {
                break;
            }
            trimmed.append(c);
        }
        return Text.literal(trimmed.toString().stripTrailing() + suffix);
    }

    private static String formatDuration(int seconds) {
        int safeSeconds = Math.max(0, seconds);
        return String.format("%02d:%02d", safeSeconds / 60, safeSeconds % 60);
    }

    private int currentSeconds(int initialSeconds) {
        int elapsedSeconds = (int) ((System.currentTimeMillis() - openedAtMillis) / 1000L);
        return Math.max(0, initialSeconds - elapsedSeconds);
    }

    private void drawWrapped(DrawContext context, Text text, int x, int y, int width, int color, int maxLines) {
        List<OrderedText> lines = textRenderer.wrapLines(text, width);
        for (int i = 0; i < lines.size() && i < maxLines; i++) {
            context.drawText(textRenderer, lines.get(i), x, y + i * 10, color, false);
        }
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubleClick) {
        if (click.button() == 0) {
            for (ClickArea area : new ArrayList<>(clickAreas)) {
                if (in((int) click.x(), (int) click.y(), area.x, area.y, area.w, area.h)) {
                    area.action.run();
                    return true;
                }
            }
        }
        return super.mouseClicked(click, doubleClick);
    }

    private static boolean in(int mouseX, int mouseY, int x, int y, int w, int h) {
        return mouseX >= x && mouseX < x + w && mouseY >= y && mouseY < y + h;
    }

    private static int rarityColor(int rarity) {
        return rarity == EventAltarQuestPool.RARITY_HARDCORE ? 0xff8d2525 : rarity == EventAltarQuestPool.RARITY_HARD ? 0xffa46b22 : 0xff51723d;
    }

    private void send(String action, int arg) {
        ClientPlayNetworking.send(new ModPackets.AltarActionPayload(
            action + "|" + state.origin.getX() + "|" + state.origin.getY() + "|" + state.origin.getZ() + "|" + arg
        ));
    }

    private record ClickArea(int x, int y, int w, int h, Runnable action) {
    }

    private record State(BlockPos origin, int level, int xp, int xpNeed, int totalCompleted, int boardRefreshSeconds, int waveCooldownSeconds, int defendCooldownSeconds, boolean altarChallengeActive, List<QuestView> quests) {
        private static State parse(String raw) {
            String[] sections = raw.split(";", -1);
            int x = parseInt(sections, 0);
            int y = parseInt(sections, 1);
            int z = parseInt(sections, 2);
            int level = parseInt(sections, 3);
            int xp = parseInt(sections, 4);
            int xpNeed = parseInt(sections, 5);
            int total = parseInt(sections, 6);
            int refreshSeconds = parseInt(sections, 8);
            int waveCooldownSeconds = parseInt(sections, 9);
            int defendCooldownSeconds = parseInt(sections, 10);
            boolean altarChallengeActive = parseInt(sections, 11) == 1;
            List<QuestView> quests = new ArrayList<>();
            if (sections.length > 7) {
                for (String encoded : sections[7].split("\\|")) {
                    if (!encoded.isBlank()) {
                        quests.add(QuestView.parse(encoded));
                    }
                }
            }
            return new State(new BlockPos(x, y, z), level, xp, xpNeed, total, refreshSeconds, waveCooldownSeconds, defendCooldownSeconds, altarChallengeActive, quests);
        }

        private static int parseInt(String[] values, int index) {
            if (index >= values.length) {
                return 0;
            }
            try {
                return Integer.parseInt(values[index]);
            } catch (NumberFormatException ignored) {
                return 0;
            }
        }
    }

    private record QuestView(int id, int type, int rarity, int target, int progress, String targetItem, boolean claimed, boolean mine, boolean rewardReady, boolean completed) {
        private static QuestView parse(String raw) {
            String[] values = raw.split(",");
            return new QuestView(
                parseInt(values, 0),
                parseInt(values, 1),
                parseInt(values, 2),
                parseInt(values, 3),
                parseInt(values, 4),
                values.length > 5 ? values[5] : "",
                parseInt(values, 6) == 1,
                parseInt(values, 7) == 1,
                parseInt(values, 8) == 1,
                false
            );
        }

        private static int parseInt(String[] values, int index) {
            if (index >= values.length) {
                return 0;
            }
            try {
                return Integer.parseInt(values[index]);
            } catch (NumberFormatException ignored) {
                return 0;
            }
        }

        private EventAltarSavedData.QuestState toState() {
            return new EventAltarSavedData.QuestState(id, type, rarity, target, progress, targetItem, "", rewardReady, completed);
        }

        private Text listStatusText() {
            if (rewardReady) {
                return Text.translatable("event_altar.caveborn.ready");
            }
            if (mine) {
                return Text.translatable("event_altar.caveborn.mine_short");
            }
            if (claimed) {
                return Text.translatable("event_altar.caveborn.claimed");
            }
            return EventAltarQuestPool.description(toState());
        }
    }
}
