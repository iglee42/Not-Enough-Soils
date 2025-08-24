package fr.iglee42.notenoughsoils;

import com.mojang.logging.LogUtils;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.neoforged.fml.ModList;

import java.io.IOException;

public class SNESReloadListener implements ResourceManagerReloadListener {
    @Override
    public void onResourceManagerReload(ResourceManager rm) {
        try {
            NotEnoughSoils.reloadConfig();
            if (ModList.get().isLoaded("mysticalagriculture")) MysticalUtils.reloadCropsSoils();
            LogUtils.getLogger().info("Successfully reloaded SNES and modified soils for {} plants", NotEnoughSoils.SOILS.size());
        } catch (Exception e) {
            LogUtils.getLogger().error("Failed to reload SNES: ", e);
        }
    }
}
