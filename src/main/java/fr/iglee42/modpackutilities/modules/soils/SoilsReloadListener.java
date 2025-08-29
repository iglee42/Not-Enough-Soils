package fr.iglee42.modpackutilities.modules.soils;

import com.mojang.logging.LogUtils;
import fr.iglee42.modpackutilities.IgleeModpackUtilities;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.neoforged.fml.ModList;

public class SoilsReloadListener implements ResourceManagerReloadListener {
    @Override
    public void onResourceManagerReload(ResourceManager rm) {
        SoilsModule module = IgleeModpackUtilities.getModule(SoilsModule.class);
        if (module == null) return;
        try {
            module.reloadConfig();
            if (ModList.get().isLoaded("mysticalagriculture")) MysticalUtils.reloadCropsSoils();
            LogUtils.getLogger().info("Successfully reloaded IMU soils and modified soils for {} plants", module.SOILS.size());
        } catch (Exception e) {
            LogUtils.getLogger().error("Failed to reload IMU soils: ", e);
        }
    }
}
