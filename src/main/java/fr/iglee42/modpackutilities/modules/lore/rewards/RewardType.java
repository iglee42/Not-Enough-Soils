package fr.iglee42.modpackutilities.modules.lore.rewards;

import com.mojang.serialization.MapCodec;
import net.minecraft.network.FriendlyByteBuf;

import java.util.function.BiConsumer;
import java.util.function.Function;

public record RewardType<R extends LoreReward>(
        MapCodec<R> codec,
        Function<FriendlyByteBuf,R> networkReader,
        BiConsumer<R,FriendlyByteBuf> networkWriter
) {}
