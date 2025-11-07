package fr.iglee42.modpackutilities.mixins.compressed;

import com.mojang.datafixers.util.Either;
import com.mojang.logging.LogUtils;
import fr.iglee42.modpackutilities.IgleeModpackUtilities;
import fr.iglee42.modpackutilities.modules.compressed.CompressedModule;
import fr.iglee42.modpackutilities.utils.LangFormatter;
import fr.iglee42.modpackutilities.utils.TextureLayerApplier;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.loading.FMLEnvironment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Mixin(ModelManager.class)
public class ModelManagerMixin {

    @Inject(method = "reload",at=  @At("HEAD"))
    private void compressed$createNewTextures(PreparableReloadListener.PreparationBarrier p_249079_, ResourceManager resourceManager, ProfilerFiller p_250336_, ProfilerFiller p_252324_, Executor p_250550_, Executor p_249221_, CallbackInfoReturnable<CompletableFuture<Void>> cir){
        Map<ResourceLocation, Resource> resources = new HashMap<>();
        resourceManager.listResources("textures", path -> path.getPath().endsWith(".png")).entrySet().stream().filter(e->e.getValue()!= null).map(r->{
            String path = r.getKey().getPath().replace("textures/","").replace(".png","");
            return new AbstractMap.SimpleEntry<>(r.getKey().withPath(path),r.getValue());
        }).forEach(e->resources.put(e.getKey(),e.getValue()));
        CompressedModule module = IgleeModpackUtilities.getModule(CompressedModule.class);
        if (module != null) {
            LogUtils.getLogger().info("Creating textures for {}, your game may lag !", module.getName());
            for (int i = 1; i <= module.getMaxCompressedTiers(); i++) {
                int finalI = i;
                module.getCompressedBlocks().forEach(c -> {
                    if (FMLEnvironment.dist == Dist.CLIENT) {
                        c.getTextures().forEach((s, e) -> {
                            ResourceLocation location = module.getTexture(c, s);
                            if (location != null) {
                                String out = location.getNamespace() + "/" + location.getPath() + "/"+finalI+".png";
                                String layer = compressed$getLayerTexture(module, finalI);
                                try {
                                    TextureLayerApplier.applyLayer(resources::get,location, ResourceLocation.parse(layer), module.getFolderFor("textures/block", true).resolve(out));
                                } catch (IllegalArgumentException ex){
                                    module.warn("Failed to apply layer to {}, it will appear as a bugged texture: {}", location,ex.getMessage());
                                } catch (Exception ex) {
                                    module.warn("Failed to apply layer to {}, it will appear as a bugged texture", location,ex);
                                }
                            }
                        });
                    }
                });
            }
        }

    }

    @Unique
    private static String compressed$getLayerTexture(CompressedModule module, int finalI) {
        Either<String, Map<Integer,String>> layers = module.getLayers() != null ? module.getLayers() : Either.left(CompressedModule.DEFAULT_LAYER);
        Map<String, Object> ctx = Map.of(
                "layer", finalI
        );
        return layers.map(gs-> LangFormatter.format(gs,ctx),
                m-> m.containsKey(finalI) ? m.get(finalI): LangFormatter.format(CompressedModule.DEFAULT_LAYER,ctx));
    }
}
