package fr.iglee42.modpackutilities.resourcepack;

import fr.iglee42.modpackutilities.IgleeModpackUtilities;
import net.neoforged.fml.loading.FMLPaths;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

public class PathConstant {

    public static Path ROOT_PATH;
    public static Path BASE_ASSETS_PATH;
    public static Path BASE_DATA_PATH;


    public static void init() {
        try {
            deleteDirectory(FMLPaths.CONFIGDIR.get().resolve(IgleeModpackUtilities.MODID + "/pack"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        ROOT_PATH = FMLPaths.CONFIGDIR.get().resolve(IgleeModpackUtilities.MODID + "/pack");
        ROOT_PATH.toFile().mkdirs();
        BASE_ASSETS_PATH = ROOT_PATH.resolve("assets");
        BASE_DATA_PATH = ROOT_PATH.resolve("data");

        BASE_ASSETS_PATH.toFile().mkdirs();
        BASE_DATA_PATH.toFile().mkdirs();
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