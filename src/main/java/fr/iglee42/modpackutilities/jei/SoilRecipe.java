package fr.iglee42.modpackutilities.jei;

import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import fr.iglee42.modpackutilities.IgleeModpackUtilities;
import fr.iglee42.modpackutilities.modules.soils.MysticalUtils;
import fr.iglee42.modpackutilities.modules.soils.SoilsModule;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;
import net.neoforged.fml.ModList;

public record SoilRecipe(ItemStack seed, ItemStack crux, List<ItemStack> soils) {

    public List<Ingredient> getIngredients() {
        return Lists.newArrayList(Ingredient.of(this.seed),Ingredient.of(this.crux), Ingredient.of(this.soils.toArray(new ItemStack[0])));
    }

    public static List<SoilRecipe> getRecipes() {
        SoilsModule module = IgleeModpackUtilities.getModule(SoilsModule.class);
        if (module == null) return new ArrayList<>();
        Set<Block> blocks = new HashSet<>(module.SOILS.keySet());
        List<SoilRecipe> recipes = new ArrayList<>();
        recipes.addAll(blocks.stream().map(b->new SoilRecipe(b.asItem().getDefaultInstance(),ItemStack.EMPTY, module.SOILS.get(b).stream().map(s->s.asItem().getDefaultInstance()).toList())).toList());
        if (ModList.get().isLoaded("mysticalagriculture")) MysticalUtils.addSeedsToRecipes(blocks,recipes);
        return recipes;
    }
}
