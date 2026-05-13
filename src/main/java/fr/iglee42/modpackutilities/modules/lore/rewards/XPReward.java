package fr.iglee42.modpackutilities.modules.lore.rewards;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.iglee42.modpackutilities.modules.lore.LoreModule;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

public record XPReward(int amount, boolean levels) implements LoreReward {

    public static final MapCodec<XPReward> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    ExtraCodecs.POSITIVE_INT.fieldOf("amount").forGetter(r -> r.amount),
                    Codec.BOOL.optionalFieldOf("levels",false).forGetter(r -> r.levels)
            ).apply(instance, XPReward::new)
    );

    public static final StreamCodec<RegistryFriendlyByteBuf,XPReward> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, XPReward::amount,
            ByteBufCodecs.BOOL,XPReward::levels,
            XPReward::new
    );


    @Override
    public void execute(Player player) {
        if (levels) player.giveExperienceLevels(amount());
        else player.giveExperiencePoints(amount());
    }

    @Override
    public RewardType<? extends LoreReward> getType() {
        return LoreModule.XP_REWARD;
    }

    @Override
    public @NotNull Component getTitle() {
        return Component.translatable("lore.reward.xp"+(levels? "_levels" :""),amount());
    }
}
