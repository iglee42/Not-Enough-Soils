package fr.iglee42.modpackutilities.modules.lore.requirements;

import com.mojang.serialization.MapCodec;
import net.minecraft.network.FriendlyByteBuf;

import java.util.function.BiConsumer;
import java.util.function.Function;

public record RequirementType<R extends LoreRequirement>(
        MapCodec<R> codec,
        Function<FriendlyByteBuf,R> networkReader,
        BiConsumer<R,FriendlyByteBuf> networkWriter
) {}
