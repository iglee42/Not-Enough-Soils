package fr.iglee42.modpackutilities;

import java.io.InputStream;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import fr.iglee42.modpackutilities.utils.Module;
import fr.iglee42.modpackutilities.utils.RequiresMods;
import net.neoforged.fml.loading.LoadingModList;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

public class IMUMixinConfig implements IMixinConfigPlugin{

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
        IgleeModpackUtilities.loadModules();
    }

    @Override
    public void postApply(String arg0, ClassNode arg1, String arg2, IMixinInfo arg3) {
    }

    @Override
    public void preApply(String arg0, ClassNode arg1, String arg2, IMixinInfo arg3) {
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        try {
            String resourcePath = mixinClassName.replace('.', '/') + ".class";
            InputStream in = getClass().getClassLoader().getResourceAsStream(resourcePath);
            if (in == null) return false;

            ClassReader reader = new ClassReader(in);
            ClassNode classNode = new ClassNode();
            reader.accept(classNode, 0);

            boolean requiresModsOk = true;

            if (classNode.visibleAnnotations != null) {
                for (AnnotationNode annotation : classNode.visibleAnnotations) {
                    if (annotation.desc.equals("Lfr/iglee42/modpackutilities/utils/RequiresMods;")) {
                        @SuppressWarnings("unchecked")
                        List<String> mods = (List<String>) annotation.values.get(1);
                        for (String mod : mods) {
                            if (LoadingModList.get().getModFileById(mod) == null) {
                                requiresModsOk = false;
                                break;
                            }
                        }
                    }
                }
            }

            if (!requiresModsOk) return false;

            AtomicBoolean canBeLoaded = new AtomicBoolean(true);
            Optional<Module> module = IgleeModpackUtilities.MODULES.stream().filter(m->mixinClassName.contains(m.getName())).findAny();
            module.ifPresent(m-> canBeLoaded.set(m.isLoaded()));
            return canBeLoaded.get();
        } catch (Exception ignored) {
            return false;
        }
    }

}