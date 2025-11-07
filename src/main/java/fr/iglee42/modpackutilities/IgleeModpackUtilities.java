package fr.iglee42.modpackutilities;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.mojang.logging.LogUtils;
import fr.iglee42.modpackutilities.modules.compressed.CompressedModule;
import fr.iglee42.modpackutilities.modules.soils.SoilsModule;
import fr.iglee42.modpackutilities.resourcepack.CustomPackType;
import fr.iglee42.modpackutilities.resourcepack.IMUPackFinder;
import fr.iglee42.modpackutilities.resourcepack.PathConstant;
import fr.iglee42.modpackutilities.utils.Module;
import net.minecraft.server.packs.PackType;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AddPackFindersEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;
import org.slf4j.Logger;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


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
        MODULES.stream().filter(Module::isLoaded).forEach(m->{
            try {
                m.init(modEventBus, MinecraftForge.EVENT_BUS);
            } catch (Exception e){
                m.fatal("Failed to load {} module : {}",m.getName(),e);
            }
        });
    }

    protected static void loadModules() {
        MODULES = new ArrayList<>();
        MODULES.add(new SoilsModule());
        MODULES.add(new CompressedModule());
        configFile = new File(FMLPaths.CONFIGDIR.get().toFile(),MODID+"/modules.json");
        configFile.getParentFile().mkdirs();

        if (configFile.exists()){
            try {
                JsonObject config = new Gson().fromJson(new FileReader(configFile),JsonObject.class);
                MODULES.forEach(m->{
                    if (config.has(m.getName())){
                        if ( config.get(m.getName()).getAsBoolean()) {
                            m.setLoaded(true);
                        }
                    } else {
                        try (FileWriter writer = new FileWriter(configFile)){
                            config.addProperty(m.getName(),true);
                            m.setLoaded(true);
                            writer.write(new GsonBuilder().setPrettyPrinting().create().toJson(config));
                        } catch (IOException ignored) {}
                    }
                });
            } catch (Exception ignored){}
        } else {
            try(FileWriter writer = new FileWriter(configFile)) {

                JsonObject json = new JsonObject();
                MODULES.forEach(m->{
                    json.addProperty(m.getName(),true);
                    m.setLoaded(true);
                });
                writer.write(new GsonBuilder().setPrettyPrinting().create().toJson(json));
            } catch (IOException ignored) {}
        }

    }

    public static boolean isModuleLoaded(Class<? extends Module> clazz) {
        for (Module m : MODULES) {
            if (m.getClass().equals(clazz)) return m.isLoaded();
        }
        return false;
    }

    public static <T extends Module> T getModule(Class<T> clazz) {
        for (Module m : MODULES) {
            if (m.getClass().equals(clazz)) return (T) m;
        }
        return null;
    }

}
