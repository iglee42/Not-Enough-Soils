package fr.iglee42.modpackutilities.compat.ftb.lib;

import dev.ftb.mods.ftblibrary.integration.stages.StageHelper;
import fr.iglee42.modpackutilities.compat.CommonCompatsHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;

public class FTBLibraryHelper {

    public static boolean hasStage(Player player, String stage) {
        return StageHelper.getInstance().getProvider().has(player, stage);
    }

    public static void giveStage(Player player, String stage) {
        if (!(player instanceof ServerPlayer sp)) return;
        StageHelper.getInstance().getProvider().add(sp, stage);
    }

    public static void registerLore(IEventBus bus) {
        if (ModList.get().isLoaded("kubejs"))
            return;
        CommonCompatsHelper.STAGE_REGISTER.register(bus);
        CommonCompatsHelper.STAGE_REWARD_REGISTER.register(bus);
    }

}
