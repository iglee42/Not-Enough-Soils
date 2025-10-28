package fr.iglee42.modpackutilities.mixins.soils.mystical;

import com.blakebr0.mysticalagriculture.api.crop.Crop;
import fr.iglee42.modpackutilities.modules.soils.CropWithSoils;
import fr.iglee42.modpackutilities.utils.RequiresMods;
import net.minecraft.world.level.block.Block;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.List;

@RequiresMods({"mysticalagriculture"})
@Mixin(value = Crop.class,remap = false)
public class CropMixin implements CropWithSoils {

    @Unique
    private List<Block> snes$customSoils;

    @Override
    public List<Block> snes$getCustomSoils() {
        return snes$customSoils;
    }

    @Override
    public void snes$setCustomSoils(List<Block> soils) {
        this.snes$customSoils = soils;
    }
}
