package fr.iglee42.modpackutilities.modules.lore;

import fr.iglee42.modpackutilities.IgleeModpackUtilities;
import fr.iglee42.modpackutilities.modules.lore.client.LoreGui;
import fr.iglee42.modpackutilities.modules.lore.progress.IProgressHandler;
import fr.iglee42.modpackutilities.modules.lore.progress.LoreProgress;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Player;

import java.util.*;


public class ClientLoreModule implements IProgressHandler {

    public static ClientLoreModule INSTANCE;
    private LoreProgress progress;
    private Map<ResourceLocation, LoreFile> loreFiles = null;

    private ClientLoreModule() {
        this.loreFiles = new HashMap<>();
    }

    @Override
    public LoreProgress getProgress(Player player) {
        return progress;
    }

    public static ClientLoreModule getInstance(){
        if (INSTANCE == null)
            INSTANCE = new ClientLoreModule();
        return INSTANCE;
    }

    public Map<ResourceLocation,LoreFile> getLoreFiles() {
        if (loreFiles == null) return null;
        return Collections.unmodifiableMap(loreFiles);
    }

    public Optional<LoreFile> getLoreFile(ResourceLocation id) {
        return Optional.ofNullable(loreFiles.get(id));
    }

    public boolean isKnownFile(ResourceLocation id) {
        return loreFiles.containsKey(id);
    }

    public void syncFromServer(Collection<LoreFile> files) {

        loreFiles.clear();
        files.forEach(file -> this.loreFiles.put(file.id(), file));

        IgleeModpackUtilities.getModule(LoreModule.class).info("{} lore files sync'd from server", files.size());
    }


    public void syncProgressFromServer(LoreProgress progress) {
        this.progress = progress;
        if (Minecraft.getInstance().screen instanceof LoreGui gui)
            gui.resize(Minecraft.getInstance(),gui.width,gui.height);
    }

    public void openLoreGui(ResourceLocation fileId, BlockPos bePos){
        Optional<LoreFile> file = getLoreFile(fileId);
        Minecraft.getInstance().setScreen(new LoreGui(file.orElse(null),bePos));
    }
}
