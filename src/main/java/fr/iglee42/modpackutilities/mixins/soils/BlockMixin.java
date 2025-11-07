package fr.iglee42.modpackutilities.mixins.soils;

import fr.iglee42.modpackutilities.IgleeModpackUtilities;
import fr.iglee42.modpackutilities.modules.soils.SoilsModule;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.IPlantable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(value = Block.class,remap = false)
public class BlockMixin {


    @Inject(method = "canSustainPlant",at = @At(value = "HEAD"),locals = LocalCapture.CAPTURE_FAILSOFT,cancellable = true)
    private void snes$cancelPlacementOnInvalidSoil(BlockState state, BlockGetter world, BlockPos pos, Direction facing, IPlantable plantable, CallbackInfoReturnable<Boolean> cir){        SoilsModule module = IgleeModpackUtilities.getModule(SoilsModule.class);
        if (module == null) return;
        if (!module.isLoaded()) return;
        BlockState plant = plantable.getPlant(world, pos.relative(facing));
        if (!module.SOILS.containsKey(plant.getBlock())) return;
        cir.setReturnValue(module.SOILS.get(plant.getBlock()).contains(state.getBlock()));
    }

}
