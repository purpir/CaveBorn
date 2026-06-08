package ru.purpir.client.screen;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import ru.purpir.Caveborn;
import ru.purpir.block.ModBlocks;
import ru.purpir.client.screen.widget.ScrollableTextPanel;
import ru.purpir.item.ModItems;

import java.util.ArrayList;
import java.util.List;

public class SolarInfusionGuideScreen extends Screen {
    private static final int PANEL_WIDTH = 430;
    private static final int PANEL_HEIGHT = 250;
    private static final int TAB_WIDTH = 92;
    private static final int ABILITY_BUTTON_SIZE = 24;
    private static final int ABILITIES_SECTION = 4;
    private static final int EVENT_ALTAR_SECTION = 5;
    private static final Identifier EVENT_ALTAR_PREVIEW = Identifier.of(Caveborn.MOD_ID, "textures/gui/event_altar_preview.png");
    private static final int ABILITY_COLUMNS = 4;
    private static final int ABILITY_ROWS = 5;
    private static final int ABILITIES_PER_PAGE = ABILITY_COLUMNS * ABILITY_ROWS;
    private static final Section[] SECTIONS = new Section[] {
        new Section("guide.caveborn.solar.section.basics", "guide.caveborn.solar.content.basics"),
        new Section("guide.caveborn.solar.section.crystal", "guide.caveborn.solar.content.crystal"),
        new Section("guide.caveborn.solar.section.infusion", "guide.caveborn.solar.content.infusion"),
        new Section("guide.caveborn.solar.section.indicator", "guide.caveborn.solar.content.indicator"),
        new Section("guide.caveborn.solar.section.abilities", "guide.caveborn.solar.content.abilities"),
        new Section("guide.caveborn.solar.section.event_altar", "guide.caveborn.solar.content.event_altar"),
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
        new AbilityEntry(new ItemStack(ModItems.VACUUMITE_SWORD), "guide.caveborn.solar.ability.vacuumite_sword", "guide.caveborn.solar.ability.vacuumite_sword.content"),
        new AbilityEntry(new ItemStack(Items.COPPER_SWORD), "guide.caveborn.solar.ability.copper_sword", "guide.caveborn.solar.ability.copper_sword.content"),
        new AbilityEntry(new ItemStack(Items.MACE), "guide.caveborn.solar.ability.mace", "guide.caveborn.solar.ability.mace.content"),
        new AbilityEntry(new ItemStack(ModItems.NETHERITE_TITANIUM_SWORD), "guide.caveborn.solar.ability.solar_strike", "guide.caveborn.solar.ability.solar_strike.content"),
        new AbilityEntry(new ItemStack(Items.SHIELD), "guide.caveborn.solar.ability.shield", "guide.caveborn.solar.ability.shield.content"),
        new AbilityEntry(new ItemStack(ModItems.VACUUMITE_MAGNET), "guide.caveborn.solar.ability.magnet", "guide.caveborn.solar.ability.magnet.content"),
        new AbilityEntry(new ItemStack(Items.BOW), "guide.caveborn.solar.ability.bow", "guide.caveborn.solar.ability.bow.content"),
        new AbilityEntry(new ItemStack(Items.TRIDENT), "guide.caveborn.solar.ability.trident", "guide.caveborn.solar.ability.trident.content"),
        new AbilityEntry(new ItemStack(Items.ARROW), "guide.caveborn.solar.ability.arrow", "guide.caveborn.solar.ability.arrow.content"),
        new AbilityEntry(new ItemStack(Items.SPECTRAL_ARROW), "guide.caveborn.solar.ability.spectral_arrow", "guide.caveborn.solar.ability.spectral_arrow.content"),
        new AbilityEntry(new ItemStack(Items.WIND_CHARGE), "guide.caveborn.solar.ability.wind_charge", "guide.caveborn.solar.ability.wind_charge.content"),
        new AbilityEntry(new ItemStack(ModItems.CRYSTAL_DUST), "guide.caveborn.solar.ability.crystal_dust", "guide.caveborn.solar.ability.crystal_dust.content"),
        new AbilityEntry(new ItemStack(Items.TOTEM_OF_UNDYING), "guide.caveborn.solar.ability.totem", "guide.caveborn.solar.ability.totem.content"),
        new AbilityEntry(new ItemStack(Items.ENDER_PEARL), "guide.caveborn.solar.ability.ender_pearl", "guide.caveborn.solar.ability.ender_pearl.content"),
        new AbilityEntry(new ItemStack(ModBlocks.HOGWEED_PASTE), "guide.caveborn.solar.ability.hogweed_paste", "guide.caveborn.solar.ability.hogweed_paste.content"),
        new AbilityEntry(new ItemStack(ModItems.RUSTED_MINER_KEY), "guide.caveborn.solar.ability.rusted_miner_key", "guide.caveborn.solar.ability.rusted_miner_key.content"),
        new AbilityEntry(new ItemStack(ModItems.BRONZE_AXE), "guide.caveborn.solar.ability.bronze_axe", "guide.caveborn.solar.ability.bronze_axe.content"),
        new AbilityEntry(new ItemStack(ModItems.ZINC_KNIFE), "guide.caveborn.solar.ability.zinc_knife", "guide.caveborn.solar.ability.zinc_knife.content"),
        new AbilityEntry(new ItemStack(ModItems.CRACK_HAMMER), "guide.caveborn.solar.ability.crack_hammer", "guide.caveborn.solar.ability.crack_hammer.content")
    };

