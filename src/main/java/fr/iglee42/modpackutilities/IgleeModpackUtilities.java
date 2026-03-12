package fr.iglee42.modpackutilities;

import com.mojang.logging.LogUtils;
import fr.iglee42.modpackutilities.modules.compressed.CompressedModule;
import fr.iglee42.modpackutilities.modules.lore.LoreModule;
import fr.iglee42.modpackutilities.modules.soils.SoilsModule;
import fr.iglee42.modpackutilities.resourcepack.CustomPackType;
import fr.iglee42.modpackutilities.resourcepack.IMUPackFinder;
import fr.iglee42.modpackutilities.resourcepack.PathConstant;
import fr.iglee42.modpackutilities.utils.Module;
import fr.iglee42.modpackutilities.utils.ModuleLoader;
import net.minecraft.server.packs.PackType;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AddPackFindersEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

import java.io.File;
import java.util.*;


@Mod(IgleeModpackUtilities.MODID)
public class IgleeModpackUtilities {

    public static final String MODID = "imu";
    public static final Logger LOGGER = LogUtils.getLogger();
    public static List<Module> MODULES;
    private static File configFile;

    public IgleeModpackUtilities() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        initModules(modEventBus);
        modEventBus.addListener(this::registerPackRepo);

        PathConstant.init();
    }

    private void registerPackRepo(AddPackFindersEvent event){
        if (event.getPackType() == PackType.CLIENT_RESOURCES) event.addRepositorySource(new IMUPackFinder(CustomPackType.RESOURCE));
        else event.addRepositorySource(new IMUPackFinder(CustomPackType.DATA));
    }

    private static void initModules(IEventBus modEventBus) {
        MODULES = new ArrayList<>();
        ModuleLoader.LOADED_MODULES.forEach(type->{
            Module m = switch (type){
                case LORE -> new LoreModule(type,"lore");
                case COMPRESSED -> new CompressedModule(type,"compressed");
                case SOILS -> new SoilsModule(type,"soils");
            };
            try {
                m.init(modEventBus, MinecraftForge.EVENT_BUS);
            } catch (Exception e){
                m.fatal("Failed to load {} module : {}",m.getName(),e);
            } finally {
                MODULES.add(m);
            }
        });
    }



    public static boolean isModuleLoaded(Class<? extends Module> clazz) {
        for (Module m : MODULES) {
            if (m.getClass().equals(clazz)) return isModuleLoaded(m.getType());
        }
        return false;
    }

    public static boolean isModuleLoaded(ModuleLoader.Modules module) {
        return ModuleLoader.LOADED_MODULES.contains(module);
    }

    public static <T extends Module> T getModule(Class<T> clazz) {
        for (Module m : MODULES) {
            if (m.getClass().equals(clazz)) return (T) m;
        }
        return null;
    }



}
