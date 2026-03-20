package fr.iglee42.modpackutilities.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import net.neoforged.fml.loading.FMLPaths;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class ModuleLoader {
    public static Set<Modules> LOADED_MODULES;

    public static void loadModules() {
        List<Modules> modules = new ArrayList<>(Arrays.asList(Modules.values()));
        modules.removeIf(Modules::isDisabled);
        LOADED_MODULES = new HashSet<>();
        File configFile = new File(FMLPaths.CONFIGDIR.get().toFile(),"imu/modules.json");
        configFile.getParentFile().mkdirs();

        if (configFile.exists()){
            try {
                JsonObject config = new Gson().fromJson(new FileReader(configFile),JsonObject.class);
                modules.forEach(m->{
                    if (config.has(m.name().toLowerCase())){
                        if ( config.get(m.name().toLowerCase()).getAsBoolean()) {
                            LOADED_MODULES.add(m);
                        }
                    } else {
                        try (FileWriter writer = new FileWriter(configFile)){
                            config.addProperty(m.name().toLowerCase(),true);
                            LOADED_MODULES.add(m);
                            writer.write(new GsonBuilder().setPrettyPrinting().create().toJson(config));
                        } catch (IOException ignored) {}
                    }
                });
            } catch (Exception ignored){}
        } else {
            try(FileWriter writer = new FileWriter(configFile)) {

                JsonObject json = new JsonObject();
                modules.forEach(m->{
                    json.addProperty(m.name().toLowerCase(),true);
                    LOADED_MODULES.add(m);
                });
                writer.write(new GsonBuilder().setPrettyPrinting().create().toJson(json));
            } catch (IOException ignored) {}
        }

    }

    public enum Modules{
        SOILS,
        COMPRESSED,
        LORE(true)
        ;

        private boolean disabled;

        Modules() {
            this(false);
        }

        Modules(boolean disabled) {
            this.disabled = disabled;
        }

        public boolean isDisabled() {
            return disabled;
        }
    }
}
