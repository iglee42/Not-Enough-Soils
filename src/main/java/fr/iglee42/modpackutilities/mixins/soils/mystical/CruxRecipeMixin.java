package fr.iglee42.modpackutilities.mixins.soils.mystical;

import com.blakebr0.mysticalagriculture.compat.jei.CruxRecipe;
import com.google.common.collect.Lists;
import fr.iglee42.modpackutilities.IgleeModpackUtilities;
import fr.iglee42.modpackutilities.modules.soils.SoilsModule;
import fr.iglee42.modpackutilities.utils.RequiresMods;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@RequiresMods({"mysticalagriculture","jei"})
@Mixin(value = CruxRecipe.class,remap = false)
public class CruxRecipeMixin {

    @Shadow @Final public ItemStack seed;

    @Shadow @Final public ItemStack crux;

    @Inject(method = "getIngredients",at = @At("HEAD"),cancellable = true)
    private void snes$modifySoilInCruxRecipe(CallbackInfoReturnable<List<Ingredient>> cir){
        ItemStack seed = this.seed;
        SoilsModule module = IgleeModpackUtilities.getModule(SoilsModule.class);
        if (module == null) return;
        if (!module.isLoaded()) return;
        if (seed.getItem() instanceof BlockItem bi && module.SOILS.containsKey(bi.getBlock())){
            cir.setReturnValue( Lists.newArrayList(Ingredient.of(this.seed), Ingredient.of(module.SOILS.get(bi.getBlock()).stream().map(ItemStack::new)), Ingredient.of(new ItemStack[]{this.crux})));
        }
    }
}
