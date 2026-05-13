package fr.iglee42.modpackutilities.modules.lore.rewards;

import com.mojang.serialization.Codec;
import fr.iglee42.modpackutilities.modules.lore.LoreKeys;
import fr.iglee42.modpackutilities.modules.lore.LoreModule;
import fr.iglee42.modpackutilities.modules.lore.requirements.LoreRequirement;
import fr.iglee42.modpackutilities.modules.lore.requirements.RequirementType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Player;

import java.util.List;
import java.util.function.BiConsumer;

public interface LoreReward {


    Codec<LoreReward> CODEC = LoreModule.LORE_REWARDS.byNameCodec().dispatch(
            LoreReward::getType,
            RewardType::codec
    );

    StreamCodec<RegistryFriendlyByteBuf, LoreReward> STREAM_CODEC = ByteBufCodecs.registry(LoreKeys.LORE_REWARD_KEY)
            .dispatch(
                    LoreReward::getType,
                    RewardType::streamCodec
            );


    void execute(Player player);

    RewardType<? extends LoreReward> getType();

    static List<LoreReward> readList(RegistryFriendlyByteBuf buffer){
        return STREAM_CODEC.apply(ByteBufCodecs.list()).decode(buffer);
    }

    static <R extends LoreReward> void writeList(RegistryFriendlyByteBuf buffer, List<R> list){
        STREAM_CODEC.apply(ByteBufCodecs.list()).encode(buffer, (List<LoreReward>) list);
    }

    Component getTitle();
}
