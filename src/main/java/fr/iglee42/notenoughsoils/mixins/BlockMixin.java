package fr.iglee42.notenoughsoils.mixins;

import fr.iglee42.notenoughsoils.NotEnoughSoils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.common.PlantType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(value = Block.class,remap = false)
public class BlockMixin {

    @Inject(method = "canSustainPlant",at = @At(value = "HEAD"),locals = LocalCapture.CAPTURE_FAILSOFT,cancellable = true)
    private void snes$cancelPlacementOnInvalidSoil(BlockState state, BlockGetter world, BlockPos pos, Direction facing, IPlantable plantable, CallbackInfoReturnable<Boolean> cir){
        BlockState plant = plantable.getPlant(world, pos.relative(facing));
        if (!NotEnoughSoils.SOILS.containsKey(plant.getBlock())) return;
        cir.setReturnValue(NotEnoughSoils.SOILS.get(plant.getBlock()).contains(state.getBlock()));
    }

}
