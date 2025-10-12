package fr.iglee42.modpackutilities.utils;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.logging.LogUtils;
import fr.iglee42.igleelib.api.utils.ModsUtils;
import fr.iglee42.modpackutilities.IgleeModpackUtilities;
import fr.iglee42.modpackutilities.resourcepack.PathConstant;
import fr.iglee42.modpackutilities.resourcepack.generation.TextureKey;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.FileAppender;
import org.apache.logging.log4j.core.appender.RollingFileAppender;
import org.apache.logging.log4j.core.appender.rolling.TimeBasedTriggeringPolicy;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public abstract class Module {

    private final String name;
    private boolean isLoaded = false;
    private File configFile;
    private final Map<String,String> langs;
    private final Logger logger;

    protected Module(String name, boolean hasConfig) {
        this.name = name;
        if (hasConfig) {
            configFile = new File(FMLPaths.CONFIGDIR.get().toFile(), IgleeModpackUtilities.MODID+"/"+name+".json");
            configFile.getParentFile().mkdirs();
        }
        langs = new HashMap<>();
        logger = createModuleLogger(name,Path.of("logs/"+IgleeModpackUtilities.MODID+"/"));
    }

    public void setLoaded(boolean loaded) {
        isLoaded = loaded;
    }

    public void init(IEventBus modEventBus, IEventBus forgeEventBus) throws Exception{
        info("Initializing {} module... ",getName());
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

    public Path getFolderFor(String folder, boolean isAssets){
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
    protected void model(String type, String name, String parent, TextureKey[] textureKeys,@NotNull String renderType){
        try {
            File file = new File(getFolderFor("models/"+type,true).toFile(), name+".json");
            file.getParentFile().mkdirs();
            String jsonBase =   "{\n"+
                    "   \"parent\": \""+ parent +"\""+(textureKeys.length > 0 || !renderType.isEmpty() ? ",":"")+"\n";
            StringBuilder builder = new StringBuilder(jsonBase);
            if (textureKeys.length > 0){
                builder.append("   \"textures\": {\n");
                for (int i = 0; i < textureKeys.length; i++){
                    builder.append(textureKeys[i].toJson());
                    if (i != textureKeys.length - 1) builder.append(",");
                    builder.append("\n");
                }
                builder.append("    }").append(renderType.isEmpty() ? "" : ",").append("\n");
            }
            if (!renderType.isEmpty()){
                builder.append("   \"render_type\": \"").append(renderType).append("\"\n");
            }
            builder.append("}");
            FileWriter writer = new FileWriter(file);
            writer.write(builder.toString());
            writer.close();
        } catch (Exception exception){
            LogUtils.getLogger().error("An error was detected when a model generating for {} module",getName(),exception);
        }
    }

    protected void recipe(String name, String type, JsonObject otherInfos){
        try {
            File file = new File(getFolderFor("recipe",false).toFile(), name+".json");
            file.getParentFile().mkdirs();
            String jsonBase =   "{\n"+
                    "   \"type\": \""+ type +"\""+(!otherInfos.keySet().isEmpty() ? ",":"")+"\n";
            StringBuilder builder = new StringBuilder(jsonBase);
            for (int i = 0; i < otherInfos.keySet().size(); i++) {
                JsonElement e = otherInfos.get(otherInfos.keySet().stream().toList().get(i));
                builder.append("   \"").append(otherInfos.keySet().stream().toList().get(i)).append("\": ");
                builder.append(new Gson().toJson(e));
                builder.append(i < otherInfos.keySet().size() - 1 ? ",":"").append("\n");
            }
            builder.append("}");
            FileWriter writer = new FileWriter(file);
            writer.write(builder.toString());
            writer.close();
        } catch (Exception exception){
            LogUtils.getLogger().error("An error was detected when a recipe generating for {} module",getName(),exception);
        }
    }

    protected void lang(String key, String value){
        langs.put(key,value);
    }

    public void generateLangFile(){
        try {
            File file = new File(getFolderFor("lang",true).toFile(), "en_us.json");
            file.getParentFile().mkdirs();
            JsonObject json = new JsonObject();
            langs.forEach(json::addProperty);
            try (FileWriter writer = new FileWriter(file)){
                writer.write(new Gson().toJson(json));
            }
        } catch (Exception exception){
            LogUtils.getLogger().error("An error was detected when a lang file generating for {} module",getName(),exception);
        }

    }

    private static Logger createModuleLogger(String moduleName,Path logDir) {
        logDir.toFile().mkdirs();
        LoggerContext context = (LoggerContext) LogManager.getContext(false);

        PatternLayout layout = PatternLayout.newBuilder()
                .withPattern("[%d{HH:mm:ss}] [%t/%level] %msg%n")
                .build();

        Path logFile = logDir.resolve(moduleName + ".log");

        FileAppender appender = FileAppender.newBuilder()
                .setName(moduleName + "FileAppender")
                .withFileName(logFile.toString())
                .withAppend(false)
                .setLayout(layout)
                .setConfiguration(context.getConfiguration())
                .build();

        appender.start();

        org.apache.logging.log4j.core.Logger coreLogger =
                ( org.apache.logging.log4j.core.Logger) LogManager.getLogger(ModsUtils.getUpperName(moduleName,"_"));
        coreLogger.setAdditive(true);
        coreLogger.addAppender(appender);

        return coreLogger;
    }

    public void log(Level level,String message, Object... params){
        logger.log(level,message,params);
    }

    public void error(String message, Object... params){
        log(Level.ERROR,message,params);
    }
    public void warn(String message, Object... params){
        log(Level.WARN,message,params);
    }
    public void info(String message, Object... params){
        log(Level.INFO,message,params);
    }
    public void fatal(String message, Object... params){
        log(Level.FATAL,message,params);
    }

}
