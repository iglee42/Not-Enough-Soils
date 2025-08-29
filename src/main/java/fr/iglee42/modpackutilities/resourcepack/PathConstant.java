package fr.iglee42.modpackutilities.resourcepack;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.platform.NativeImage;
import fr.iglee42.modpackutilities.IgleeModpackUtilities;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.resources.IoSupplier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.common.util.Size2i;
import net.neoforged.neoforge.resource.ResourcePackLoader;
import org.apache.commons.lang3.tuple.Pair;

import java.io.*;
import java.nio.file.Path;
import java.util.Optional;

public class PathConstant {

    public static Path ROOT_PATH;
    public static Path ASSETS_PATH;
    public static Path DATAS_PATH;

    public static Path LANGS_PATH;
    public static Path RECIPES_PATH;
    public static Path LOOT_TABLES_PATH;
    public static Path TAGS_PATH;

    public static Path ITEMS_TAGS_PATH;
    public static Path ENTITY_TYPES_TAGS_PATH;

    public static Path BLOCK_STATES_PATH;
    public static Path MODELS_PATH;
    public static Path ITEM_MODELS_PATH;
    public static Path BLOCK_MODELS_PATH;

    public static Path MC_DATA_PATH;
    public static Path MC_TAGS_PATH;
    public static Path MC_BLOCK_TAGS_PATH;
    public static Path MC_ITEM_TAGS_PATH;
    public static Path MC_MINEABLE_TAGS_PATH;

    public static Path COMMON_DATA_PATH;
    public static Path COMMON_TAGS_PATH;
    public static Path COMMON_BLOCK_TAGS_PATH;
    public static Path COMMON_ITEM_TAGS_PATH;


    public static void init() {
        try {
            deleteDirectory(FMLPaths.CONFIGDIR.get().resolve(IgleeModpackUtilities.MODID + "/pack"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        ROOT_PATH = FMLPaths.CONFIGDIR.get().resolve(IgleeModpackUtilities.MODID + "/pack");
        ROOT_PATH.toFile().mkdirs();
        if (FMLEnvironment.dist == Dist.CLIENT) {
            Optional<String> logoOpt = ModList.get().getModFileById(IgleeModpackUtilities.MODID).getFile().getModInfos().getFirst().getLogoFile();
            logoOpt.ifPresent(logoFile ->{
                TextureManager tm = Minecraft.getInstance().getTextureManager();
                final Pack.ResourcesSupplier resourcePack = ResourcePackLoader.getPackFor(IgleeModpackUtilities.MODID)
                        .orElse(ResourcePackLoader.getPackFor("neoforge").orElseThrow(() -> new RuntimeException("Can't find neoforge, WHAT!")));
                try (PackResources packResources = resourcePack.openPrimary(new PackLocationInfo("mod/" + IgleeModpackUtilities.MODID, Component.empty(), PackSource.BUILT_IN, Optional.empty()))) {
                    IoSupplier<InputStream> logoResource = packResources.getRootResource(logoFile.split("[/\\\\]"));
                    if (logoResource != null)
                    {
                        try (OutputStream writer = new FileOutputStream(new File(ROOT_PATH.toFile(), "pack.png"))) {
                            writer.write(logoResource.get().readAllBytes());
                        }
                    }
                } catch (IOException | IllegalArgumentException ignored) {}
            });


        }
        ASSETS_PATH = ROOT_PATH.resolve("assets/resourcefulshulkers");
        DATAS_PATH = ROOT_PATH.resolve("data/resourcefulshulkers");

        BLOCK_STATES_PATH = ASSETS_PATH.resolve("blockstates");
        LANGS_PATH = ASSETS_PATH.resolve("lang");
        MODELS_PATH = ASSETS_PATH.resolve("models");

        RECIPES_PATH = DATAS_PATH.resolve("recipe");
        LOOT_TABLES_PATH = DATAS_PATH.resolve("loot_table/blocks");
        TAGS_PATH = DATAS_PATH.resolve("tags");


        ITEMS_TAGS_PATH = TAGS_PATH.resolve("item");
        ENTITY_TYPES_TAGS_PATH = TAGS_PATH.resolve("entity_type");

        ITEM_MODELS_PATH = MODELS_PATH.resolve("item");
        BLOCK_MODELS_PATH = MODELS_PATH.resolve("block");

        MC_DATA_PATH = ROOT_PATH.resolve("data/minecraft");
        MC_TAGS_PATH = MC_DATA_PATH.resolve("tags");
        MC_BLOCK_TAGS_PATH = MC_TAGS_PATH.resolve("block");
        MC_ITEM_TAGS_PATH = MC_TAGS_PATH.resolve("item");
        MC_MINEABLE_TAGS_PATH = MC_BLOCK_TAGS_PATH.resolve("mineable");

        COMMON_DATA_PATH = ROOT_PATH.resolve("data/c");
        COMMON_TAGS_PATH = COMMON_DATA_PATH.resolve("tags");
        COMMON_BLOCK_TAGS_PATH = COMMON_TAGS_PATH.resolve("block");
        COMMON_ITEM_TAGS_PATH = COMMON_TAGS_PATH.resolve("item");

        BLOCK_STATES_PATH.toFile().mkdirs();
        LANGS_PATH.toFile().mkdirs();
        ITEM_MODELS_PATH.toFile().mkdirs();
        BLOCK_MODELS_PATH.toFile().mkdirs();
        RECIPES_PATH.toFile().mkdirs();
        LOOT_TABLES_PATH.toFile().mkdirs();
        TAGS_PATH.toFile().mkdirs();

        ITEMS_TAGS_PATH.toFile().mkdirs();
        ENTITY_TYPES_TAGS_PATH.toFile().mkdirs();

        MC_TAGS_PATH.toFile().mkdirs();
        MC_BLOCK_TAGS_PATH.toFile().mkdirs();
        MC_ITEM_TAGS_PATH.toFile().mkdirs();
        MC_MINEABLE_TAGS_PATH.toFile().mkdirs();

        COMMON_TAGS_PATH.toFile().mkdirs();
        COMMON_BLOCK_TAGS_PATH.toFile().mkdirs();
        COMMON_ITEM_TAGS_PATH.toFile().mkdirs();



        JsonObject emptyJson = new JsonObject();
        try {
            FileWriter writer = new FileWriter(new File(ASSETS_PATH.toFile(), "sounds.json"));
            writer.write(new Gson().toJson(emptyJson));
            writer.close();
        }catch (Exception ignored){}

    }

    public static void deleteDirectory(Path sourceDirectory) throws IOException {
        if (!sourceDirectory.toFile().exists()) return;

        for (String f : sourceDirectory.toFile().list()) {
            deleteDirectoryCompatibilityMode(new File(sourceDirectory.toFile(), f).toPath());
        }
        sourceDirectory.toFile().delete();
    }

    public static void deleteDirectoryCompatibilityMode(Path source) throws IOException {
        if (source.toFile().isDirectory()) {
            deleteDirectory(source);
        } else {
            source.toFile().delete();
        }
    }

}