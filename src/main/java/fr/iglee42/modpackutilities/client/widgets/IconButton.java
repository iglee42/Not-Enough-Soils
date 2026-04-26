package fr.iglee42.modpackutilities.client.widgets;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.function.Function;

public class IconButton extends AbstractButton {

    protected final IconButton.OnPress onPress;
    protected final ResourceLocation icon;

    public static IconButton.Builder builder(ResourceLocation icon , IconButton.OnPress onPress) {
        return new IconButton.Builder(icon, onPress);
    }

    protected IconButton(int y, int x, OnPress onPress, ResourceLocation icon) {
        super(y, x, 20, 20, Component.empty());
        this.onPress = onPress;
        this.icon = icon;
    }

    protected IconButton(IconButton.Builder builder) {
        this(builder.x, builder.y, builder.onPress, builder.icon);
    }

    public void onPress() {
        this.onPress.onPress(this);
    }

    protected MutableComponent createNarrationMessage() {
        return Component.empty();
    }

    public void updateWidgetNarration(NarrationElementOutput p_259196_) {
        this.defaultButtonNarrationText(p_259196_);
    }

    @Override
    protected void renderWidget(GuiGraphics graphics, int p_282682_, int p_281714_, float p_282542_) {
        super.renderWidget(graphics, p_282682_, p_281714_, p_282542_);
        graphics.blit(icon,getX() + 2,getY() + 2,0,0,0,16,16,16,16);
    }

    @Override
    public void renderString(GuiGraphics p_283366_, Font p_283054_, int p_281656_) {}

    @OnlyIn(Dist.CLIENT)
    public static class Builder {
        private final ResourceLocation icon;
        private final IconButton.OnPress onPress;
        private int x;
        private int y;

        public Builder(ResourceLocation icon, IconButton.OnPress onPress) {
            this.icon = icon;
            this.onPress = onPress;
        }

        public IconButton.Builder pos(int p_254538_, int p_254216_) {
            this.x = p_254538_;
            this.y = p_254216_;
            return this;
        }

        public IconButton build() {
            return build(IconButton::new);
        }

        public IconButton build(Function<Builder, IconButton> builder) {
            return builder.apply(this);
        }
    }
    @OnlyIn(Dist.CLIENT)
    public interface OnPress {
        void onPress(IconButton btn);
    }
}