    private int selectedSection = 0;
    private int selectedAbility = 0;
    private int abilityPage = 0;
    private final List<ButtonWidget> tabButtons = new ArrayList<>();
    private final List<ButtonWidget> abilityButtons = new ArrayList<>();
    private ButtonWidget previousAbilityPageButton;
    private ButtonWidget nextAbilityPageButton;
    private ScrollableTextPanel abilityTextPanel;

    public SolarInfusionGuideScreen() {
        super(Text.translatable("guide.caveborn.solar.title"));
    }

    @Override
    protected void init() {
        this.tabButtons.clear();
        int x = (this.width - PANEL_WIDTH) / 2;
        int y = (this.height - PANEL_HEIGHT) / 2;
        this.abilityTextPanel = new ScrollableTextPanel(this.textRenderer);

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

        for (int i = 0; i < ABILITIES_PER_PAGE; i++) {
            final int slotIndex = i;
            int column = i % ABILITY_COLUMNS;
            int row = i / ABILITY_COLUMNS;
            ButtonWidget button = ButtonWidget.builder(Text.empty(), widget -> {
                    int abilityIndex = this.abilityPage * ABILITIES_PER_PAGE + slotIndex;
                    if (abilityIndex < ABILITIES.length) {
                        this.selectedAbility = abilityIndex;
                        this.abilityTextPanel.resetScroll();
                        refreshTabButtons();
                    }
                })
                .dimensions(x + 120 + column * 30, y + 48 + row * 30, ABILITY_BUTTON_SIZE, ABILITY_BUTTON_SIZE)
                .build();
            this.abilityButtons.add(button);
            this.addDrawableChild(button);
        }

        this.previousAbilityPageButton = ButtonWidget.builder(Text.literal("<"), widget -> {
                this.abilityPage = Math.max(0, this.abilityPage - 1);
                this.selectedAbility = this.abilityPage * ABILITIES_PER_PAGE;
                this.abilityTextPanel.resetScroll();
                refreshTabButtons();
            })
            .dimensions(x + 150, y + 196, 24, 18)
            .build();
        this.addDrawableChild(this.previousAbilityPageButton);

        this.nextAbilityPageButton = ButtonWidget.builder(Text.literal(">"), widget -> {
                this.abilityPage = Math.min(getMaxAbilityPage(), this.abilityPage + 1);
                this.selectedAbility = this.abilityPage * ABILITIES_PER_PAGE;
                this.abilityTextPanel.resetScroll();
                refreshTabButtons();
            })
            .dimensions(x + 190, y + 196, 24, 18)
            .build();
        this.addDrawableChild(this.nextAbilityPageButton);

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
        for (int i = 0; i < this.abilityButtons.size(); i++) {
            int abilityIndex = this.abilityPage * ABILITIES_PER_PAGE + i;
            ButtonWidget button = this.abilityButtons.get(i);
            boolean hasAbility = abilityIndex < ABILITIES.length;
            button.visible = showAbilities && hasAbility;
            button.active = showAbilities && hasAbility;
            int column = i % ABILITY_COLUMNS;
            int row = i / ABILITY_COLUMNS;
            if (button.visible) {
                button.setPosition((this.width - PANEL_WIDTH) / 2 + 120 + column * 30, (this.height - PANEL_HEIGHT) / 2 + 48 + row * 30);
            } else {
                button.setPosition(-1000, -1000);
            }
        }

        if (this.previousAbilityPageButton != null && this.nextAbilityPageButton != null) {
            this.previousAbilityPageButton.visible = showAbilities && getMaxAbilityPage() > 0;
            this.nextAbilityPageButton.visible = showAbilities && getMaxAbilityPage() > 0;
            this.previousAbilityPageButton.active = showAbilities && this.abilityPage > 0;
            this.nextAbilityPageButton.active = showAbilities && this.abilityPage < getMaxAbilityPage();
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
        } else if (this.selectedSection == EVENT_ALTAR_SECTION) {
            renderEventAltarPage(context, x, y, section);
        } else {
            drawWrappedContent(context, translateForBook(section.contentKey()), x + 120, y + 54, 282, 0xff3b372d);
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
        drawWrappedContent(context, translateForBook(ability.titleKey()), pageX + 22, pageY, 123, 0xff3b372d);
        this.abilityTextPanel.setBounds(pageX, pageY + 28, 148, 102);
        this.abilityTextPanel.setText(translateForBook(ability.contentKey()));
        this.abilityTextPanel.render(context, 0xff3b372d);
    }

    private void renderEventAltarPage(DrawContext context, int x, int y, Section section) {
        int imageX = x + 120;
        int imageY = y + 54;
        int imageW = 140;
        int imageH = 79;
        context.fill(imageX - 2, imageY - 2, imageX + imageW + 2, imageY + imageH + 2, 0xff6b5b3b);
        context.drawTexture(RenderPipelines.GUI_TEXTURED, EVENT_ALTAR_PREVIEW, imageX, imageY, 0.0f, 0.0f, imageW, imageH, 220, 124, 220, 124);
        drawWrappedContent(context, translateForBook(section.contentKey()), x + 120, y + 140, 282, 0xff3b372d);
    }

    private void renderAbilityIcons(DrawContext context, int x, int y, int mouseX, int mouseY) {
        for (int i = 0; i < ABILITIES_PER_PAGE; i++) {
            int abilityIndex = this.abilityPage * ABILITIES_PER_PAGE + i;
            if (abilityIndex >= ABILITIES.length) {
                continue;
            }

            int column = i % ABILITY_COLUMNS;
            int row = i / ABILITY_COLUMNS;
            int iconX = x + 124 + column * 30;
            int iconY = y + 52 + row * 30;
            if (abilityIndex == this.selectedAbility) {
                context.fill(iconX - 4, iconY - 4, iconX + 20, iconY + 20, 0x55fff2a6);
            }
            context.drawItem(ABILITIES[abilityIndex].icon(), iconX, iconY);
        }
    }

    private int getMaxAbilityPage() {
        return Math.max(0, (ABILITIES.length - 1) / ABILITIES_PER_PAGE);
    }

    private String translateForBook(String key) {
        String translated = I18n.translate(key);
        if (translated.startsWith("Format error: ")) {
            return translated.substring("Format error: ".length());
        }
        return translated;
    }

    private void drawWrappedContent(DrawContext context, String text, int x, int y, int width, int color) {
        int lineY = y;
        for (OrderedText line : wrapContentLines(text, width)) {
            context.drawText(this.textRenderer, line, x, lineY, color, false);
            lineY += 10;
        }
    }

    private List<OrderedText> wrapContentLines(String text, int width) {
        List<OrderedText> lines = new ArrayList<>();
        for (String paragraph : text.split("\\\\n|\\n")) {
            if (paragraph.isBlank()) {
                lines.add(OrderedText.EMPTY);
                continue;
            }

            lines.addAll(this.textRenderer.wrapLines(Text.literal(paragraph), width));
            lines.add(OrderedText.EMPTY);
        }
        return lines;
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (this.selectedSection == ABILITIES_SECTION && this.abilityTextPanel != null &&
            this.abilityTextPanel.mouseScrolled(mouseX, mouseY, verticalAmount)) {
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubleClick) {
        if (this.selectedSection == ABILITIES_SECTION && this.abilityTextPanel != null &&
            this.abilityTextPanel.mouseClicked(click)) {
            return true;
        }
        return super.mouseClicked(click, doubleClick);
    }

    @Override
    public boolean mouseDragged(Click click, double offsetX, double offsetY) {
        if (this.selectedSection == ABILITIES_SECTION && this.abilityTextPanel != null &&
            this.abilityTextPanel.mouseDragged(click, offsetY)) {
            return true;
        }
        return super.mouseDragged(click, offsetX, offsetY);
    }

    @Override
    public boolean mouseReleased(Click click) {
        if (this.abilityTextPanel != null && this.abilityTextPanel.mouseReleased(click)) {
            return true;
        }
        return super.mouseReleased(click);
    }

    private record Section(String titleKey, String contentKey) {
    }

    private record AbilityEntry(ItemStack icon, String titleKey, String contentKey) {
    }
}
