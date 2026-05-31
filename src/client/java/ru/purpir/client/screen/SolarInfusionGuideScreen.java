package ru.purpir.client.screen;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import ru.purpir.item.ModItems;

import java.util.ArrayList;
import java.util.List;

public class SolarInfusionGuideScreen extends Screen {
    private static final int PANEL_WIDTH = 430;
    private static final int PANEL_HEIGHT = 250;
    private static final int TAB_WIDTH = 92;
    private static final int ABILITY_BUTTON_SIZE = 24;
    private static final int ABILITIES_SECTION = 3;
    private static final Section[] SECTIONS = new Section[] {
        new Section("guide.caveborn.solar.section.basics", "guide.caveborn.solar.content.basics"),
        new Section("guide.caveborn.solar.section.crystal", "guide.caveborn.solar.content.crystal"),
        new Section("guide.caveborn.solar.section.infusion", "guide.caveborn.solar.content.infusion"),
        new Section("guide.caveborn.solar.section.abilities", "guide.caveborn.solar.content.abilities"),
        new Section("guide.caveborn.solar.section.cooldowns", "guide.caveborn.solar.content.cooldowns")
    };
    private static final AbilityEntry[] ABILITIES = new AbilityEntry[] {
        new AbilityEntry(new ItemStack(Items.WOODEN_SWORD), "guide.caveborn.solar.ability.wooden_sword", "guide.caveborn.solar.ability.wooden_sword.content"),
        new AbilityEntry(new ItemStack(Items.STONE_SWORD), "guide.caveborn.solar.ability.stone_sword", "guide.caveborn.solar.ability.stone_sword.content"),
        new AbilityEntry(new ItemStack(Items.GOLDEN_SWORD), "guide.caveborn.solar.ability.golden_sword", "guide.caveborn.solar.ability.golden_sword.content"),
        new AbilityEntry(new ItemStack(Items.IRON_SWORD), "guide.caveborn.solar.ability.iron_sword", "guide.caveborn.solar.ability.iron_sword.content"),
        new AbilityEntry(new ItemStack(Items.DIAMOND_SWORD), "guide.caveborn.solar.ability.diamond_sword", "guide.caveborn.solar.ability.diamond_sword.content"),
        new AbilityEntry(new ItemStack(Items.NETHERITE_SWORD), "guide.caveborn.solar.ability.netherite_sword", "guide.caveborn.solar.ability.netherite_sword.content"),
        new AbilityEntry(new ItemStack(ModItems.BRONZE_SWORD), "guide.caveborn.solar.ability.bronze_sword", "guide.caveborn.solar.ability.bronze_sword.content"),
        new AbilityEntry(new ItemStack(Items.COPPER_SWORD), "guide.caveborn.solar.ability.copper_sword", "guide.caveborn.solar.ability.copper_sword.content"),
        new AbilityEntry(new ItemStack(Items.MACE), "guide.caveborn.solar.ability.mace", "guide.caveborn.solar.ability.mace.content"),
        new AbilityEntry(new ItemStack(ModItems.NETHERITE_TITANIUM_SWORD), "guide.caveborn.solar.ability.solar_strike", "guide.caveborn.solar.ability.solar_strike.content"),
        new AbilityEntry(new ItemStack(Items.SHIELD), "guide.caveborn.solar.ability.shield", "guide.caveborn.solar.ability.shield.content"),
        new AbilityEntry(new ItemStack(ModItems.VACUUMITE_MAGNET), "guide.caveborn.solar.ability.magnet", "guide.caveborn.solar.ability.magnet.content"),
        new AbilityEntry(new ItemStack(Items.BOW), "guide.caveborn.solar.ability.bow", "guide.caveborn.solar.ability.bow.content"),
        new AbilityEntry(new ItemStack(Items.WIND_CHARGE), "guide.caveborn.solar.ability.wind_charge", "guide.caveborn.solar.ability.wind_charge.content")
    };

    private int selectedSection = 0;
    private int selectedAbility = 0;
    private final List<ButtonWidget> tabButtons = new ArrayList<>();
    private final List<ButtonWidget> abilityButtons = new ArrayList<>();

    public SolarInfusionGuideScreen() {
        super(Text.translatable("guide.caveborn.solar.title"));
    }

