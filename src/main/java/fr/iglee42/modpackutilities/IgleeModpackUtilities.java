package fr.iglee42.modpackutilities;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.mojang.logging.LogUtils;
import fr.iglee42.modpackutilities.modules.soils.SoilsModule;
import fr.iglee42.modpackutilities.resourcepack.IMUPackFinder;
import fr.iglee42.modpackutilities.resourcepack.CustomPackType;
import fr.iglee42.modpackutilities.resourcepack.PathConstant;
import net.minecraft.client.Minecraft;
import net.minecraft.server.packs.PackType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddPackFindersEvent;
import org.slf4j.Logger;

import java.io.*;
import java.util.*;

import fr.iglee42.modpackutilities.utils.Module;


@Mod(IgleeModpackUtilities.MODID)
public class IgleeModpackUtilities {

    public static final String MODID = "imu";
    public static final Logger LOGGER = LogUtils.getLogger();
    public static List<Module> MODULES;
    private static File configFile;

    public IgleeModpackUtilities(IEventBus modEventBus) {
        initModules(modEventBus);
        modEventBus.addListener(this::registerPackRepo);

        PathConstant.init();

        //try {
        //    if (FMLEnvironment.dist == Dist.CLIENT) {
        //        Minecraft.getInstance().getResourcePackRepository().addPackFinder(new IMUPackFinder(CustomPackType.RESOURCE));
        //    }
        //} catch (Exception ignored) {}
    }

    private void registerPackRepo(AddPackFindersEvent event){
        if (event.getPackType() == PackType.CLIENT_RESOURCES) event.addRepositorySource(new IMUPackFinder(CustomPackType.RESOURCE));
        else event.addRepositorySource(new IMUPackFinder(CustomPackType.DATA));
    }

    private static void initModules(IEventBus modEventBus) {
        MODULES.stream().filter(Module::isLoaded).forEach(m->m.init(modEventBus, NeoForge.EVENT_BUS));
    }

    protected static void loadModules() {
        MODULES = new ArrayList<>();
        MODULES.add(new SoilsModule());
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
                            writer.write(new Gson().toJson(config));
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
