package fr.iglee42.modpackutilities.modules.lore.client;

import fr.iglee42.modpackutilities.IgleeModpackUtilities;
import fr.iglee42.modpackutilities.modules.lore.ClientLoreModule;
import fr.iglee42.modpackutilities.modules.lore.LoreEntry;
import fr.iglee42.modpackutilities.modules.lore.LoreFile;
import fr.iglee42.modpackutilities.modules.lore.LoreModule;
import fr.iglee42.modpackutilities.modules.lore.network.UnlockLoreEntryPacket;
import fr.iglee42.modpackutilities.modules.lore.progress.LoreProgress;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LoreGui extends Screen {

    private static final int LIST_MARGIN = 20;
    private static final int ENTRY_HEADER_HEIGHT = 16;
    private static final int ENTRY_SPACING = 6;
    private static final int ENTRY_CONTENT_MARGIN = ENTRY_SPACING;
    private static final int LINE_HEIGHT = 10;
    private static final int LIST_CONTENT_TOP_PADDING = 4;
    private static final int UNLOCK_BUTTON_GAP = 8;
    private static final int ENTRY_PLAY_BUTTON_WIDTH = 34;
    private static final int ENTRY_PLAY_BUTTON_HEIGHT = 12;
    private static final int GUI_WIDTH_REDUCTION = 100;
    private static final int GUI_HEIGHT_REDUCTION = 25;

    // Textures (change only these paths/sizes when you add your assets)
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(IgleeModpackUtilities.MODID, "textures/gui/lore/background.png");


    private static final int PANEL_TOP_PADDING = 26;
    private static final int PANEL_BOTTOM_PADDING = 30;
    private static final int PANEL_SIDE_PADDING = 8;

    private final LoreFile file;
    private final Map<ResourceLocation, Boolean> collapsedEntries = new HashMap<>();

    private Button doneButton;
    private Button unlockButton;
    private double scrollAmount;
    private int contentHeight;
    private int totalScrollableHeight;
    private LoreEntry nextUnlockable;

    public LoreGui(LoreFile file) {
        super(Component.translatable(LoreTranslation.FILE_NAME.key(file)));
        this.file = file;
        IgleeModpackUtilities.getModule(LoreModule.class).info("{}",file.id());

    }

    @Override
    protected void init() {
        int doneButtonY = getPanelY() + getPanelHeight() - 24;
        this.doneButton = this.addRenderableWidget(Button.builder(Component.translatable("gui.done"), button -> this.onClose())
                .bounds(this.width / 2 - 50, doneButtonY, 100, 20)
                .build());

        this.unlockButton = this.addWidget(Button.builder(Component.literal("Unlock next entry"), button -> unlockNextEntry())
                .bounds(getListX() + 8, 8, 150, 20)
                .build());

        this.updateUnlockButton();
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics,mouseX,mouseY,partialTick);

        this.updateUnlockButton();

        this.doneButton.setY(getPanelY() + getPanelHeight() - 24);
        this.doneButton.setX(this.width / 2 - 50);

        drawStretchedTexture(guiGraphics, getPanelX(), getPanelY(), getPanelWidth(), getPanelHeight());
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, getPanelY() + 8, 0xFFFFFF);

        int listX = getListX();
        int listY = getListY();
        int listWidth = getListWidth();
        int listHeight = getListHeight();

        renderEntries(guiGraphics, listX, listY, listWidth, listHeight, mouseX, mouseY, partialTick);
        renderScrollbar(guiGraphics, listX, listY, listWidth, listHeight);

        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    private void renderEntries(GuiGraphics guiGraphics, int listX, int listY, int listWidth, int listHeight, int mouseX, int mouseY, float partialTick) {
        List<LoreEntry> unlockedEntries = getUnlockedEntries();
        Player player = this.minecraft != null ? this.minecraft.player : null;

        if (this.totalScrollableHeight > listHeight) {
            listWidth = listWidth - 4;
        }
        this.contentHeight = 0;
        for (LoreEntry entry : unlockedEntries) {
            this.contentHeight += getEntryHeight(entry, listWidth, unlockedEntries);
        }

        // Add height for next unlockable entry or finished message
        int nextUnlockableHeight = 0;
        if (this.nextUnlockable != null && !this.nextUnlockable.canFulfillRequirements(player)) {
            // Has next entry but requirements not fulfilled - show requirements
            nextUnlockableHeight = getNextUnlockableHeight(this.nextUnlockable, listWidth);
        } else if (this.nextUnlockable == null) {
            // No next entry - show finished message
            nextUnlockableHeight = getFinishedHeight();
        }

        int unlockHeight = this.unlockButton != null && this.unlockButton.visible ? this.unlockButton.getHeight() + UNLOCK_BUTTON_GAP : 0;
        this.totalScrollableHeight = LIST_CONTENT_TOP_PADDING + this.contentHeight + nextUnlockableHeight + unlockHeight + LIST_CONTENT_TOP_PADDING;

        this.scrollAmount = Mth.clamp(this.scrollAmount, 0.0D, getMaxScroll(listHeight));


        guiGraphics.enableScissor(listX, listY, listX + listWidth, listY + listHeight);


        int y = listY + LIST_CONTENT_TOP_PADDING - (int) this.scrollAmount;
        guiGraphics.blitNineSlicedSized(ResourceLocation.fromNamespaceAndPath(IgleeModpackUtilities.MODID, "textures/gui/lore/inner_back.png"),listX,y - LIST_CONTENT_TOP_PADDING,listWidth,totalScrollableHeight,4,12,12,0,0,12,12);
        for (LoreEntry entry : unlockedEntries) {
            int entryHeight = getEntryHeight(entry, listWidth, unlockedEntries);
            boolean collapsed = isCollapsed(entry, unlockedEntries);

            drawStretchedTexture(guiGraphics, listX + 4, y, listWidth - 8, ENTRY_HEADER_HEIGHT);

            SoundEvent sound = entry.voiceLocation().flatMap(BuiltInRegistries.SOUND_EVENT::getOptional).orElse(SoundEvents.EMPTY);
            int playButtonX = getPlayButtonX(listX, listWidth);
            if (sound != SoundEvents.EMPTY) {
                int playButtonY = y + 2;
                guiGraphics.fill(playButtonX, playButtonY, playButtonX + ENTRY_PLAY_BUTTON_WIDTH, playButtonY + ENTRY_PLAY_BUTTON_HEIGHT, 0xAA3A3A3A);
                guiGraphics.drawCenteredString(this.font, "Play", playButtonX + ENTRY_PLAY_BUTTON_WIDTH / 2, playButtonY + 2, 0xE0E0E0);
            } else {
                playButtonX = listX + listWidth - 10;
            }

            String icon = collapsed ? "▶" : "▼";
            int titleMaxWidth = Math.max(20, playButtonX - (listX + 10));
            Component titleComponent = Component.literal(icon + " ").append(Component.translatable(LoreTranslation.ENTRY_TITLE.key(this.file, entry)));
            String titleText = this.font.plainSubstrByWidth(titleComponent.getString(), titleMaxWidth);
            guiGraphics.drawString(this.font, titleText, listX + 8, y + 4, 4210752, false);

            if (!collapsed) {
                int textY = y + ENTRY_HEADER_HEIGHT + ENTRY_CONTENT_MARGIN;
                for (String line : entry.lines()) {
                    for (var wrapped : this.font.split(Component.translatable(line), listWidth - 24)) {
                        guiGraphics.drawString(this.font, wrapped, listX + 16, textY, 0xCFCFCF, false);
                        textY += LINE_HEIGHT;
                    }
                }
            }

            y += entryHeight;
        }

        // Render next unlockable requirements or finished message
        if (this.nextUnlockable != null && !this.nextUnlockable.canFulfillRequirements(player)) {
            y += ENTRY_SPACING;
            renderNextUnlockableRequirements(guiGraphics, listX, y, listWidth, this.nextUnlockable);
        } else if (this.nextUnlockable == null) {
            y += ENTRY_SPACING;
            renderFinishedMessage(guiGraphics, listX, y, listWidth);
        }

        if (this.unlockButton != null) {
            this.unlockButton.setY(y);
            if (this.unlockButton.visible) {
                drawStretchedTexture(guiGraphics, this.unlockButton.getX() - 4, this.unlockButton.getY() - 4, this.unlockButton.getWidth() + 8, this.unlockButton.getHeight() + 8);
                this.unlockButton.render(guiGraphics, mouseX, mouseY, partialTick);
            }
        }

        guiGraphics.disableScissor();
    }

    private void renderScrollbar(GuiGraphics guiGraphics, int listX, int listY, int listWidth, int listHeight) {
        if (this.totalScrollableHeight <= listHeight) {
            return;
        }

        int barX = listX + listWidth - 6;
        int barY = listY;
        int barHeight = listHeight;

        guiGraphics.fill(barX, barY, barX + 4, barY + barHeight, 0x80404040);

        int thumbHeight = Math.max(20, (int) ((listHeight / (double) this.totalScrollableHeight) * barHeight));
        thumbHeight = Math.min(thumbHeight, barHeight);
        int maxScroll = Math.max(1, (int) Math.ceil(getMaxScroll(listHeight)));
        int thumbOffset = (int) ((this.scrollAmount / maxScroll) * (barHeight - thumbHeight));
        thumbOffset = Mth.clamp(thumbOffset, 0, barHeight - thumbHeight);

        guiGraphics.fill(barX, barY + thumbOffset, barX + 4, barY + thumbOffset + thumbHeight, 0xC0C0C0C0);
    }

    private int getEntryHeight(LoreEntry entry, int listWidth, List<LoreEntry> unlockedEntries) {
        int height = ENTRY_HEADER_HEIGHT + ENTRY_SPACING;
        if (isCollapsed(entry, unlockedEntries)) {
            return height;
        }

        int linesHeight = 0;
        for (String line : entry.lines()) {
            linesHeight += this.font.split(Component.translatable(line), listWidth - 24).size() * LINE_HEIGHT;
        }
        return height + ENTRY_CONTENT_MARGIN + linesHeight;
    }

    private List<LoreEntry> getUnlockedEntries() {
        Player player = this.minecraft != null ? this.minecraft.player : null;
        LoreProgress progress = getProgress(player);
        List<LoreEntry> unlocked = new ArrayList<>();

        if (progress == null) {
            return unlocked;
        }

        for (LoreEntry entry : this.file.entries()) {
            if (entry.isUnlocked(player)) {
                unlocked.add(entry);
            }
        }
        return unlocked;
    }

    private LoreProgress getProgress(Player player) {
        if (player == null) {
            return null;
        }
        return ClientLoreModule.getInstance().getProgress(player);
    }

    private boolean isCollapsed(LoreEntry entry, List<LoreEntry> unlockedEntries) {
        boolean defaultCollapsed = unlockedEntries.isEmpty() || !unlockedEntries.get(unlockedEntries.size() - 1).equals(entry);
        return this.collapsedEntries.getOrDefault(entry.id(), defaultCollapsed);
    }

    private int getPlayButtonX(int listX, int listWidth) {
        return listX + listWidth - ENTRY_PLAY_BUTTON_WIDTH - 10;
    }

    private double getMaxScroll(int listHeight) {
        return Math.max(0.0D, this.totalScrollableHeight - listHeight);
    }

    private int getPanelX() {
        return getListX() - PANEL_SIDE_PADDING;
    }

    private int getPanelY() {
        return getListY() - PANEL_TOP_PADDING;
    }

    private int getPanelWidth() {
        return getListWidth() + (PANEL_SIDE_PADDING * 2);
    }

    private int getPanelHeight() {
        return getListHeight() + PANEL_TOP_PADDING + PANEL_BOTTOM_PADDING;
    }

    private int getListX() {
        return LIST_MARGIN + (GUI_WIDTH_REDUCTION / 2);
    }

    private int getListY() {
        return 36;
    }

    private int getListWidth() {
        return this.width - LIST_MARGIN * 2 - GUI_WIDTH_REDUCTION;
    }

    private int getListHeight() {
        return this.height - 72 - GUI_HEIGHT_REDUCTION;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double deltaX, double delta) {
        int listY = getListY();
        int listHeight = getListHeight();
        if (mouseY >= listY && mouseY <= listY + listHeight) {
            this.scrollAmount = Mth.clamp(this.scrollAmount - (delta * 14.0D), 0.0D, getMaxScroll(listHeight));
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, deltaX, delta);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && handleEntryClick(mouseX, mouseY)) {
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private boolean handleEntryClick(double mouseX, double mouseY) {
        int listX = getListX();
        int listY = getListY();
        int listWidth = getListWidth();
        int listHeight = getListHeight();

        if (mouseX < listX || mouseX > listX + listWidth || mouseY < listY || mouseY > listY + listHeight) {
            return false;
        }

        int y = listY + LIST_CONTENT_TOP_PADDING - (int) this.scrollAmount;
        List<LoreEntry> unlockedEntries = getUnlockedEntries();
        int playButtonX = getPlayButtonX(listX, listWidth);

        for (LoreEntry entry : unlockedEntries) {
            int entryHeight = getEntryHeight(entry, listWidth, unlockedEntries);
            int playButtonY = y + 2;
            SoundEvent sound = entry.voiceLocation().flatMap(BuiltInRegistries.SOUND_EVENT::getOptional).orElse(SoundEvents.EMPTY);
            if (mouseY >= playButtonY && mouseY <= playButtonY + ENTRY_PLAY_BUTTON_HEIGHT &&
                    mouseX >= playButtonX && mouseX <= playButtonX + ENTRY_PLAY_BUTTON_WIDTH && sound != SoundEvents.EMPTY) {
                playVoice(entry);
                Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                return true;
            }

            if (mouseY >= y && mouseY <= y + ENTRY_HEADER_HEIGHT) {
                this.collapsedEntries.put(entry.id(), !isCollapsed(entry, unlockedEntries));
                Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                return true;
            }
            y += entryHeight;
        }
        return false;
    }

    private void playVoice(LoreEntry entry) {
        SoundEvent sound = entry.voiceLocation().flatMap(BuiltInRegistries.SOUND_EVENT::getOptional).orElse(SoundEvents.EMPTY);
        if (sound != SoundEvents.EMPTY){
            if (LoreSoundHandler.getInstance().isSoundPlaying(sound)){
                LoreSoundHandler.getInstance().stopSound();
            } else {
                LoreSoundHandler.getInstance().playSound(sound);
            }
        }
    }

    private void renderFinishedMessage(GuiGraphics guiGraphics, int listX, int y, int listWidth) {
        drawStretchedTexture(guiGraphics, listX + 4, y, listWidth - 8, ENTRY_HEADER_HEIGHT);
        guiGraphics.drawString(this.font, Component.literal("✓ ").append(Component.translatable(LoreTranslation.FILE_FINISHED.key(this.file))), listX + 8, y + 4, 0xFFD700, false);
    }

    private void renderNextUnlockableRequirements(GuiGraphics guiGraphics, int listX, int y, int listWidth, LoreEntry entry) {
        drawStretchedTexture(guiGraphics, listX + 4, y, listWidth - 8, getNextUnlockableHeight(entry, listWidth) - 4);
        Component requirementsText = Component.literal("⚠ Requirements: ");
        String key = LoreTranslation.ENTRY_REQUIREMENTS.key(this.file, entry);
        boolean translation = Language.getInstance().has(key);
        if (translation) {
            requirementsText = Component.translatable(key);
        }
        guiGraphics.drawString(this.font, requirementsText, listX + 8, y + 4, 0xFF6B6B, false);

        int textY = y + ENTRY_HEADER_HEIGHT + ENTRY_CONTENT_MARGIN;
        if (!translation) {
            for (var requirement : entry.requirements()) {
                String reqStr = "• " + requirement;
                for (var wrapped : this.font.split(Component.literal(reqStr), listWidth - 24)) {
                    guiGraphics.drawString(this.font, wrapped, listX + 16, textY, 0xFFAA00, false);
                    textY += LINE_HEIGHT;
                }
            }
        }
    }

    private int getFinishedHeight() {
        return ENTRY_HEADER_HEIGHT + ENTRY_SPACING;
    }

    private int getNextUnlockableHeight(LoreEntry entry, int listWidth) {
        int height = ENTRY_HEADER_HEIGHT + ENTRY_SPACING;
        int linesHeight = 0;
        String key = LoreTranslation.ENTRY_REQUIREMENTS.key(this.file, entry);
        if (Language.getInstance().has(key)) {
            linesHeight += this.font.split(Component.translatable(key), listWidth - 24).size() * LINE_HEIGHT;
        } else {
            for (var requirement : entry.requirements()) {
                String reqStr = "• " + requirement;
                linesHeight += this.font.split(Component.literal(reqStr), listWidth - 24).size() * LINE_HEIGHT;
            }
        }
        return height + ENTRY_CONTENT_MARGIN + linesHeight;
    }

    private void drawStretchedTexture(GuiGraphics guiGraphics, int x, int y, int width, int height) {
        guiGraphics.blitNineSlicedSized(TEXTURE, x, y, width,height, 4,12,12,0,0,12,12);
    }

    private void updateUnlockButton() {
        this.nextUnlockable = findNextUnlockable();
        Player player = this.minecraft != null ? this.minecraft.player : null;
        boolean canUnlock = this.nextUnlockable != null && this.nextUnlockable.canFulfillRequirements(player);

        if (this.unlockButton != null) {
            this.unlockButton.visible = canUnlock;
            this.unlockButton.active = canUnlock;
        }
    }

    private LoreEntry findNextUnlockable() {
        Player player = this.minecraft != null ? this.minecraft.player : null;
        LoreProgress progress = getProgress(player);
        if (player == null || progress == null) {
            return null;
        }

        LoreEntry previous = null;
        for (LoreEntry entry : this.file.entries()) {
            if (entry.getPreviousEntry().isEmpty()) {
                LoreModule.NET_INSTANCE.sendToServer(new UnlockLoreEntryPacket(this.file.id(), entry.id()));
                previous = entry;
                continue;
            }

            if (progress.hasUnlockedEntry(this.file.id(), entry.id())) {
                previous = entry;
                continue;
            }

            if (previous != null && !progress.hasUnlockedEntry(this.file.id(), previous.id())) {
                return null;
            }

            return entry;
        }
        return null;
    }

    private void unlockNextEntry() {
        if (this.nextUnlockable == null) {
            return;
        }
        collapsedEntries.put(getUnlockedEntries().getLast().id(),true);
        PacketDistributor.sendToServer(new UnlockLoreEntryPacket(this.file.id(), this.nextUnlockable.id()));
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private enum LoreTranslation {
        ENTRY_TITLE("entry.%s.title"),
        ENTRY_REQUIREMENTS("entry.%s.requirements"),
        FILE_NAME("title"),
        FILE_FINISHED("finished");

        private final String suffixPattern;

        LoreTranslation(String suffixPattern) {
            this.suffixPattern = suffixPattern;
        }

        private static String prefix(LoreFile file) {
            return "lore." + file.id().getNamespace() + "." + file.id().getPath() + ".";
        }

        String key(LoreFile file) {
            return prefix(file) + this.suffixPattern;
        }

        String key(LoreFile file, LoreEntry entry) {
            return prefix(file) + this.suffixPattern.formatted(entry.id().getPath());
        }
    }
}
