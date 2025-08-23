package fr.iglee42.notenoughsoils.mixins.mystical;

import com.blakebr0.mysticalagriculture.block.MysticalCropBlock;
import com.blakebr0.mysticalagriculture.compat.jei.CruxRecipe;
import com.google.common.collect.Lists;
import fr.iglee42.notenoughsoils.NotEnoughSoils;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(value = CruxRecipe.class,remap = false)
public class CruxRecipeMixin {

    @Shadow @Final public ItemStack seed;

    @Shadow @Final public ItemStack crux;

    @Inject(method = "getIngredients",at = @At("HEAD"),cancellable = true)
    private void snes$modifySoilInCruxRecipe(CallbackInfoReturnable<List<Ingredient>> cir){
        ItemStack seed = this.seed;
        if (seed.getItem() instanceof BlockItem bi && NotEnoughSoils.SOILS.containsKey(bi.getBlock())){
            cir.setReturnValue( Lists.newArrayList(Ingredient.of(this.seed), Ingredient.of(NotEnoughSoils.SOILS.get(bi.getBlock()).stream().map(ItemStack::new)), Ingredient.of(new ItemStack[]{this.crux})));
        }
    }
}
