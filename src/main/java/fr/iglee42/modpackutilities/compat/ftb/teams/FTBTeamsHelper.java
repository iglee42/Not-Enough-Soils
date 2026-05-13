package fr.iglee42.modpackutilities.compat.ftb.teams;

import dev.ftb.mods.ftbteams.api.FTBTeamsAPI;
import dev.ftb.mods.ftbteams.api.Team;
import fr.iglee42.modpackutilities.modules.lore.LoreModule;
import fr.iglee42.modpackutilities.modules.lore.network.SyncLoreProgressPacket;
import fr.iglee42.modpackutilities.modules.lore.progress.LoreProgress;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.Optional;
import java.util.UUID;

public class FTBTeamsHelper {
    public static Optional<UUID> getTeamUUID(Player player) {
        return FTBTeamsAPI.api().getManager().getTeamForPlayer((ServerPlayer) player).map(Team::getTeamId);
    }

    public static void syncProgressToPlayerTeam(Player p, LoreProgress progress) {
        ServerPlayer player = (ServerPlayer) p;
        FTBTeamsAPI.api().getManager().getTeamForPlayer(player).ifPresent(team->{
            team.getOnlineMembers().stream().filter(p1->!p1.equals(player)).forEach(p1->{
                PacketDistributor.sendToPlayer(p1, new SyncLoreProgressPacket(progress));
            });
        });
    }
}
