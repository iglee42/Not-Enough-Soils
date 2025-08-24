package fr.iglee42.notenoughsoils.jei;

import com.blakebr0.cucumber.util.Localizable;
import com.blakebr0.mysticalagriculture.MysticalAgriculture;
import com.blakebr0.mysticalagriculture.compat.jei.CruxRecipe;
import com.blakebr0.mysticalagriculture.init.ModItems;
import fr.iglee42.notenoughsoils.NotEnoughSoils;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class SoilCategory implements IRecipeCategory<SoilRecipe> {
    public static final RecipeType<SoilRecipe> RECIPE_TYPE = RecipeType.create(NotEnoughSoils.MODID, "soils", SoilRecipe.class);

    private final IDrawable background;
    private final IDrawable icon;
    private final IDrawable slot;

    public SoilCategory(IGuiHelper helper) {
        this.background = helper.createBlankDrawable(18, 54);
        this.icon = helper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(Items.FARMLAND));
        this.slot = helper.getSlotDrawable();
    }

    @Override
    public RecipeType<SoilRecipe> getRecipeType() {
        return RECIPE_TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("category.snes.soils");
    }

    @Override
    public IDrawable getBackground() {
        return this.background;
    }

    @Override
    public IDrawable getIcon() {
        return this.icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, SoilRecipe recipe, IFocusGroup focuses) {
        var inputs = recipe.getIngredients();

        builder.addSlot(RecipeIngredientRole.INPUT, 1, 1).addIngredients(inputs.get(0));
        builder.addSlot(RecipeIngredientRole.INPUT, 1, 19).addIngredients(inputs.get(2));
        if (!inputs.get(1).isEmpty())
            builder.addSlot(RecipeIngredientRole.INPUT, 1, 37).addIngredients(inputs.get(1));
    }

    @Override
    public void draw(SoilRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        IRecipeCategory.super.draw(recipe, recipeSlotsView, guiGraphics, mouseX, mouseY);
        slot.draw(guiGraphics,0,0);
        slot.draw(guiGraphics,0,18);
        if (!recipe.getIngredients().get(1).isEmpty())
            slot.draw(guiGraphics,0,36);
    }
}