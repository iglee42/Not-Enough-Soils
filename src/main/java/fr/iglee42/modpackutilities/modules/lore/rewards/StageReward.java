package fr.iglee42.modpackutilities.modules.lore.rewards;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.iglee42.modpackutilities.compat.CommonCompatsHelper;
import fr.iglee42.modpackutilities.compat.ftb.lib.FTBLibraryHelper;
import fr.iglee42.modpackutilities.compat.kubejs.KubeJSHelper;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Player;
import net.neoforged.fml.ModList;
import org.jetbrains.annotations.NotNull;

public record StageReward(String stageId) implements LoreReward {
    public static final MapCodec<StageReward> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    Codec.STRING.fieldOf("stage").forGetter(r -> r.stageId)
            ).apply(instance, StageReward::new)
    );

    public static final StreamCodec<RegistryFriendlyByteBuf,StageReward> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8,StageReward::stageId,
            StageReward::new
    );

    @Override
    public void execute(Player player) {
        if (stageId.trim().isEmpty()) return;
        if (ModList.get().isLoaded("kubejs"))
            if (KubeJSHelper.giveStage(player,stageId()))
                return;
        if (ModList.get().isLoaded("ftblibrary")){
            FTBLibraryHelper.giveStage(player, stageId());
        }
    }

    @Override
    public RewardType<? extends LoreReward> getType() {
        return CommonCompatsHelper.STAGE_REWARD.get();
    }

    @Override
    public @NotNull Component getTitle() {
        return Component.literal("Obtain stage : " + stageId());
    }
}
