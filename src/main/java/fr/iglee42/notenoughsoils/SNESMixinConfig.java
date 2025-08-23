package fr.iglee42.notenoughsoils;

import java.util.List;
import java.util.Set;

import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.LoadingModList;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

public class SNESMixinConfig implements IMixinConfigPlugin{

    @Override
    public void acceptTargets(Set<String> arg0, Set<String> arg1) {
    }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public void onLoad(String arg0) {
    }

    @Override
    public void postApply(String arg0, ClassNode arg1, String arg2, IMixinInfo arg3) {
    }

    @Override
    public void preApply(String arg0, ClassNode arg1, String arg2, IMixinInfo arg3) {
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        if (targetClassName.contains("jei") && LoadingModList.get().getModFileById("jei") == null ) return false;
        if (mixinClassName.contains("mystical")) return LoadingModList.get().getModFileById("mysticalagriculture") != null;
        if (mixinClassName.contains("mscustomization")) return LoadingModList.get().getModFileById("mysticalcustomization") != null;
        return true;
    }

}