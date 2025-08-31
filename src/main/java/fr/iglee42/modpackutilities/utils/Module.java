package fr.iglee42.modpackutilities.utils;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.mojang.logging.LogUtils;
import fr.iglee42.modpackutilities.IgleeModpackUtilities;
import fr.iglee42.modpackutilities.resourcepack.PathConstant;
import fr.iglee42.modpackutilities.resourcepack.generation.TextureKey;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.event.AddReloadListenerEvent;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
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

    protected Path getFolderFor(String folder, boolean isAssets){
        return (isAssets ? PathConstant.BASE_ASSETS_PATH : PathConstant.BASE_DATA_PATH).resolve(getName() + "/" +folder);
    }

    protected void blockstate(String name,String model){
        try {
            File file = new File(getFolderFor("blockstates",true).toFile(), name+".json");
            file.getParentFile().mkdirs();
            FileWriter writer = new FileWriter(file);
            writer.write("{\n" +
                    "  \"variants\": {\n" +
                    "    \"\": {\n" +
                    "      \"model\": \""+model+"\"\n" +
                    "    }\n" +
                    "  }\n" +
                    "}");
            writer.close();
        } catch (Exception exception){
            LogUtils.getLogger().error("An error was detected when a blockstate generating for {} module",getName(),exception);
        }
    }
    protected void model(String type, String name, String parent, TextureKey[] textureKeys){
        try {
            File file = new File(getFolderFor("models/"+type,true).toFile(), name+".json");
            file.getParentFile().mkdirs();
            String jsonBase =   "{\n"+
                    "   \"parent\": \""+ parent +"\""+(textureKeys.length > 0 ? ",":"")+"\n";
            StringBuilder builder = new StringBuilder(jsonBase);
            if (textureKeys.length > 0){
                builder.append("   \"textures\": {\n");
                for (int i = 0; i < textureKeys.length; i++){
                    builder.append(textureKeys[i].toJson());
                    if (i != textureKeys.length - 1) builder.append(",");
                    builder.append("\n");
                }
                builder.append("    }\n");
            }
            builder.append("}");
            FileWriter writer = new FileWriter(file);
            writer.write(builder.toString());
            writer.close();
        } catch (Exception exception){
            LogUtils.getLogger().error("An error was detected when a model generating for {} module",getName(),exception);
        }
    }

}
