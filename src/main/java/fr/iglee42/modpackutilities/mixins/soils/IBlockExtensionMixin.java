package fr.iglee42.modpackutilities.mixins.soils;

import fr.iglee42.modpackutilities.IgleeModpackUtilities;
import fr.iglee42.modpackutilities.modules.soils.SoilsModule;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.extensions.IBlockExtension;
import net.neoforged.neoforge.common.util.TriState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(value = IBlockExtension.class,remap = false)
public interface IBlockExtensionMixin {

    /**
     * @author iglee42
     * @reason Disable if snes soil not match
     */
    @Overwrite
    default TriState canSustainPlant(BlockState state, BlockGetter level, BlockPos soilPosition, Direction facing, BlockState plant) {
        SoilsModule module = IgleeModpackUtilities.getModule(SoilsModule.class);
        if (module == null) return TriState.DEFAULT;
        if (!module.isLoaded()) return TriState.DEFAULT;
        if (!module.SOILS.containsKey(plant.getBlock())) return TriState.DEFAULT;
        return module.SOILS.get(plant.getBlock()).contains(state.getBlock())? TriState.TRUE : TriState.FALSE;
    }

}
