package fr.iglee42.modpackutilities.compat.kubejs;

import dev.latvian.mods.kubejs.core.PlayerKJS;
import fr.iglee42.modpackutilities.compat.CommonCompatsHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.eventbus.api.IEventBus;

public class KubeJSHelper {

    public static boolean doesPlayerHasStage(Player player, String stage){
        return player instanceof PlayerKJS kjs && kjs.kjs$getStages().has(stage);
    }

    public static void registerLore(IEventBus bus){
        CommonCompatsHelper.STAGE_REGISTER.register(bus);
        CommonCompatsHelper.STAGE_REWARD_REGISTER.register(bus);
    }

    public static boolean giveStage(Player player, String stage) {
        return player instanceof PlayerKJS kjs && kjs.kjs$getStages().add(stage);
    }
}
