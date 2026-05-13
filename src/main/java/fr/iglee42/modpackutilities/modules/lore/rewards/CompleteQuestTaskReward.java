package fr.iglee42.modpackutilities.modules.lore.rewards;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.ftb.mods.ftbquests.api.FTBQuestsAPI;
import dev.ftb.mods.ftbquests.quest.BaseQuestFile;
import dev.ftb.mods.ftbquests.quest.QuestObject;
import dev.ftb.mods.ftbquests.quest.QuestObjectBase;
import dev.ftb.mods.ftbquests.quest.task.Task;
import fr.iglee42.modpackutilities.compat.ftb.quests.FTBQuestsHelper;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.jetbrains.annotations.NotNull;

public record CompleteQuestTaskReward(String taskId) implements LoreReward {

    public static final MapCodec<CompleteQuestTaskReward> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    Codec.STRING.fieldOf("task").forGetter(r -> r.taskId)
            ).apply(instance, CompleteQuestTaskReward::new)
    );

    public static final StreamCodec<RegistryFriendlyByteBuf,CompleteQuestTaskReward> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8,CompleteQuestTaskReward::taskId,
            CompleteQuestTaskReward::new
    );

    @Override
    public void execute(Player player) {
        BaseQuestFile file = FTBQuestsAPI.api().getQuestFile(player.level().isClientSide());
        Task task = file.getTask(QuestObjectBase.parseCodeString(taskId()));
        file.getOrCreateTeamData(player).markTaskCompleted(task);
    }

    @Override
    public RewardType<? extends LoreReward> getType() {
        return FTBQuestsHelper.TASK.get();
    }

    @Override
    public @NotNull Component getTitle() {
        BaseQuestFile file = FTBQuestsAPI.api().getQuestFile(ServerLifecycleHooks.getCurrentServer() == null);
        Task task = file.getTask(QuestObjectBase.parseCodeString(taskId()));
        if (task == null) return Component.literal("Complete unknown task " + taskId());
        return Component.literal("Complete task : " + task.getTitle().getString());
    }
}
