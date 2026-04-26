package fr.iglee42.modpackutilities.modules.lore.requirements;

import com.mojang.serialization.MapCodec;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

import java.util.function.BiConsumer;
import java.util.function.Function;

public record RequirementType<R extends LoreRequirement>(
        MapCodec<R> codec,
        StreamCodec<RegistryFriendlyByteBuf,R> streamCodec
) {}
