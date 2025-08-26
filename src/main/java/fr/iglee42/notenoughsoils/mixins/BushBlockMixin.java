package fr.iglee42.notenoughsoils.mixins;

import fr.iglee42.notenoughsoils.NotEnoughSoils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(value = BushBlock.class,remap = false)
public class BushBlockMixin {

    @Inject(method = "canSurvive",at = @At("RETURN"),cancellable = true,locals = LocalCapture.CAPTURE_FAILSOFT)
    private void snes$cancelPlacementOnInvalidSoil(BlockState state, LevelReader level, BlockPos p_51030_, CallbackInfoReturnable<Boolean> cir, BlockPos below){
        if (!NotEnoughSoils.SOILS.containsKey(state.getBlock())) return;
        cir.setReturnValue(NotEnoughSoils.SOILS.get(state.getBlock()).contains(level.getBlockState(below).getBlock()));
    }

}
