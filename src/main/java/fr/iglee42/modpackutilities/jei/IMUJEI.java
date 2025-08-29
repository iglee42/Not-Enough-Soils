package fr.iglee42.modpackutilities.jei;

import fr.iglee42.modpackutilities.IgleeModpackUtilities;
import fr.iglee42.modpackutilities.modules.soils.SoilsModule;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.resources.ResourceLocation;

@JeiPlugin
public class IMUJEI implements IModPlugin {

    @Override
    public ResourceLocation getPluginUid() {
        return ResourceLocation.fromNamespaceAndPath("igleemods","snes");
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        if (IgleeModpackUtilities.isModuleLoaded(SoilsModule.class)) registration.addRecipeCategories(new SoilCategory(registration.getJeiHelpers().getGuiHelper()));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        if (IgleeModpackUtilities.isModuleLoaded(SoilsModule.class)) registration.addRecipes(SoilCategory.RECIPE_TYPE,SoilRecipe.getRecipes());
    }
}
