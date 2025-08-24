package fr.iglee42.notenoughsoils;

import com.blakebr0.mysticalagriculture.api.MysticalAgricultureAPI;
import com.blakebr0.mysticalagriculture.registry.CropRegistry;
import fr.iglee42.notenoughsoils.jei.SoilRecipe;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FarmBlock;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public class MysticalUtils {
    public static void reloadCropsSoils() {
        MysticalAgricultureAPI.getCropRegistry().getCrops().forEach(c->{
            if (c instanceof CropWithSoils casted){
                if (casted.snes$getCustomSoils() != null && !casted.snes$getCustomSoils().isEmpty())NotEnoughSoils.SOILS.put(c.getCropBlock(),casted.snes$getCustomSoils());
            }
        });
    }

    public static void addSeedsToRecipes(Set<Block> blocks, List<SoilRecipe> recipes) {
        CropRegistry.getInstance().getCrops().stream().filter(c->c.getCruxBlock() != null || blocks.contains(c.getCropBlock()))
                .forEach(c->{
                    if (blocks.contains(c.getCropBlock())){
                        Optional<SoilRecipe> oldRecipe = recipes.stream().filter(r->r.seed().is(c.getSeedsItem())).findAny();
                        if (oldRecipe.isPresent() && c.getCruxBlock() != null){
                            SoilRecipe old = oldRecipe.get();
                            recipes.add(new SoilRecipe(c.getSeedsItem().getDefaultInstance(),new ItemStack(c.getCruxBlock()),old.soils()));
                            recipes.remove(old);
                        }
                    } else if (c.getCruxBlock() != null) {
                         recipes.add(new SoilRecipe(c.getSeedsItem().getDefaultInstance(),new ItemStack(c.getCruxBlock()), BuiltInRegistries.BLOCK.stream().filter(b-> b instanceof FarmBlock).map(ItemStack::new).toList()));
                    }
                });
    }
}