    @Override
    protected void init() {
        this.tabButtons.clear();
        int x = (this.width - PANEL_WIDTH) / 2;
        int y = (this.height - PANEL_HEIGHT) / 2;

        for (int i = 0; i < SECTIONS.length; i++) {
            final int sectionIndex = i;
            ButtonWidget button = ButtonWidget.builder(
                    Text.translatable(SECTIONS[i].titleKey()),
                    widget -> {
                        this.selectedSection = sectionIndex;
                        refreshTabButtons();
                    })
                .dimensions(x + 12, y + 30 + i * 22, TAB_WIDTH, 20)
                .build();
            this.tabButtons.add(button);
            this.addDrawableChild(button);
        }

        for (int i = 0; i < ABILITIES.length; i++) {
            final int abilityIndex = i;
            int column = i % 4;
            int row = i / 4;
            ButtonWidget button = ButtonWidget.builder(Text.empty(), widget -> this.selectedAbility = abilityIndex)
                .dimensions(x + 120 + column * 30, y + 48 + row * 30, ABILITY_BUTTON_SIZE, ABILITY_BUTTON_SIZE)
                .build();
            this.abilityButtons.add(button);
            this.addDrawableChild(button);
        }

        this.addDrawableChild(ButtonWidget.builder(Text.translatable("gui.caveborn.previous"), widget -> {
                this.selectedSection = Math.max(0, this.selectedSection - 1);
                refreshTabButtons();
            })
            .dimensions(x + PANEL_WIDTH - 138, y + PANEL_HEIGHT - 25, 58, 20)
            .build());

        this.addDrawableChild(ButtonWidget.builder(Text.translatable("gui.caveborn.next"), widget -> {
                this.selectedSection = Math.min(SECTIONS.length - 1, this.selectedSection + 1);
                refreshTabButtons();
            })
            .dimensions(x + PANEL_WIDTH - 76, y + PANEL_HEIGHT - 25, 58, 20)
            .build());

        refreshTabButtons();
    }

    private void refreshTabButtons() {
        for (int i = 0; i < this.tabButtons.size(); i++) {
            this.tabButtons.get(i).active = i != this.selectedSection;
        }

        boolean showAbilities = this.selectedSection == ABILITIES_SECTION;
        for (ButtonWidget button : this.abilityButtons) {
            button.visible = showAbilities;
            button.active = showAbilities;
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        context.fill(0, 0, this.width, this.height, 0xaa000000);

        int x = (this.width - PANEL_WIDTH) / 2;
        int y = (this.height - PANEL_HEIGHT) / 2;
        drawBookPanel(context, x, y);

        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, y + 10, 0xfff6f0d8);
        Section section = SECTIONS[this.selectedSection];
        context.drawTextWithShadow(this.textRenderer, Text.translatable(section.titleKey()), x + 120, y + 34, 0xfffff5c8);

        if (this.selectedSection == ABILITIES_SECTION) {
            renderAbilitiesPage(context, x, y, mouseX, mouseY);
        } else {
            drawWrappedContent(context, I18n.translate(section.contentKey()), x + 120, y + 54, 282, 0xff3b372d);
        }

        super.render(context, mouseX, mouseY, delta);

        if (this.selectedSection == ABILITIES_SECTION) {
            renderAbilityIcons(context, x, y, mouseX, mouseY);
        }
    }

    private void drawBookPanel(DrawContext context, int x, int y) {
        context.fill(x, y, x + PANEL_WIDTH, y + PANEL_HEIGHT, 0xff1d1c1b);
        context.fill(x + 2, y + 2, x + PANEL_WIDTH - 2, y + PANEL_HEIGHT - 2, 0xffdedbd0);
        context.fill(x + 6, y + 6, x + PANEL_WIDTH - 6, y + PANEL_HEIGHT - 6, 0xff3b3933);
        context.fill(x + 10, y + 24, x + 110, y + PANEL_HEIGHT - 34, 0xffc9c5b8);
        context.fill(x + 114, y + 24, x + PANEL_WIDTH - 10, y + PANEL_HEIGHT - 34, 0xfff0ead8);
        context.fill(x + 110, y + 24, x + 114, y + PANEL_HEIGHT - 34, 0xff8c8778);
    }

    private void renderAbilitiesPage(DrawContext context, int x, int y, int mouseX, int mouseY) {
        AbilityEntry ability = ABILITIES[this.selectedAbility];
        int pageX = x + 252;
        int pageY = y + 54;
        context.fill(pageX - 8, pageY - 8, x + PANEL_WIDTH - 18, y + PANEL_HEIGHT - 43, 0x22ffffff);
        context.drawItem(ability.icon(), pageX, pageY - 2);
        context.drawTextWithShadow(this.textRenderer, Text.translatable(ability.titleKey()), pageX + 22, pageY + 2, 0xfffff5c8);
        drawWrappedContent(context, I18n.translate(ability.contentKey()), pageX, pageY + 24, 145, 0xff3b372d);
    }

    private void renderAbilityIcons(DrawContext context, int x, int y, int mouseX, int mouseY) {
        for (int i = 0; i < ABILITIES.length; i++) {
            int column = i % 4;
            int row = i / 4;
            int iconX = x + 124 + column * 30;
            int iconY = y + 52 + row * 30;
            if (i == this.selectedAbility) {
                context.fill(iconX - 4, iconY - 4, iconX + 20, iconY + 20, 0x55fff2a6);
            }
            context.drawItem(ABILITIES[i].icon(), iconX, iconY);
        }
    }

    private void drawWrappedContent(DrawContext context, String text, int x, int y, int width, int color) {
        int lineY = y;
        for (String paragraph : text.split("\\\\n|\\n")) {
            if (paragraph.isBlank()) {
                lineY += 8;
                continue;
            }

            for (OrderedText line : this.textRenderer.wrapLines(Text.literal(paragraph), width)) {
                context.drawText(this.textRenderer, line, x, lineY, color, false);
                lineY += 10;
            }
            lineY += 4;
        }
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    private record Section(String titleKey, String contentKey) {
    }

    private record AbilityEntry(ItemStack icon, String titleKey, String contentKey) {
    }
}
