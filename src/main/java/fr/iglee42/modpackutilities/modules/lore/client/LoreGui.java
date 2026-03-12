package fr.iglee42.modpackutilities.modules.lore.client;

import fr.iglee42.modpackutilities.IgleeModpackUtilities;
import fr.iglee42.modpackutilities.modules.lore.ClientLoreModule;
import fr.iglee42.modpackutilities.modules.lore.LoreEntry;
import fr.iglee42.modpackutilities.modules.lore.LoreFile;
import fr.iglee42.modpackutilities.modules.lore.LoreModule;
import fr.iglee42.modpackutilities.modules.lore.network.UnlockLoreEntryPacket;
import fr.iglee42.modpackutilities.modules.lore.progress.LoreProgress;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LoreGui extends Screen {

    private static final int LIST_MARGIN = 20;
    private static final int ENTRY_HEADER_HEIGHT = 16;
    private static final int ENTRY_PADDING = 5;
    private static final int ENTRY_SPACING = 6;
    private static final int LINE_HEIGHT = 10;

    private final LoreFile file;
    private final Map<ResourceLocation, Boolean> collapsedEntries = new HashMap<>();

    private Button unlockButton;
    private double scrollAmount;
    private int contentHeight;
    private LoreEntry nextUnlockable;

    public LoreGui(LoreFile file) {
        super(Component.literal(file.name()));
        this.file = file;
        IgleeModpackUtilities.getModule(LoreModule.class).info("{}",file.id());

    }

    @Override
    protected void init() {
        int buttonY = this.height - 28;
        this.addRenderableWidget(Button.builder(Component.translatable("gui.done"), button -> this.onClose())
                .bounds(this.width / 2 - 50, buttonY, 100, 20)
                .build());

        this.unlockButton = this.addRenderableWidget(Button.builder(Component.literal("Unlock next entry"), button -> unlockNextEntry())
                .bounds(this.width - 160, 8, 150, 20)
                .build());

        this.updateUnlockButton();
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics);

        this.updateUnlockButton();

        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 10, 0xFFFFFF);

        int listX = LIST_MARGIN;
        int listY = 36;
        int listWidth = this.width - LIST_MARGIN * 2;
        int listHeight = this.height - 72;

        guiGraphics.fill(listX - 1, listY - 1, listX + listWidth + 1, listY + listHeight + 1, 0xFF555555);
        guiGraphics.fill(listX, listY, listX + listWidth, listY + listHeight, 0xCC101010);

        renderEntries(guiGraphics, listX, listY, listWidth, listHeight);
        renderScrollbar(guiGraphics, listX, listY, listWidth, listHeight);

        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    private void renderEntries(GuiGraphics guiGraphics, int listX, int listY, int listWidth, int listHeight) {
        List<LoreEntry> unlockedEntries = getUnlockedEntries();

        IgleeModpackUtilities.getModule(LoreModule.class).info("{}",unlockedEntries);
        this.contentHeight = 0;
        for (LoreEntry entry : unlockedEntries) {
            this.contentHeight += getEntryHeight(entry, listWidth);
        }

        this.scrollAmount = Mth.clamp(this.scrollAmount, 0.0D, getMaxScroll(listHeight));

        guiGraphics.enableScissor(listX, listY, listX + listWidth, listY + listHeight);

        int y = listY + 4 - (int) this.scrollAmount;
        for (LoreEntry entry : unlockedEntries) {
            int entryHeight = getEntryHeight(entry, listWidth);
            boolean collapsed = isCollapsed(entry);

            guiGraphics.fill(listX + 4, y, listX + listWidth - 4, y + ENTRY_HEADER_HEIGHT, 0xAA2A2A2A);
            String icon = collapsed ? ">" : "v";
            guiGraphics.drawString(this.font, icon + " " + entry.id(), listX + 8, y + 4, 0xE0E0E0, false);

            if (!collapsed) {
                int textY = y + ENTRY_HEADER_HEIGHT + 2;
                for (String line : entry.lines()) {
                    for (var wrapped : this.font.split(Component.literal(line), listWidth - 16)) {
                        guiGraphics.drawString(this.font, wrapped, listX + 8, textY, 0xCFCFCF, false);
                        textY += LINE_HEIGHT;
                    }
                }
            }

            y += entryHeight;
        }

        guiGraphics.disableScissor();
    }

    private void renderScrollbar(GuiGraphics guiGraphics, int listX, int listY, int listWidth, int listHeight) {
        if (this.contentHeight <= listHeight) {
            return;
        }

        int barX = listX + listWidth - 6;
        int barY = listY;
        int barHeight = listHeight;

        guiGraphics.fill(barX, barY, barX + 4, barY + barHeight, 0x80404040);

        int thumbHeight = Math.max(20, (int) ((listHeight / (double) this.contentHeight) * barHeight));
        int maxScroll = Math.max(1, this.contentHeight - listHeight);
        int thumbOffset = (int) ((this.scrollAmount / maxScroll) * (barHeight - thumbHeight));

        guiGraphics.fill(barX, barY + thumbOffset, barX + 4, barY + thumbOffset + thumbHeight, 0xC0C0C0C0);
    }

    private int getEntryHeight(LoreEntry entry, int listWidth) {
        int height = ENTRY_HEADER_HEIGHT + ENTRY_PADDING + ENTRY_SPACING;
        if (isCollapsed(entry)) {
            return height;
        }

        int linesHeight = 0;
        for (String line : entry.lines()) {
            linesHeight += this.font.split(Component.literal(line), listWidth - 16).size() * LINE_HEIGHT;
        }
        return height + linesHeight;
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

    private boolean isCollapsed(LoreEntry entry) {
        return this.collapsedEntries.getOrDefault(entry.id(), true);
    }

    private double getMaxScroll(int listHeight) {
        return Math.max(0, this.contentHeight - listHeight + 8);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        int listY = 36;
        int listHeight = this.height - 72;
        if (mouseY >= listY && mouseY <= listY + listHeight) {
            this.scrollAmount = Mth.clamp(this.scrollAmount - (delta * 14.0D), 0.0D, getMaxScroll(listHeight));
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && handleEntryClick(mouseX, mouseY)) {
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private boolean handleEntryClick(double mouseX, double mouseY) {
        int listX = LIST_MARGIN;
        int listY = 36;
        int listWidth = this.width - LIST_MARGIN * 2;
        int listHeight = this.height - 72;

        if (mouseX < listX || mouseX > listX + listWidth || mouseY < listY || mouseY > listY + listHeight) {
            return false;
        }

        int y = listY + 4 - (int) this.scrollAmount;
        for (LoreEntry entry : getUnlockedEntries()) {
            int entryHeight = getEntryHeight(entry, listWidth);
            if (mouseY >= y && mouseY <= y + ENTRY_HEADER_HEIGHT) {
                this.collapsedEntries.put(entry.id(), !isCollapsed(entry));
                return true;
            }
            y += entryHeight;
        }
        return false;
    }

    private void updateUnlockButton() {
        this.nextUnlockable = findNextUnlockable();
        boolean canUnlock = this.nextUnlockable != null;

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
            if (progress.hasUnlockedEntry(this.file.id(), entry.id())) {
                previous = entry;
                continue;
            }

            if (previous != null && !progress.hasUnlockedEntry(this.file.id(), previous.id())) {
                return null;
            }

            return entry.canFulfillRequirements(player) ? entry : null;
        }
        return null;
    }

    private void unlockNextEntry() {
        if (this.nextUnlockable == null || LoreModule.NET_INSTANCE == null) {
            return;
        }

        LoreModule.NET_INSTANCE.sendToServer(new UnlockLoreEntryPacket(this.file.id(), this.nextUnlockable.id()));
    }
}
