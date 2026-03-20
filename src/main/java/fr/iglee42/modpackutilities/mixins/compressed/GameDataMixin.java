package fr.iglee42.modpackutilities.mixins.compressed;

import fr.iglee42.modpackutilities.IgleeModpackUtilities;
import fr.iglee42.modpackutilities.modules.compressed.CompressedModule;
import net.neoforged.neoforge.registries.GameData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = GameData.class,remap = false)
public class GameDataMixin {

    @Inject(method = "postRegisterEvents",at = @At(value = "INVOKE", target = "Lnet/neoforged/neoforge/common/CommonHooks;modifyAttributes()V"))
    private static void compressed$validateBlocks(CallbackInfo ci){
        if (IgleeModpackUtilities.isModuleLoaded(CompressedModule.class) && IgleeModpackUtilities.getModule(CompressedModule.class) != null){
            CompressedModule module = IgleeModpackUtilities.getModule(CompressedModule.class);
            module.validateBlocks();
        }
    }
}
