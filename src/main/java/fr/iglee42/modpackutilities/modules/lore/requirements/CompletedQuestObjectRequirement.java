package fr.iglee42.modpackutilities.modules.lore.requirements;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.ftb.mods.ftbquests.api.FTBQuestsAPI;
import dev.ftb.mods.ftbquests.quest.BaseQuestFile;
import dev.ftb.mods.ftbquests.quest.QuestObject;
import dev.ftb.mods.ftbquests.quest.QuestObjectBase;
import fr.iglee42.igleelib.api.utils.ModsUtils;
import fr.iglee42.modpackutilities.compat.ftb.quests.FTBQuestsHelper;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.jetbrains.annotations.NotNull;

public record CompletedQuestObjectRequirement(String objectId) implements LoreRequirement {

    public static final MapCodec<CompletedQuestObjectRequirement> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    Codec.STRING.fieldOf("object").forGetter(r -> r.objectId)
            ).apply(instance, CompletedQuestObjectRequirement::new)
    );

    public static final StreamCodec<RegistryFriendlyByteBuf,CompletedQuestObjectRequirement> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8,CompletedQuestObjectRequirement::objectId,
            CompletedQuestObjectRequirement::new
    );

    @Override
    public boolean test(Player player) {
        BaseQuestFile file = FTBQuestsAPI.api().getQuestFile(player.level().isClientSide());
        QuestObject qo = file.get(QuestObjectBase.parseCodeString(objectId()));
        return qo != null && file.getOrCreateTeamData(player).isCompleted(qo);
    }

    @Override
    public RequirementType<? extends LoreRequirement> getType() {
        return FTBQuestsHelper.QUEST_OBJECT.get();
    }

    @Override
    public @NotNull Component getTitle() {
        BaseQuestFile file = FTBQuestsAPI.api().getQuestFile(ServerLifecycleHooks.getCurrentServer() == null);
        QuestObject qo = file.get(QuestObjectBase.parseCodeString(objectId()));
        if (qo == null) return Component.translatable("lore.requirements.unknown_quest_object", objectId());
        return Component.translatable("lore.requirements.quest_object" , ModsUtils.getUpperName(qo.getObjectType().name().toLowerCase(),"_"), qo.getTitle());
    }
}
