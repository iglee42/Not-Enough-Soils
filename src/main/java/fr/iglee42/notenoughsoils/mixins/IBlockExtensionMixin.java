package fr.iglee42.notenoughsoils.mixins;

import fr.iglee42.notenoughsoils.NotEnoughSoils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.extensions.IBlockExtension;
import net.neoforged.neoforge.common.util.TriState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(value = IBlockExtension.class,remap = false)
public interface IBlockExtensionMixin {

    /**
     * @author iglee42
     * @reason Disable if snes soil not match
     */
    @Overwrite
    default TriState canSustainPlant(BlockState state, BlockGetter level, BlockPos soilPosition, Direction facing, BlockState plant) {
        if (!NotEnoughSoils.SOILS.containsKey(plant.getBlock())) return TriState.DEFAULT;
        return NotEnoughSoils.SOILS.get(plant.getBlock()).contains(state.getBlock())? TriState.TRUE : TriState.FALSE;
    }

}
