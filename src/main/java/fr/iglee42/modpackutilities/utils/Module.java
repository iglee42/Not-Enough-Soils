package fr.iglee42.modpackutilities.utils;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import fr.iglee42.modpackutilities.IgleeModpackUtilities;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.event.AddReloadListenerEvent;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.function.Consumer;

public abstract class Module {

    private final String name;
    private boolean isLoaded = false;
    private File configFile;

    protected Module(String name,boolean hasConfig) {
        this.name = name;
        if (hasConfig) {
            configFile = new File(FMLPaths.CONFIGDIR.get().toFile(), IgleeModpackUtilities.MODID+"/"+name+".json");
            configFile.getParentFile().mkdirs();
        }
    }

    public void setLoaded(boolean loaded) {
        isLoaded = loaded;
    }

    public void init(IEventBus modEventBus, IEventBus forgeEventBus) throws Exception{
        IgleeModpackUtilities.LOGGER.info("Loading {} module... ",getName());
        if (getReloadListener() != null) forgeEventBus.addListener(this::addReloadListener);
    }

    private void addReloadListener(AddReloadListenerEvent event){
        if (getReloadListener() != null) event.addListener((ResourceManagerReloadListener) resourceManager -> getReloadListener().accept(resourceManager));
    }

    public String getName() {
        return name;
    }

    public boolean isLoaded() {
        return isLoaded;
    }

    public boolean hasConfig(){
        return configFile != null;
    }

    public JsonObject getConfig() {
        if (hasConfig()) {
            if (configFile.exists()) {
                try {
                    return new Gson().fromJson(new FileReader(configFile), JsonObject.class);
                } catch (Exception ignored) {
                }
            } else {
                try (FileWriter writer = new FileWriter(configFile)) {
                    JsonObject defaultConfig = getDefaultConfig();
                    writer.write(new Gson().toJson(defaultConfig));
                    return defaultConfig;
                } catch (IOException ignored) {
                }
            }
        }
        return null;
    }

    public void generateAssetsForPack(){}

    protected JsonObject getDefaultConfig(){
        return new JsonObject();
    }

    protected Consumer<ResourceManager> getReloadListener(){
        return null;
    }
}
